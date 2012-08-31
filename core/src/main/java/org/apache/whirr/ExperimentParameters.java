package org.apache.whirr;

public class ExperimentParameters {
	// runExperiment command options
	public static final String YCSB_EXPERIMENT_ACTION = "whirr.ycsb-experiment-action";
	public static final String YCSB_DB = "whirr.ycsb-db";
	public static final String YCSB_WORKLOAD_FILE = "whirr.ycsb-workload-file";
	public static final String YCSB_WORKLOAD_PHASE = "whirr.ycsb-workload-phase";

	public static enum EXPERIMENT_ACTION_VALUE {
		EXPERIMENT_PREPARE("prepare"), EXPERIMENT_RUN(
				"run"), EXPERIMENT_STOP_MONITORING(
						"stop-monitoring"), EXPERIMENT_UPLOAD_DATA("upload"), EXPERIMENT_UPLOAD_MONITORING_DATA("upload-monitoring-data");

		private final String value;

		private EXPERIMENT_ACTION_VALUE(final String val) {
			this.value = val;
		}

		public String toString() {
			return value;
		}
	}
	
	public static enum EXPERIMENT_PHASE_VALUE {
		EXPERIMENT_PHASE_LOAD("load"), EXPERIMENT_PHASE_TRANSACTION(
				"transaction");

		private final String value;

		private EXPERIMENT_PHASE_VALUE(final String val) {
			this.value = val;
		}

		public String toString() {
			return value;
		}
	}
}
