/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.whirr.cli.command;

import static org.apache.whirr.RolePredicates.anyRoleIn;
import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.compute.options.RunScriptOptions.Builder.overrideCredentialsWith;
import static org.jclouds.compute.options.RunScriptOptions.Builder.overrideLoginCredentials;
import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.compute.predicates.NodePredicates.withIds;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import joptsimple.OptionSet;

import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.InstanceTemplate;
import org.apache.whirr.command.AbstractClusterCommand;
import org.apache.whirr.compute.BootstrapTemplate;
import org.apache.whirr.compute.NodeStarterFactory;
import org.apache.whirr.compute.StartupProcess;
import org.apache.whirr.service.ComputeCache;
import org.apache.whirr.service.jclouds.StatementBuilder;
import org.apache.whirr.service.jclouds.TemplateBuilderStrategy;
import org.apache.whirr.service.zookeeper.ZooKeeperClusterActionHandler;
import org.apache.whirr.state.ClusterStateStore;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.domain.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RepairClusterCommand extends AbstractClusterCommand {

	private static final Logger LOG = LoggerFactory
			.getLogger(RepairClusterCommand.class);

	public RepairClusterCommand() {
		super("repair-cluster", "Repair a running cluster by "
				+ "rebuilding missing nodes", new ClusterControllerFactory());
	}

	@Override
	public int run(InputStream in, PrintStream out, PrintStream err,
			List<String> args) throws Exception {
		OptionSet optionSet = parser.parse(args.toArray(new String[0]));
		try {
			ClusterSpec spec = getClusterSpec(optionSet);
			Credentials credentials = new Credentials(spec.getClusterUser(),
					spec.getPrivateKey());

			ClusterController controller = createClusterController(spec
					.getServiceName());
			ClusterStateStore stateStore = createClusterStateStore(spec);

			/*
			 * Steps needed for repairing a ZooKeeper cluster:
			 * 
			 * -1, make a diff and find the list of instances that need to be
			 * repaired
			 */

			Map<Set<String>, Integer> missing = getMissingRoles(
					controller.getInstances(spec, stateStore),
					spec.getInstanceTemplates());

			if (missing.size() == 0) {
				LOG.info("Nothing to repair.");
				return 0;
			}

			if (LOG.isInfoEnabled()) {
				for (Set<String> roles : missing.keySet()) {
					LOG.info("Missing: '{}' ({})", Joiner.on(",").join(roles),
							missing.get(roles));
				}
			}

			if (!missing.containsKey(Sets.newHashSet("zookeeper"))) {
				LOG.error("Found no missing 'zookeeper' instances.");
				return 0;
			}

			if (missing.size() > 1) {
				LOG.warn("Ignoring: {}", Sets.filter(missing.keySet(),
						new Predicate<Set<String>>() {
							final Set<String> roles = Sets
									.newHashSet("zookeeper");

							@Override
							public boolean apply(@Nullable Set<String> s) {
								return !roles.equals(s);
							}
						}));
			}

			/**
			 * 0. start a new set of machines and record the private and public
			 * IPs
			 */

			String zookeeperRole = ZooKeeperClusterActionHandler.ZOOKEEPER_ROLE;
			int count = missing.get(Sets.newHashSet(zookeeperRole));

			ComputeService computeService = ComputeCache.INSTANCE.apply(spec)
					.getComputeService();
			ExecutorService executors = Executors.newCachedThreadPool();

			/*
			 * push the statements needed in the bootstrap / service install
			 * phase
			 */
			StatementBuilder builder = new StatementBuilder();

			builder.addStatements(
					call("install_java"),
					call("install_tarball"),
					call("install_service"),
					call("install_zookeeper",
							spec.getConfiguration()
									.getString(
											"whirr.zookeeper.tarball.url",
											"http://archive.apache.org/dist/zookeeper/zookeeper-3.3.3/zookeeper-3.3.3.tar.gz")));

			final Set<? extends NodeMetadata> newNodes = (new StartupProcess(
					spec.getClusterName(),
					count, /* minCount= */
					count, /* retries= */
					1,
					Sets.newHashSet(zookeeperRole),
					computeService,
					BootstrapTemplate.build(
							spec,
							computeService,
							builder,
							new TemplateBuilderStrategy(),
							spec.getInstanceTemplate(ZooKeeperClusterActionHandler.ZOOKEEPER_ROLE)),
					executors, new NodeStarterFactory())).call();

			/* register the new instance to the cluster */
			Cluster cluster = stateStore.load();
			cluster.getInstances().addAll(
					getInstances(Sets.newHashSet(zookeeperRole), newNodes));
			stateStore.save(cluster);

			/*
			 * The easy way (with small downtime) - stop all zookeeper nodes -
			 * reconfigure and start
			 */
			Predicate<NodeMetadata> condition = Predicates.and(
					runningInGroup(spec.getClusterName()),
					buildFilterPredicate(cluster));

			builder = new StatementBuilder();
			builder.addStatement(call("stop_zookeeper"));
			Statement statement = builder.build(spec);
			
			RunScriptOptions options = defaultRunScriptOptionsForSpec(spec);

//			computeService.runScriptOnNodesMatching(predicate, builder,
//					overrideCredentialsWith(credentials).wrapInInitScript(true)
//							.runAsRoot(true));
			computeService.runScriptOnNodesMatching(condition, statement,options);

			String servers = Joiner.on(' ').join(
					getPrivateIps(cluster
							.getInstancesMatching(role(zookeeperRole))));

			builder = new StatementBuilder();
			builder.addStatements(
					call("configure_zookeeper", "-c", spec.getProvider(),
							servers), call("start_zookeeper"));
			statement = builder.build(spec);

			computeService.runScriptOnNodesMatching(condition, statement,options);

			/*
			 * The hard way (with no downtime) 1. reconfigure existing cluster
			 * nodes by replacing the ip for the failed machine with a new one
			 * (in a rolling restart fashion) stop ~> configure ~> start (be
			 * careful with myid)
			 * 
			 * 2. configure the new machine and start zookeeper configure ~>
			 * start
			 */

			/*
			 * (3. make clients aware of the fact that the ensemble IPs changed)
			 * 
			 * If the ZooKeeper cluster has >= 5 nodes the clients should see no
			 * downtime.
			 * 
			 * No data-loss should occur while rebuilding the cluster.
			 */

		} catch (IllegalArgumentException e) {
			err.println(e.getMessage());
			return -1;
		}
		return 0;
	}

	private Predicate<NodeMetadata> buildFilterPredicate(Cluster cluster)
			throws IOException {
		List<String> ids = Lists.newArrayList();

		for (Cluster.Instance instance : cluster
				.getInstancesMatching(anyRoleIn(Sets
						.<String> newHashSet("zookeeper")))) {
			ids.add(instance.getId().split("\\/")[1]);
		}

		return Predicates.and(Predicates.alwaysTrue(),
				withIds(ids.toArray(new String[0])));
	}

	private List<String> getPrivateIps(Set<Cluster.Instance> instances) {
		return Lists.transform(Lists.newArrayList(instances),
				new Function<Cluster.Instance, String>() {
					@Override
					public String apply(Cluster.Instance instance) {
						return instance.getPrivateIp();
					}
				});
	}

	private Set<Cluster.Instance> getInstances(final Set<String> roles,
			Set<? extends NodeMetadata> nodes) {
		return Sets.newLinkedHashSet(Collections2.transform(
				Sets.newLinkedHashSet(nodes),
				new Function<NodeMetadata, Cluster.Instance>() {
					@Override
					public Cluster.Instance apply(NodeMetadata node) {
						return new Cluster.Instance(node.getCredentials(),
								roles, Iterables.get(node.getPublicAddresses(),
										0), Iterables.get(
										node.getPrivateAddresses(), 0), node
										.getId(), node);
					}
				}));
	}

	private Map<Set<String>, Integer> getMissingRoles(
			Set<Cluster.Instance> instances, List<InstanceTemplate> templates) {
		Map<Set<String>, Integer> missing = Maps.newHashMap();

		for (InstanceTemplate template : templates) {
			int count = countInstanceRunning(instances, template.getRoles());
			if (count < template.getNumberOfInstances()) {
				missing.put(template.getRoles(),
						template.getNumberOfInstances() - count);
			}
		}

		return missing;
	}

	private int countInstanceRunning(Set<Cluster.Instance> instances,
			final Set<String> roles) {
		return Sets.filter(instances, new Predicate<Cluster.Instance>() {
			@Override
			public boolean apply(@Nullable Cluster.Instance instance) {
				return instance.getRoles().containsAll(roles);
			}
		}).size();
	}
	
	public RunScriptOptions defaultRunScriptOptionsForSpec(ClusterSpec spec) {
		LoginCredentials credentials = LoginCredentials.builder()
				.user(spec.getClusterUser()).privateKey(spec.getPrivateKey())
				.build();
		return overrideLoginCredentials(credentials).wrapInInitScript(false)
				.runAsRoot(false);
	}

}
