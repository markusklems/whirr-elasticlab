package org.apache.whirr.service.ganglia;

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

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.ExperimentParameters;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class GangliaMetadClusterActionHandler extends
		ClusterActionHandlerSupport {

	private static final Logger LOG = LoggerFactory
			.getLogger(GangliaMetadClusterActionHandler.class);

	public static final String GANGLIA_METAD_ROLE = "ganglia-metad";
	public static final int GANGLIA_META_PORT = 8651;
	public static final int HTTP_PORT = 80;

	// Experiment parameters
	public static final String MAJOR_VERSION = "whirr.ycsb.version.major";
	public static final String WORKLOAD_REPO_GIT = "whirr.ycsb.workload.repo.git";

	@Override
	public String getRole() {
		return GANGLIA_METAD_ROLE;
	}

	protected Configuration getConfiguration(ClusterSpec spec)
			throws IOException {
		return getConfiguration(spec, "whirr-ganglia-default.properties");
	}

	protected String getInstallFunction(Configuration config) {
		return getInstallFunction(config, getRole(),
				GangliaCluster.INSTALL_FUNCTION);
	}

	protected String getConfigureFunction(Configuration config) {
		return getConfigureFunction(config, getRole(),
				GangliaCluster.CONFIGURE_FUNCTION);
	}

	@Override
	protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
		ClusterSpec clusterSpec = event.getClusterSpec();
		Configuration config = getConfiguration(clusterSpec);

		addStatement(event,
				call(getInstallFunction(config), "-r", GANGLIA_METAD_ROLE));
	}

	@Override
	protected void beforeConfigure(ClusterActionEvent event)
			throws IOException, InterruptedException {
		ClusterSpec clusterSpec = event.getClusterSpec();
		Cluster cluster = event.getCluster();

		// FIXME: the ganglia port is only opened so the ganglia xml dump can be
		// checked in GangliaServiceTest
		event.getFirewallManager().addRule(
				Rule.create().destination(role(GANGLIA_METAD_ROLE))
						.ports(HTTP_PORT, GANGLIA_META_PORT));

		Configuration config = getConfiguration(clusterSpec);
		String configureFunction = getConfigureFunction(config);

		// Call the configure function.
		String metadHost = cluster
				.getInstanceMatching(role(GANGLIA_METAD_ROLE)).getPrivateIp();
		addStatement(event, call(configureFunction, "-m", metadHost));

		// clone experiment github repo
		addStatement(event, call("install_git"));
		String repo = event.getClusterSpec().getConfiguration()
				.getString(WORKLOAD_REPO_GIT, null);

		// clone the workload repository
		addStatement(event, call("setup_github"));
		addStatement(event, call("update_workload_repo", repo));
	}

	@Override
	protected void afterConfigure(ClusterActionEvent event) {
		ClusterSpec clusterSpec = event.getClusterSpec();
		Cluster cluster = event.getCluster();

		LOG.info("Completed configuration of {}", clusterSpec.getClusterName());
		String hosts = Joiner.on(',')
				.join(getHosts(cluster
						.getInstancesMatching(role(GANGLIA_METAD_ROLE))));
		LOG.info(
				"Meta host: {}. You should be able to connect on http://{}/ganglia",
				hosts, hosts);
	}

	@Override
	protected void beforeRunExperiment(ClusterActionEvent event)
			throws IOException, InterruptedException {
		ClusterSpec clusterSpec = event.getClusterSpec();
		Configuration config = clusterSpec.getConfiguration();
		
		final String experimentAction = config
				.getString(ExperimentParameters.YCSB_EXPERIMENT_ACTION, null);
		final String workload = config.getString(ExperimentParameters.YCSB_WORKLOAD_FILE, null);
		final String major = config.getString(MAJOR_VERSION, null);
		final String phase = config.getString(ExperimentParameters.YCSB_WORKLOAD_PHASE, null);
		// experiment phase
		final String load = ExperimentParameters.EXPERIMENT_PHASE_VALUE.EXPERIMENT_PHASE_LOAD.toString();
		final String transaction = ExperimentParameters.EXPERIMENT_PHASE_VALUE.EXPERIMENT_PHASE_TRANSACTION.toString();
		// experiment action
		final String prepare = ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_PREPARE
				.toString();
		final String run = ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_RUN.toString();
		final String stopmonitoring = ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_STOP_MONITORING.toString();
		final String uploadmonitoring = ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_UPLOAD_MONITORING_DATA
				.toString();
		

		if (experimentAction.equalsIgnoreCase(prepare)) {
			// do nothing
		} else if (experimentAction.equalsIgnoreCase(run)) {
			// record start time
			addStatement(event, call("save_start_time"));
		} else if (experimentAction.equalsIgnoreCase(stopmonitoring)) {
			// record end time	
			addStatement(event, call("save_end_time"));
	    // TODO: this used to create a race condition because ycsb also pushes using the "upload" method. This is not a great solution but it works for small numbers of "observer" services.
		} else if (experimentAction.equalsIgnoreCase(uploadmonitoring)) {
			if (phase.equalsIgnoreCase(load)) {
				final String monitoringDirOption = getMonitoringDataDirectoryPath(major, workload+"-load");
				addStatement(event, call("collect_monitoring_data",monitoringDirOption,"ycsb-cassandra-cluster"));
			}
			else if(phase.equalsIgnoreCase(transaction)) {
				final String monitoringDirOption = getMonitoringDataDirectoryPath(major, workload);
				addStatement(event, call("collect_monitoring_data",monitoringDirOption,"ycsb-cassandra-cluster"));
			}
			addStatement(event, call("push_data_to_github"));
		}
	}	@Override
	protected void afterRunExperiment(ClusterActionEvent event)
			throws IOException, InterruptedException {
		// ClusterSpec clusterSpec = event.getClusterSpec();
		// Configuration config = clusterSpec.getConfiguration();
		//
		// final String experimentAction = config
		// .getString(ExperimentParameters.YCSB_EXPERIMENT_ACTION, null);
		// final String workload =
		// config.getString(ExperimentParameters.YCSB_WORKLOAD_FILE, null);
		// final String major = config.getString(MAJOR_VERSION, null);
		// final String prepare =
		// ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_PREPARE
		// .toString();
		// final String load =
		// ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_LOAD.toString();
		// final String run =
		// ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_RUN.toString();
		// final String upload =
		// ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_UPLOAD_DATA
		// .toString();
		//
		// final String monitoringDirOption =
		// getMonitoringDataDirectoryPath(major, workload);
		//
		// if (experimentAction.equalsIgnoreCase(prepare)) {
		// // do nothing
		// }
		// else if (experimentAction.equalsIgnoreCase(load)) {
		// // record end time
		// // experimentEndTime.add(""+System.currentTimeMillis());
		// // final String start =
		// experimentStartTime.get(experimentStartTime.size()-1);
		// // final String end =
		// experimentEndTime.get(experimentStartTime.size()-1);
		// // addStatement(event,
		// call("collect_monitoring_data",monitoringDirOption,"ycsb-cassandra-cluster",start,end));
		// } else if (experimentAction.equalsIgnoreCase(run)) {
		// // record end time
		// // experimentEndTime.add(""+System.currentTimeMillis());
		// // final String start =
		// experimentStartTime.get(experimentStartTime.size()-1);
		// // final String end =
		// experimentEndTime.get(experimentStartTime.size()-1);
		// // addStatement(event,
		// call("collect_monitoring_data",monitoringDirOption,"ycsb-cassandra-cluster",start,end));
		// } else if (experimentAction.equalsIgnoreCase(upload)) {
		// // addStatement(event, call("push_data_to_github"));
		// }

	}

	static List<String> getHosts(Set<Instance> instances) {
		return Lists.transform(Lists.newArrayList(instances),
				new Function<Instance, String>() {
					@Override
					public String apply(Instance instance) {
						try {
							return instance.getPublicHostName();
						} catch (IOException e) {
							throw new IllegalArgumentException(e);
						}
					}
				});
	}

	/**
	 * 
	 * File path to /usr/local/ycsb-0.1.4/workloads/reports/
	 * <code>workload</code>-monitoring.
	 * 
	 * @param major
	 * @param workload
	 * @return
	 */
	private String getMonitoringDataDirectoryPath(String major, String workload) {
		StringBuffer toReturn = new StringBuffer();
		// construct path to ycsb report file for the given workload
		toReturn.append(getYCSBRootPath(major, workload));
		toReturn.append("reports/");
		if (workload != null) {
			// could be for example "performance/workloadb-monitoring/"
			toReturn.append(workload);
			toReturn.append("-monitoring/");
		} else {
			toReturn.append("performance/workloada-monitoring/");
		}
		return toReturn.toString();
	}

	private String getYCSBRootPath(String major, String workload) {
		StringBuffer toReturn = new StringBuffer();
		// construct path to ycsb workloads directory
		toReturn.append("/usr/local/ycsb-");
		toReturn.append(major);
		toReturn.append("/workloads/");
		return toReturn.toString();
	}

}
