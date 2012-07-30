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

import static org.jclouds.compute.options.RunScriptOptions.Builder.overrideLoginCredentials;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import joptsimple.OptionSet;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.InstanceTemplate;
import org.apache.whirr.RolePredicates;
import org.apache.whirr.command.AbstractClusterCommand;
import org.apache.whirr.compute.BootstrapTemplate;
import org.apache.whirr.compute.NodeStarterFactory;
import org.apache.whirr.compute.StartupProcess;
import org.apache.whirr.service.ComputeCache;
import org.apache.whirr.service.cassandra.CassandraClusterActionHandler;
import org.apache.whirr.service.cassandra.CassandraHelper;
import org.apache.whirr.service.ganglia.GangliaMonitorClusterActionHandler;
import org.apache.whirr.service.ganglia.GangliaMonitorHelper;
import org.apache.whirr.service.jclouds.StatementBuilder;
import org.apache.whirr.service.jclouds.TemplateBuilderStrategy;
import org.apache.whirr.service.ycsb.YcsbClusterActionHandler;
import org.apache.whirr.service.ycsb.YcsbHelper;
import org.apache.whirr.service.zookeeper.ZkHelper;
import org.apache.whirr.service.zookeeper.ZooKeeperClusterActionHandler;
import org.apache.whirr.state.ClusterStateStore;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.domain.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;

public class RepairClusterCommand extends AbstractClusterCommand {

	private static final Logger LOG = LoggerFactory
			.getLogger(RepairClusterCommand.class);
	public static String REPAIR_ACTION = "repair";
	public static List<String> ROLES = new ArrayList<String>();
	public static final String REPAIR_ROLES ="repair-roles";
	public final static String WHIRR_REPAIR_ROLES = "whirr."+REPAIR_ROLES;

	public RepairClusterCommand() {
		super(REPAIR_ACTION + "-cluster", "Repair a running cluster by "
				+ "rebuilding missing nodes", new ClusterControllerFactory());
	}

	public static boolean MISSING = false;
	public static boolean SUPERFLUOUS = false;

	@Override
	public int run(InputStream in, PrintStream out, PrintStream err,
			List<String> args) throws Exception {
		parser.accepts(REPAIR_ROLES);
		OptionSet optionSet = parser.parse(args.toArray(new String[0]));

		try {
			ClusterSpec spec = getClusterSpec(optionSet);
			ClusterController controller = createClusterController(spec
					.getServiceName());
			ClusterStateStore stateStore = createClusterStateStore(spec);

			Configuration config = spec.getConfiguration();
			// read roles from command line "--repair-roles=xyz" (or from the
			// properties file)
			String repairRoles = config.getString(WHIRR_REPAIR_ROLES, null); //(String) optionSet.valueOf(REPAIR_ROLES); TODO 
			LOG.info("Repair roles: "+repairRoles);
			for (String role : repairRoles.split(" ")) {
				ROLES.add(role);
			}

			/*
			 * Steps needed for repairing a ZooKeeper cluster:
			 * 
			 * -1, make a diff and find the list of instances that need to be
			 * repaired
			 */

			Map<Set<String>, Integer> missing = getMissingRoles(
					controller.getInstances(spec, stateStore),
					spec.getInstanceTemplates());
			Map<Set<String>, Integer> superfluous = getSuperfluousRoles(
					controller.getInstances(spec, stateStore),
					spec.getInstanceTemplates());

			// There is no difference between the template and the running
			// cluster.
			if (missing.size() == 0 && superfluous.size() == 0) {
				LOG.info("Nothing to repair.");
				return 0;
			}

			if (missing.size() > 0) {
				MISSING = true;
				if (LOG.isInfoEnabled()) {
					for (Set<String> roles : missing.keySet()) {
						LOG.info("Missing: '{}' ({})",
								Joiner.on(",").join(roles), missing.get(roles));
					}
				}
			}
			if (superfluous.size() > 0) {
				SUPERFLUOUS = true;
				if (LOG.isInfoEnabled()) {
					for (Set<String> roles : superfluous.keySet()) {
						LOG.info("Superfluous: '{}' ({})",
								Joiner.on(",").join(roles),
								superfluous.get(roles));
					}
				}
			}

			// if (!missing.containsKey(Sets.newHashSet(ROLE))) {
			// LOG.error("Found no missing '" + ROLE + "' instances.");
			// if (!superfluous.containsKey(Sets.newHashSet(ROLE))) {
			// LOG.error("Found no superfluous '" + ROLE + "' instances.");
			// return 0;
			// }
			// }

			if (missing.size() > 1) {
				LOG.warn("Ignoring: {}", Sets.filter(missing.keySet(),
						new Predicate<Set<String>>() {
							final Set<String> roles = Sets.newHashSet(ROLES);

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
			if (MISSING) {
				int count = missing.get(Sets.newHashSet(ROLES));

				ComputeService computeService = ComputeCache.INSTANCE.apply(
						spec).getComputeService();
				ExecutorService executors = Executors.newCachedThreadPool();

				InstanceTemplate instanceTemplate = spec
						.getInstanceTemplate(Sets.newHashSet(ROLES));
				/*
				 * push the statements needed in the bootstrap / service install
				 * phase
				 */
				StatementBuilder builder = new StatementBuilder();
				if (ROLES
						.contains(YcsbClusterActionHandler.YCSB_ROLE)) {
					YcsbHelper ycsbHelper = new YcsbHelper();
					for (Statement s : ycsbHelper.getStatements(spec))
						builder.addStatement(s);
				}
				if (ROLES
						.contains(ZooKeeperClusterActionHandler.ZOOKEEPER_ROLE)) {
					ZkHelper zkHelper = new ZkHelper();
					for (Statement s : zkHelper.getStatements(spec))
						builder.addStatement(s);
				}
				if (ROLES
						.contains(GangliaMonitorClusterActionHandler.GANGLIA_MONITOR_ROLE)) {
					GangliaMonitorHelper gmondHelper = new GangliaMonitorHelper();
					for (Statement s : gmondHelper.getStatements(spec))
						builder.addStatement(s);
				}
				if (ROLES
						.contains(CassandraClusterActionHandler.CASSANDRA_ROLE)) {
					CassandraHelper cassHelper = new CassandraHelper();
					for (Statement s : cassHelper.getStatements(spec))
						builder.addStatement(s);
				}
//				if (ROLES
//						.contains(HBaseMasterClusterActionHandler.ROLE)) {
//					HBaseMasterHelper hmHelper = new HBaseMasterHelper();
//					for (Statement s : hmHelper.getStatements(spec))
//						builder.addStatement(s);
//				}
//				if (ROLES
//						.contains(HBaseRegionServerClusterActionHandler.ROLE)) {
//					HBaseRegionServerHelper hrsHelper = new HBaseRegionServerHelper();
//					for (Statement s : hrsHelper.getStatements(spec))
//						builder.addStatement(s);
//				}
				
				// stop the services
				controller.stopServices(spec);

				// build and bootstrap new nodes
				final Set<? extends NodeMetadata> newNodes = (new StartupProcess(
						spec.getClusterName(), count, /* minCount= */
						count, /* retries= */
						1, Sets.newHashSet(ROLES), computeService,
						BootstrapTemplate
								.build(spec, computeService, builder,
										new TemplateBuilderStrategy(),
										instanceTemplate), executors,
						new NodeStarterFactory())).call();

				/* register the new instance to the cluster */
				Cluster cluster = stateStore.load();
				cluster.getInstances().addAll(
						getInstances(Sets.newHashSet(ROLES), newNodes));
				stateStore.save(cluster);
				/*
				 * The easy way (with small downtime) - stop all zookeeper nodes
				 * - reconfigure and start
				 */
				
				// re-configure the cluster and start services
				controller.configureServices(spec);
				controller.startServices(spec);
			}

			if (SUPERFLUOUS) {
				/**
				 * 0. destroy the superfluous instances
				 */
				
				// Stop all nodes
				controller.stopServices(spec);

				// Now, kill the superfluous ones
				int count = superfluous.get(Sets.newHashSet(ROLES));

				Cluster cluster = stateStore.load();
				Predicate<Cluster.Instance> predicate = RolePredicates
						.allRolesIn(Sets.newHashSet(ROLES));
				Set<Instance> toDestroyInstances = cluster
						.getInstancesMatching(predicate);
				Set<NodeMetadata> destroyedInstances = new HashSet<NodeMetadata>();
				int i = 0;
				for (Instance instance : toDestroyInstances) {
					if (i < count) {
						String instanceId = instance.getId();
						LOG.info("Preparing to destroy instance " + instanceId);
						controller.destroyInstance(spec, instanceId);
						destroyedInstances.add(instance.getNodeMetadata());
						i++;
					}
				}

				// cluster.getInstances().removeAll(
				// getInstances(Sets.newHashSet(zookeeperRole),
				// destroyedInstances));
				// stateStore.save(cluster);
				/*
				 * The easy way (with small downtime) - stop all zookeeper nodes
				 * - reconfigure and start
				 */
				
				// Re-configure and start the services
				controller.configureServices(spec);
				controller.startServices(spec);
			}

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

	// public static Template build(
	// final ClusterSpec clusterSpec,
	// ComputeService computeService,
	// StatementBuilder statementBuilder,
	// TemplateBuilderStrategy strategy,
	// InstanceTemplate instanceTemplate
	// ) {
	// String name = REPAIR_ACTION+"-" +
	// Joiner.on('_').join(instanceTemplate.getRoles());
	//
	// LOG.info("Configuring template for {}", name);
	//
	// statementBuilder.name(name);
	// ensureUserExistsAndAuthorizeSudo(statementBuilder,
	// clusterSpec.getClusterUser(),
	// clusterSpec.getPublicKey(), clusterSpec.getPrivateKey());
	// Statement bootstrap = statementBuilder.build(clusterSpec);
	//
	// if (LOG.isDebugEnabled()) {
	// LOG.debug("Running script {}:\n{}", name,
	// bootstrap.render(OsFamily.UNIX));
	// }
	//
	// TemplateBuilder templateBuilder = computeService.templateBuilder()
	// .options(runScript(bootstrap));
	// strategy.configureTemplateBuilder(clusterSpec, templateBuilder,
	// instanceTemplate);
	//
	// return setSpotInstancePriceIfSpecified(
	// computeService.getContext(), clusterSpec, templateBuilder.build(),
	// instanceTemplate
	// );
	// }

	public ListenableFuture<ExecResponse> runStatementOnInstanceInCluster(
			ComputeService computeService, StatementBuilder statementBuilder,
			Instance instance, ClusterSpec clusterSpec, RunScriptOptions options) {
		Statement statement = statementBuilder.name(
				REPAIR_ACTION + "-" + Joiner.on('_').join(instance.getRoles()))
				.build(clusterSpec, instance);
		return computeService.submitScriptOnNode(instance.getId(), statement,
				options);
	}

	// private Predicate<NodeMetadata> buildFilterPredicate(Cluster cluster)
	// throws IOException {
	// List<String> ids = Lists.newArrayList();
	//
	// for (Cluster.Instance instance : cluster
	// .getInstancesMatching(anyRoleIn(Sets
	// .<String> newHashSet("zookeeper")))) {
	// ids.add(instance.getId().split("\\/")[1]);
	// }
	//
	// return Predicates.and(Predicates.alwaysTrue(),
	// withIds(ids.toArray(new String[0])));
	// }

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

	private Map<Set<String>, Integer> getSuperfluousRoles(
			Set<Cluster.Instance> instances, List<InstanceTemplate> templates) {
		Map<Set<String>, Integer> superfluous = Maps.newHashMap();

		for (InstanceTemplate template : templates) {
			int count = countInstanceRunning(instances, template.getRoles());
			if (count > template.getNumberOfInstances()) {
				superfluous.put(template.getRoles(),
						count - template.getNumberOfInstances());
			}
		}

		return superfluous;
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
		return overrideLoginCredentials(credentials).wrapInInitScript(true)
				.runAsRoot(true);
	}

}
