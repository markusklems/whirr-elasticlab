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
import org.apache.whirr.ExperimentParameters;
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
	public static final String EXPERIMENT_REPO = "whirr.ycsb.experiment.repo.git";
	public static final String OBSERVATION_DATASTORE = "whirr.ycsb.observations.s3.bucket";
	
	// Cloud credentials
	public static final String ACCESS_KEY = "whirr.identity";
	public static final String SECRET_KEY = "whirr.credential";
	
	// FIXME should be a whirr variables
	public static final String LOCAL_EXPERIMENT_DIR = "/usr/local/experiments";
	public static final String BENCHMARKING_DATA_DIR="/usr/local/benchmarking-data";

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
		String tarball = prepareRemoteFileUrl(event,
				config.getString(BIN_TARBALL, null));
		String major = config.getString(MAJOR_VERSION, null);
		addStatement(event, call("install_tarball"));
		addStatement(event, call("install_service"));

		if (tarball != null && major != null) {
			addStatement(event, call("install_ycsb", major, tarball));
		} else {
			addStatement(event, call("install_ycsb"));
		}
		addStatement(event, call("install_git"));
		
		final String aws_key = config.getString(ACCESS_KEY, null);
		final String aws_secret = config.getString(SECRET_KEY, null);
		addStatement(event, call("install_s3cmd",aws_key,aws_secret));
	}

	/**
	 * 
	 */
	@Override
	protected void beforeConfigure(ClusterActionEvent event)
			throws IOException, InterruptedException {
//		addStatement(event, call("install_git"));
		String repo = event.getClusterSpec().getConfiguration()
				.getString(EXPERIMENT_REPO, null);

		// remove and then clone the workload repository
		addStatement(event, call("clone_experiment_repo", repo,LOCAL_EXPERIMENT_DIR));
	}

	private Set<Instance> getInstancesThatMatchTheRoles(Cluster cluster, ClusterActionEvent event) throws IOException {
		Set<Instance> cassandraInstances = cluster
				.getInstancesMatching(role(CASSANDRA_ROLE));
//		Set<Instance> hbaseInstances = cluster
//				.getInstancesMatching(role(HBASE_MASTER_ROLE));
		Set<Instance> instances = new HashSet<Instance>();
		instances.addAll(cassandraInstances);
//		instances.addAll(hbaseInstances);

		// Firewall settings
		if (!cassandraInstances.isEmpty()) {
			event.getFirewallManager().addRule(
					Rule.create().destination(role(CASSANDRA_ROLE))
							.ports(HTTP_PORT));
//			instances
//					.addAll(cluster.getInstancesMatching(role(CASSANDRA_ROLE)));
		}
//		if (!hbaseInstances.isEmpty()) {
//			event.getFirewallManager().addRule(
//					Rule.create().destination(role(HBASE_MASTER_ROLE))
//							.ports(HTTP_PORT));
//			event.getFirewallManager().addRule(
//					Rule.create().destination(role(HBASE_MASTER_ROLE))
//							.ports(60000));
//			event.getFirewallManager().addRule(
//					Rule.create().destination(role(HBASE_MASTER_ROLE))
//							.ports(2181));
////			instances.addAll(cluster
////					.getInstancesMatching(role(HBASE_MASTER_ROLE)));
//		}
		return instances;
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
		Cluster cluster = event.getCluster();
		Set<Instance> instances = getInstancesThatMatchTheRoles(cluster, event);
		// Add hosts line to the workload file. The cassandra instances are
		// started and their ips are known by now.
		List<String> privateIps = getPrivateIps(instances.iterator());
	    String workloadFileHostsParam = Joiner.on(' ').join(
				privateIps.iterator());

		String experimentAction = config
				.getString(ExperimentParameters.YCSB_EXPERIMENT_ACTION, null);
		final String db = config.getString(ExperimentParameters.YCSB_DB, null);
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
		final String upload = ExperimentParameters.EXPERIMENT_ACTION_VALUE.EXPERIMENT_UPLOAD_DATA
				.toString();
		// add database, e.g., cassandra-10
		String dbOption = db == null ? "basic" : db;
		// add path to workload file
		String workloadOption = getWorkloadFilePath(major, workload);
		// print output into this report file
		String reportOption = getReportFilePath(major, workload);
		
//		String repo = event.getClusterSpec().getConfiguration()
//				.getString(WORKLOAD_REPO_GIT, null);

		if (experimentAction.equalsIgnoreCase(prepare)) {
			// set the local experiment directory path
			String experimentDir = LOCAL_EXPERIMENT_DIR;
			// set the local benchmarking data dir
			String benchmarkDir = BENCHMARKING_DATA_DIR;
			// set the name of the workload file that we want use in the experiment
			String workloadFileName = event.getClusterSpec().getConfiguration()
					.getString(ExperimentParameters.YCSB_WORKLOAD_FILE,null);
			addStatement(
					event,
					call("prepare_ycsb",experimentDir,workloadFileName,benchmarkDir));
			// add/replace the database hosts in the workload file
			addStatement(event,
					call("append_hosts_to_workload_file", workloadFileHostsParam));
		} else if (experimentAction.equalsIgnoreCase(run)) {
			if (phase.equalsIgnoreCase(load)) {
				addStatement(
						event,
						call("execute_ycsb", load, dbOption, workloadOption,
								reportOption+"-load"));
			} else if(phase.equalsIgnoreCase(transaction)) {
				// TODO future work
				// collect gossip messages in a log file and write into monitoring directory
				// every 10 seconds
				// timestamp -> CLUSTER STATUS
				//TODO future work
				// wait 600 seconds
				// move A+B parallel, then move C
				// addStatement("sleep 600; nodetool move nodeA tokenA tptargetA && nodetool move nodeB tokenB tpTargetB; nodetool move nodeC tokenC tpTargetC")
			addStatement(
					event,
					call("execute_ycsb", run, dbOption, workloadOption,
							reportOption));
			}
		} else if (experimentAction.equalsIgnoreCase(upload)) {
			String s3_bucket = config.getString(OBSERVATION_DATASTORE,null);	
			addStatement(event, call("push_ycsb_data_to_s3",s3_bucket,BENCHMARKING_DATA_DIR));
		}
	}

	private String getReportFilePath(String major, String workload) {
		StringBuffer toReturn = new StringBuffer();
		// construct path to ycsb report file for the given workload
		//toReturn.append(getYCSBRootPath(major, workload));
		//toReturn.append("reports/");
		toReturn.append(BENCHMARKING_DATA_DIR);
		toReturn.append("/");
		if (workload != null) {
			// could be for example "performance/workloadb"
			toReturn.append(workload);
		} else {
			toReturn.append("performance/workloada");
		}
		return toReturn.toString();
	}

	private String getWorkloadFilePath(String major, String workload) {
		StringBuffer toReturn = new StringBuffer();
		// construct path to ycsb workload file
//		toReturn.append(getYCSBRootPath(major, workload));
		// Path to the experiment dir.
		String experimentDir = LOCAL_EXPERIMENT_DIR+"/";
		toReturn.append(experimentDir);
		if (workload != null) {
			// could be for example "performance/workloadb"
			toReturn.append(workload);
		} else {
			toReturn.append("performance/workloada");
		}
		return toReturn.toString();
	}

//	private String getYCSBRootPath(String major, String workload) {
//		StringBuffer toReturn = new StringBuffer();
//		// construct path to ycsb workloads directory
//		toReturn.append("/usr/local/ycsb-");
//		toReturn.append(major);
//		toReturn.append("/workloads/");
//		return toReturn.toString();
//	}

	@Override
	protected void afterRunExperiment(ClusterActionEvent event)
			throws IOException, InterruptedException {
		// upload ycsb measurements to github?
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
