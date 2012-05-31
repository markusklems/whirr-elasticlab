package org.apache.whirr.experiment;

import org.apache.velocity.app.VelocityEngine;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.InstanceTemplate;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.FirewallManager;
import org.jclouds.compute.ComputeServiceContext;

import com.google.common.base.Function;

public class ExperimentEvent extends ClusterActionEvent {

	public ExperimentEvent(String action, ClusterSpec clusterSpec,
			InstanceTemplate instanceTemplate, Cluster cluster,
			Function<ClusterSpec, ComputeServiceContext> getCompute,
			FirewallManager firewallManager, VelocityEngine velocityEngine) {
		super(action, clusterSpec, instanceTemplate, cluster, getCompute,
				firewallManager, velocityEngine);
		// TODO Auto-generated constructor stub
	}

}
