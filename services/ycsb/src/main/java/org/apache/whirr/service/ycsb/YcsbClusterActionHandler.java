package org.apache.whirr.service.ycsb;

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class YcsbClusterActionHandler extends ClusterActionHandlerSupport {

	private static final Logger LOG = LoggerFactory
			.getLogger(YcsbClusterActionHandler.class);

	public static final String YCSB_ROLE = "ycsb";
	public static final String CASSANDRA_ROLE = "cassandra";
	public static final String HBASE_MASTER_ROLE = "hbase-master";
	public static final String HBASE_RS_ROLE = "hbase-regionserver";
	public static final String HADOOP_DATANODE_ROLE = "hadoop-datanode";
	public static final String HADOOP_NAMENODE_ROLE = "hadoop-namenode";
	// public static final int YCSB_PORT = xxx;
	public static final int HTTP_PORT = 80;

	public static final String BIN_TARBALL = "whirr.ycsb.tarball.url";
	public static final String MAJOR_VERSION = "whirr.ycsb.version.major";
	public static final String WORKLOAD_REPO_GIT = "whir.ycsb.workload.repo.git";

	// runExperiment command options
	public static final String YCSB_EXPERIMENT_ACTION = "whirr.ycsb-experiment-action";
	public static final String YCSB_DB = "whirr.ycsb-db";
	public static final String YCSB_WORKLOAD_FILE = "whirr.ycsb-workload-file";

	public static enum EXPERIMENT_ACTION_VALUE {
		EXPERIMENT_LOAD("load"), EXPERIMENT_RUN("run"), EXPERIMENT_UPLOAD_DATA(
				"upload");

		private final String value;

		private EXPERIMENT_ACTION_VALUE(final String val) {
			this.value = val;
		}

		public String toString() {
			return value;
		}
	}

	@Override
	public String getRole() {
		return YCSB_ROLE;
	}

	// protected Configuration getConfiguration(ClusterSpec spec)
	// throws IOException {
	// return getConfiguration(spec, "whirr-ycsb-default.properties");
	// }

	/**
	 * Install openjdk and YCSB.
	 */
	@Override
	protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
		ClusterSpec clusterSpec = event.getClusterSpec();
		Configuration config = clusterSpec.getConfiguration();

		addStatement(event,
				call(getInstallFunction(config, "java", "install_openjdk")));
		addStatement(event, call("install_tarball_no_md5"));

		// addStatement(event, call("install_service"));
		// addStatement(event, call("remove_service"));

		String tarball = prepareRemoteFileUrl(event,
				config.getString(BIN_TARBALL, null));
		String major = config.getString(MAJOR_VERSION, null);

		addStatement(event, call("install_tarball"));
		addStatement(event, call("install_service"));
		
		if (tarball != null && major != null) {
			// addStatement(event,
			// call("install_ycsb", major, tarball, db, workload));
			addStatement(event, call("install_ycsb", major, tarball));
		} else {
			addStatement(event, call("install_ycsb"));
		}
	}

	/**
	 * 
	 */
	@Override
	protected void beforeConfigure(ClusterActionEvent event)
			throws IOException, InterruptedException {
		Cluster cluster = event.getCluster();
		Set<Instance> cassandraInstances = cluster
				.getInstancesMatching(role(CASSANDRA_ROLE));
		Set<Instance> hbaseInstances = cluster
		.getInstancesMatching(role(HBASE_MASTER_ROLE));
		Set<Instance> instances = new HashSet<Instance>();
		instances.addAll(cassandraInstances);
		instances.addAll(hbaseInstances);
		
		// Firewall settings	
		if(!cassandraInstances.isEmpty()) {
			event.getFirewallManager().addRule(
					Rule.create().destination(role(CASSANDRA_ROLE))
							.ports(HTTP_PORT));
		}
		if(!hbaseInstances.isEmpty()) {
			event.getFirewallManager().addRule(
					Rule.create().destination(role(HBASE_MASTER_ROLE))
							.ports(HTTP_PORT));
			event.getFirewallManager().addRule(
					Rule.create().destination(role(HBASE_MASTER_ROLE))
							.ports(60000));
			event.getFirewallManager().addRule(
					Rule.create().destination(role(HBASE_MASTER_ROLE))
							.ports(2181));
		}
		
			
		
//		event.getFirewallManager().addRule(
//				Rule.create().destination(role(HADOOP_DATANODE_ROLE))
//						.ports(HTTP_PORT));
//		event.getFirewallManager().addRule(
//				Rule.create().destination(role(HADOOP_NAMENODE_ROLE))
//						.ports(HTTP_PORT));
//		event.getFirewallManager().addRule(
//				Rule.create().destination(role(HBASE_RS_ROLE))
//						.ports(HTTP_PORT));

//		instances.addAll(cluster
//				.getInstancesMatching(role(HBASE_RS_ROLE)));
		
		// Add hosts line to the workload file. The cassandra instances are
		// started and their ips are known by now.
		List<String> privateIps = getPrivateIps(instances.iterator());
		String workloadFileHostsParam = Joiner.on(' ').join(privateIps.iterator());
		addStatement(event, call("install_git"));
		String repo = event.getClusterSpec().getConfiguration()
				.getString(WORKLOAD_REPO_GIT, null);

		// clone the workload repository
		addStatement(event, call("update_workload_repo", repo));
		addStatement(event,
				call("prepare_append_hosts_to_workload_file","/usr/local/ycsb-0.1.4/workloads/"+event.getClusterSpec().getConfiguration()
						.getString(YCSB_WORKLOAD_FILE, null)));
		addStatement(event,
				call("append_hosts_to_workload_file", workloadFileHostsParam));
	}

	private List<String> getPrivateIps(Iterator<Instance> it) {
		List<String> toReturn = new ArrayList<String>();
		while (it.hasNext())
			toReturn.add(it.next().getPrivateIp());
		return toReturn;
	}

	@Override
	protected void beforeRunExperiment(ClusterActionEvent event)
			throws IOException, InterruptedException {
		ClusterSpec clusterSpec = event.getClusterSpec();
		Configuration config = clusterSpec.getConfiguration();

		String experimentAction = config.getString(YCSB_EXPERIMENT_ACTION, null);
		final String db = config.getString(YCSB_DB, null);
		final String workload = config.getString(YCSB_WORKLOAD_FILE, null);
		final String major = config.getString(MAJOR_VERSION, null);
		final String load = EXPERIMENT_ACTION_VALUE.EXPERIMENT_LOAD.toString();
		final String run = EXPERIMENT_ACTION_VALUE.EXPERIMENT_RUN.toString();
		final String upload = EXPERIMENT_ACTION_VALUE.EXPERIMENT_UPLOAD_DATA
				.toString();
		// add database, e.g., cassandra-10
		String dbOption = db == null ? "basic" : db;
		// add path to workload file
		String workloadOption = getWorkloadFilePath(major,workload);
		// print output into this report file
		String reportOption = getReportFilePath(major,workload);

		if (experimentAction.equalsIgnoreCase(load)) {
			addStatement(event, call("execute_ycsb", load, dbOption, workloadOption,reportOption+"-load"));
		} else if (experimentAction.equalsIgnoreCase(run)) {
			addStatement(event, call("execute_ycsb", run, dbOption, workloadOption,reportOption));
		} else if (experimentAction.equalsIgnoreCase(upload)) {
			addStatement(event, call("upload_ycsb_results"));
		}
	}

	private String getReportFilePath(String major, String workload) {
		StringBuffer toReturn = new StringBuffer();
		// construct path to ycsb report file for the given workload
		toReturn.append(getYCSBRootPath(major,workload));
		toReturn.append("reports/");
		if (workload != null) {
			// could be for example "performance/workloadb"
			toReturn.append(workload);
		} else {
			toReturn.append("workloada");
		}
		return toReturn.toString();
	}

	private String getWorkloadFilePath(String major, String workload) {
		StringBuffer toReturn = new StringBuffer();
		// construct path to ycsb workload file
		toReturn.append(getYCSBRootPath(major,workload));
		if (workload != null) {
			// could be for example "performance/workloadb"
			toReturn.append(workload);
		} else {
			toReturn.append("workloada");
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

	@Override
	protected void afterRunExperiment(ClusterActionEvent event)
			throws IOException, InterruptedException {
		// upload ycsb measurements to github (doesnt work -> why?)
	}

	// @Override
	// protected void afterConfigure(ClusterActionEvent event) throws
	// IOException,
	// InterruptedException {
	//
	// // addStatement(event, call("load_ycsb"));
	// }

	// @Override
	// protected void beforeStart(ClusterActionEvent event) throws IOException,
	// InterruptedException {
	// // addStatement(event, call("run_ycsb"));
	// }

}
