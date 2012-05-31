package org.apache.whirr.cli.command;

import java.io.IOException;

import joptsimple.OptionSet;

import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.experiment.ExperimentPlan;
import org.apache.whirr.state.ClusterStateStoreFactory;

public class RunExperimentCommand extends ExperimentRoleLifecycleCommand {

	public RunExperimentCommand() throws IOException {
		this(new ClusterControllerFactory());
	}

	public RunExperimentCommand(ClusterControllerFactory factory) {
		this(factory, new ClusterStateStoreFactory());
	}

	public RunExperimentCommand(ClusterControllerFactory factory,
			ClusterStateStoreFactory stateStoreFactory) {
		super("run-experiment", "Run cluster experiment.", factory,
				stateStoreFactory);
	}

	@Override
	public int runExperimentLifecycleStep(ExperimentPlan experimentPlan,
			ClusterSpec clusterSpec, ClusterController controller,
			OptionSet optionSet) throws IOException, InterruptedException {
		controller.runExperiment(experimentPlan, clusterSpec, getCluster(clusterSpec, controller),
				getTargetRolesOrEmpty(optionSet),
				getTargetInstanceIdsOrEmpty(optionSet));
		return 0;
	}

}
