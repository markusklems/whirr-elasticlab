Apache Whirr Fork for Cluster Experiment Automation
---------------------------------------------------
Please check out the official whirr website to get basic info on whirr: http://whirr.apache.org/


Getting Started (Quick & Dirty)
-----------------------------
~ $ git clone git://github.com/markusklems/whirr.git
~ $ cd whirr
whirr $ git branch ycsb
whirr $ mvn install
whirr $ whirr launch-cluster --config recipes/ycsb.properties
whirr $ whirr run-experiment --config recipes/ycsb.properties
Log into ycsb machine and take a look at the stats that have been collected.
whirr $ whirr destroy-cluster --config recipes/ycsb.properties