package org.apache.whirr.experiment;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;

/**
 * 
 * 
 * @author markusklems
 *
 */
public class ExperimentPlan {
	
	private List<ExperimentInstance> experiments;
	
	/**
	 * @param arg0
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(ExperimentInstance arg0) {
		return experiments.add(arg0);
	}

	/**
	 * @return
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return experiments.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.List#iterator()
	 */
	public Iterator<ExperimentInstance> iterator() {
		return experiments.iterator();
	}

	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int size() {
		return experiments.size();
	}

//	public void run(Cluster cluster, ClusterSpec clusterSpec) {
//		for(ExperimentPhase experimentPhase : experiments) {
//			RunExperimentPhaseAction action = experimentPhase.getRunExperimentPhaseAction();
//			try {
//				action.execute(clusterSpec, cluster);
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}


	public class ExperimentInstance {
		
		private final RunExperimentAction runExperimentAction;
		private String name;

		public ExperimentInstance(RunExperimentAction runExperimentPhaseAction) {
			this.runExperimentAction = runExperimentPhaseAction;
		}

		public ExperimentInstance(
				RunExperimentAction runExperimentPhaseAction, String name) {
			this.runExperimentAction = runExperimentPhaseAction;
			this.name = name;
		}

		/**
		 * @return the runExperimentPhaseAction
		 */
		public RunExperimentAction getRunExperimentPhaseAction() {
			return runExperimentAction;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

	}

}
