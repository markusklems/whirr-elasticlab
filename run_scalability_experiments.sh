#!/bin/bash
echo "launch 3nodes: $(date +%s)" >> scalability_experiment.log
whirr launch-cluster --config=experiments/3nodes
echo "finished launch 3nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/3nodes --ycsb-experiment-action=prepare --ycsb-workload-file=scale/workloada-03nodes
echo "load 3nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/3nodes --ycsb-experiment-action=run --ycsb-workload-phase=load --ycsb-workload-file=scale/workloada-03nodes
whirr run-experiment --config=experiments/3nodes --ycsb-experiment-action=upload --ycsb-workload-phase=load
echo "run 3nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/3nodes --ycsb-experiment-action=run --ycsb-workload-phase=transaction --ycsb-workload-file=scale/workloada-03nodes
whirr run-experiment --config=experiments/3nodes --ycsb-experiment-action=upload --ycsb-workload-phase=transaction
echo "finished experiment 3nodes: $(date +%s)" >> scalability_experiment.log
echo "repair 3nodes to 4nodes: $(date +%s)" >> scalability_experiment.log
whirr repair-cluster --config=experiments/4nodes
echo "finished repair 3nodes to 4nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/4nodes --ycsb-experiment-action=prepare --ycsb-workload-file=scale/workloada-04nodes
echo "load 4nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/4nodes --ycsb-experiment-action=run --ycsb-workload-phase=load --ycsb-workload-file=scale/workloada-04nodes
whirr run-experiment --config=experiments/4nodes --ycsb-experiment-action=upload --ycsb-workload-phase=load
echo "run 4nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/4nodes --ycsb-experiment-action=run --ycsb-workload-phase=transaction --ycsb-workload-file=scale/workloada-04nodes
whirr run-experiment --config=experiments/4nodes --ycsb-experiment-action=upload --ycsb-workload-phase=transaction
echo "finished experiment 4nodes: $(date +%s)" >> scalability_experiment.log
whirr repair-cluster --config=experiments/5nodes
echo "finished repair 4nodes to 5nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/5nodes --ycsb-experiment-action=prepare --ycsb-workload-file=scale/workloada-05nodes
echo "load 5nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/5nodes --ycsb-experiment-action=run --ycsb-workload-phase=load --ycsb-workload-file=scale/workloada-05nodes
whirr run-experiment --config=experiments/5nodes --ycsb-experiment-action=upload --ycsb-workload-phase=load
echo "run 5nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/5nodes --ycsb-experiment-action=run --ycsb-workload-phase=transaction --ycsb-workload-file=scale/workloada-05nodes
whirr run-experiment --config=experiments/5nodes --ycsb-experiment-action=upload --ycsb-workload-phase=transaction
echo "finished experiment 5nodes: $(date +%s)" >> scalability_experiment.log
whirr repair-cluster --config=experiments/6nodes
echo "finished repair 5nodes to 6nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/6nodes --ycsb-experiment-action=prepare --ycsb-workload-file=scale/workloada-06nodes
echo "load 6nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/6nodes --ycsb-experiment-action=run --ycsb-workload-phase=load --ycsb-workload-file=scale/workloada-06nodes
whirr run-experiment --config=experiments/6nodes --ycsb-experiment-action=upload --ycsb-workload-phase=load
echo "run 6nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/6nodes --ycsb-experiment-action=run --ycsb-workload-phase=transaction --ycsb-workload-file=scale/workloada-06nodes
whirr run-experiment --config=experiments/6nodes --ycsb-experiment-action=upload --ycsb-workload-phase=transaction
echo "finished experiment 6nodes: $(date +%s)" >> scalability_experiment.log
whirr repair-cluster --config=experiments/7nodes
echo "finished repair 6nodes to 5nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/7nodes --ycsb-experiment-action=prepare --ycsb-workload-file=scale/workloada-07nodes
echo "load 7nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/7nodes --ycsb-experiment-action=run --ycsb-workload-phase=load --ycsb-workload-file=scale/workloada-07nodes
whirr run-experiment --config=experiments/7nodes --ycsb-experiment-action=upload --ycsb-workload-phase=load
echo "run 7nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/7nodes --ycsb-experiment-action=run --ycsb-workload-phase=transaction --ycsb-workload-file=scale/workloada-07nodes
whirr run-experiment --config=experiments/7nodes --ycsb-experiment-action=upload --ycsb-workload-phase=transaction
echo "finished experiment 7nodes: $(date +%s)" >> scalability_experiment.log
whirr repair-cluster --config=experiments/8nodes
echo "finished repair 8nodes to 8nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/8nodes --ycsb-experiment-action=prepare --ycsb-workload-file=scale/workloada-08nodes
echo "load 8nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/8nodes --ycsb-experiment-action=run --ycsb-workload-phase=load --ycsb-workload-file=scale/workloada-08nodes
whirr run-experiment --config=experiments/8nodes --ycsb-experiment-action=upload --ycsb-workload-phase=load
echo "run 8nodes: $(date +%s)" >> scalability_experiment.log
whirr run-experiment --config=experiments/8nodes --ycsb-experiment-action=run --ycsb-workload-phase=transaction --ycsb-workload-file=scale/workloada-08nodes
whirr run-experiment --config=experiments/8nodes --ycsb-experiment-action=upload --ycsb-workload-phase=transaction
echo "finished experiment 8nodes: $(date +%s)" >> scalability_experiment.log
whirr destroy-cluster --config=experiments/8nodes
