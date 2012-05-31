package org.apache.whirr.cli.command;

import java.io.IOException;

import joptsimple.OptionSet;

import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.experiment.ExperimentPlan;
import org.apache.whirr.state.ClusterStateStoreFactory;

public abstract class ExperimentRoleLifecycleCommand extends RoleLifecycleCommand {

	public ExperimentRoleLifecycleCommand(String name, String description,
			ClusterControllerFactory factory,
			ClusterStateStoreFactory stateStoreFactory) {
		super(name, description, factory, stateStoreFactory);
	}

	public abstract int runExperimentLifecycleStep(ExperimentPlan exmperimentPlan, ClusterSpec clusterSpec,
			ClusterController controller, OptionSet optionSet) throws IOException, InterruptedException;

	@Override
	public int runLifecycleStep(ClusterSpec clusterSpec,
			ClusterController controller, OptionSet optionSet)
			throws IOException, InterruptedException {
		return runExperimentLifecycleStep(new ExperimentPlan(), clusterSpec,
				controller, optionSet);
	}

	
}
