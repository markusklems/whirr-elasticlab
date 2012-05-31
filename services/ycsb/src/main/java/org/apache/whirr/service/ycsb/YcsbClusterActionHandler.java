package org.apache.whirr.service.ycsb;

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
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
	// public static final int YCSB_PORT = xxx;
	public static final int HTTP_PORT = 80;

	public static final String BIN_TARBALL = "whirr.ycsb.tarball.url";
	public static final String MAJOR_VERSION = "whirr.ycsb.version.major";
	public static final String DB = "whirr.ycsb.db";
	public static final String WORKLOAD_FILE = "whirr.ycsb.workload.file";

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
		String db = config.getString(DB, null);
		String workload = config.getString(WORKLOAD_FILE, null);

		addStatement(event, call("install_tarball"));
		addStatement(event, call("install_service"));
		if (tarball != null && major != null) {
			addStatement(event,
					call("install_ycsb", major, tarball, db, workload));
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
		// Firewall settings
		event.getFirewallManager().addRule(
				Rule.create().destination(role(CASSANDRA_ROLE))
						.ports(HTTP_PORT));

		// Add hosts line to the workload file. The cassandra instances are
		// started and their ips are known by now.
		Cluster cluster = event.getCluster();
		Set<Instance> instances = cluster
				.getInstancesMatching(role(CASSANDRA_ROLE));
		List<String> privateIps = getPrivateIps(instances.iterator());
		String cassandraHosts = Joiner.on(' ').join(privateIps.iterator());

		addStatement(event, call("update_workload_file", cassandraHosts));
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
		addStatement(event, call("load_ycsb"));
		addStatement(event, call("run_ycsb"));
	}
	
	@Override
	protected void afterRunExperiment(ClusterActionEvent event)
			throws IOException, InterruptedException {
		// ... download stats
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
