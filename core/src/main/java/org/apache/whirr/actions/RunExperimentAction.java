package org.apache.whirr.actions;

import java.util.Set;

import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionHandler;
import org.jclouds.compute.ComputeServiceContext;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;

public class RunExperimentAction extends ScriptBasedClusterAction {

	public RunExperimentAction(
			Function<ClusterSpec, ComputeServiceContext> getCompute,
			LoadingCache<String, ClusterActionHandler> handlerMap) {
		super(getCompute, handlerMap);
	}

	public RunExperimentAction(
			Function<ClusterSpec, ComputeServiceContext> getCompute,
			LoadingCache<String, ClusterActionHandler> handlerMap,
			Set<String> targetRoles, Set<String> targetInstanceIds) {
		super(getCompute, handlerMap, targetRoles, targetInstanceIds);
	}

	@Override
	protected String getAction() {
		return ClusterActionHandler.RUN_EXPERIMENT_ACTION;
	}

}
