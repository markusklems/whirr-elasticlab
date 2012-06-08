Apache Whirr Fork for Cluster Experiment Automation
---------------------------------------------------
Please check out the official whirr website to get basic info on whirr: http://whirr.apache.org/


Getting Started (Quick & Dirty)
-----------------------------
 * ~$ `git clone git://github.com/markusklems/whirr.git`
 * ~$ `cd whirr`
 * whirr$ `git branch -a`
 * whirr$ `git checkout -b ycsb remotes/origin/ycsb`
 * whirr$ `mvn install` (use Maven3)
 * whirr$ `mvn eclipse:eclipse`

Set up the whirr/recipes/ycsb.properties file properly.
You should set the private/public key pair that you have added to your github repository.
Otherwise, the repo update and upload features won't work.

 * whirr$ `whirr launch-cluster --config recipes/ycsb.properties`
 * whirr$ `whirr run-experiment --config recipes/ycsb.properties --ycsb-experiment-action=load`
 * whirr$ `whirr run-experiment --config recipes/ycsb.properties --ycsb-experiment-action=run`
 * whirr$ `whirr run-experiment --config recipes/ycsb.properties --ycsb-experiment-action=upload`
 * whirr$ `whirr destroy-cluster --config recipes/ycsb.properties`
