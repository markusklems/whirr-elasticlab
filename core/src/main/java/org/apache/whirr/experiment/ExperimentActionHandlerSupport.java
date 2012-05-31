package org.apache.whirr.experiment;

import java.io.IOException;

import org.apache.whirr.service.ClusterActionHandlerSupport;

public abstract class ExperimentActionHandlerSupport extends
		ClusterActionHandlerSupport implements ExperimentActionHandler {

	public void beforeExperiment(ExperimentEvent event) throws IOException,
			InterruptedException {
		if (event.getAction().equals(RUN_EXPERIMENT_ACTION)) {
			beforeRunExperiment(event);
		}
	}

	public void afterAction(ExperimentEvent event) throws IOException,
			InterruptedException {
		if (event.getAction().equals(RUN_EXPERIMENT_ACTION)) {
			afterRunExperiment(event);
		}
	}

	protected void afterRunExperiment(ExperimentEvent event)
			throws IOException, InterruptedException {
	}

	protected void beforeRunExperiment(ExperimentEvent event)
			throws IOException, InterruptedException {
	}
}
