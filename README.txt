Apache Whirr Fork for Cluster Experiment Automation
---------------------------------------------------
Please check out the official whirr website to get basic info on whirr: http://whirr.apache.org/


Getting Started (Quick & Dirty)
-----------------------------
* ~ $ git clone git://github.com/markusklems/whirr.git
* ~ $ cd whirr
* whirr$ git branch -a
* whirr$ git checkout -b ycsb remotes/origin/ycsb
* whirr$ mvn install (use Maven3)
* whirr$ whirr launch-cluster --config recipes/ycsb.properties
* whirr$ whirr run-experiment --config recipes/ycsb.properties
* whirr$ whirr destroy-cluster --config recipes/ycsb.properties

The ycsb stats have been uploaded to the github repository.