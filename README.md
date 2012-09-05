Intro
=====
Whirr-elasticlab is a tool for performing experiments that involve cluster services, such as performance benchmarking of cloud-based database services.
The tool is based on Apache whirr (check out [http://whirr.apache.org] (http://whirr.apache.org) for more info).


Getting Started in 5 Minutes
====

Download and extract the tarball
----
* $ `wget https://github.com/downloads/markusklems/whirr-elasticlab/whirr-0.7.0-elasticlab.tar.gz`
* $ `tar -xvfz whirr-0.7.0-elasticlab.tar.gz whirr-elasticlab`

Set some environment variables
----
Set these variables in the `~/.bashrc` (Ubuntu):
* `export AWS_ACCESS_KEY_ID=XXX`
* `export AWS_SECRET_ACCESS_KEY=YYY`

Then execute `source ~/.bashrc` on your shell.
If you use a different operating system or cloud provider, check Google to figure out how to set the appropriate environment variables.

You can also add your whirr-elasticlab/bin folder to the path, like this `export PATH=$PATH:/path/tp/whirr-elasticlab/bin`export PATH=$PATH:/path/tp/whirr-elasticlab/bin`.

Configure the cluster recipe file
----
The cluster recipe in ` whirr-elasticlab/recipes` has a few properties that you should adjust to your won setup. Here is a sample:

<pre><code># Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Setup a Cassandra Cluster with YCSB and Ganglia
# 

# Read the Configuration Guide for more info:
# http://whirr.apache.org/docs/latest/configuration-guide.html 

# Change the cluster name here
whirr.cluster-name=ycsb-cassandra-cluster

#whirr.hardware-id=m1.xlarge

# Setup your cloud credentials by copying conf/credentials.sample
# to ~/.whirr/credentials and editing as needed

# Change the name of cluster admin user
whirr.cluster-user=${sys:user.name}

# YCSB configs
# You can use my YCSB distribution or the official one: github.com/brianfrankcooper
whirr.ycsb.tarball.url=https://github.com/downloads/markusklems/YCSBX/ycsb-0.1.4.tar.gz
whirr.ycsb.version.major=0.1.4
whirr.ycsb-db=cassandra-10
whirr.ycsb-workload-file=workloads/workloada
whirr.ycsb-experiment-action=prepare

# Enter the Git-Read-Only link here.
# You should change this, unless you want to use my experiment setup.
whirr.ycsb.experiment.repo.git=git://github.com/markusklems/Experiments.git
# S3 bucket for observation data
# You should change this! S3 bucket names must be unique across all S3 users.
whirr.ycsb.observations.s3.bucket=whirr-experiment-bucket

# Cassandra configs
whirr.cassandra.version.major=1.1.2
whirr.cassandra.tarball.url=http://apache.mirror.iphh.net/cassandra/1.1.2/apache-cassandra-1.1.2-bin.tar.gz

# Change the number of machines in the cluster here
whirr.instance-templates=1 ycsb, 3 cassandra+ganglia-monitor, 1 ganglia-metad
whirr.provider=aws-ec2
whirr.identity=${env:AWS_ACCESS_KEY_ID} 
whirr.credential=${env:AWS_SECRET_ACCESS_KEY}
whirr.private-key-file=${sys:user.home}/.ssh/id_rsa
whirr.public-key-file=${sys:user.home}/.ssh/id_rsa.pub

# repair configs
whirr.repair-roles=cassandra ganglia-monitor

# jclouds and ssh connection settings
jclouds.compute.timeout.node-terminated=12000000
jclouds.compute.timeout.node-running=36000000
jclouds.compute.timeout.script-complete=120000000
jclouds.compute.timeout.port-open=12000000
</code></pre>

Example
----
 * $ `whirr launch-cluster --config=recipes/cassandra`
 * $ `whirr run-experiment --config=recipes/cassandra --ycsb-experiment-action=prepare --ycsb-workload-phase=load`
 * $ `whirr run-experiment --config=recipes/cassandra --ycsb-experiment-action=run --ycsb-workload-phase=load`
 * $ `whirr run-experiment --conig=recipes/cassandra  --ycsb-experiment-action=upload --ycsb-workload-phase=load`
 * $ `whirr run-experiment --config=recipes/cassandra --ycsb-experiment-action=prepare --ycsb-workload-phase=transaction`
 * ...
 * $ `whirr destroy-cluster --config=recipes/cassandra`


Set up Whirr-Elasticlab in Eclipse
====
Here are [the instructions](https://cwiki.apache.org/confluence/display/WHIRR/How+To+Contribute) to set up whirr in Eclipse. This is the short setup guide for whirr-elasticlab:

 * ~$ `git clone git://github.com/markusklems/whirr-elasticlab.git`
 * ~$ `cd whirr-elasticlab`
 * Whirr-elasticlab$ `mvn install` (must use Maven3)
 * whirr-elasticlab$ `mvn eclipse:eclipse`

That's it.

 FAQ
====
 * "Maven does not work!" - "Be sure to use Maven3"
