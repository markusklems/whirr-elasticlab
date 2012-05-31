/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.whirr.examples;

import java.util.Set;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.RolePredicates;
import org.apache.whirr.service.cassandra.CassandraClusterActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class shows how you should use Whirr to start a YCSB client and a
 * Cassandra cluster and run performance benchmarks.
 * 
 * (parts are copy & paste from the cassandra and ycsb integration tests)
 * 
 */
public class YCSBwithCassandraClusterExample extends Example {
	
	  private ClusterSpec clusterSpec;
	  private ClusterControllerFactory factory = new ClusterControllerFactory();
	  private ClusterController controller;
	  private Cluster cluster;

	private static final Logger LOG = LoggerFactory
			.getLogger(YCSBwithCassandraClusterExample.class);

	@Override
	public String getName() {
		return "ycsb-cassandra-cluster";
	}

	@Override
	public int main(String[] args) throws Exception {

		/**
		 * Assumption: Cloud credentials are available in the current
		 * environment
		 * 
		 * export AWS_ACCESS_KEY_ID=... export AWS_SECRET_ACCESS_KEY=...
		 * 
		 * You can find the credentials for EC2 by following this steps: 1. Go
		 * to http://aws-portal.amazon.com/gp/aws/developer/account/index.html?
		 * action=access-key 2. Log in, if prompted 3. Find your Access Key ID
		 * and Secret Access Key in the "Access Credentials" section, under the
		 * "Access Keys" tab. You will have to click "Show" to see the text of
		 * your secret access key.
		 * 
		 */

		if (!System.getenv().containsKey("AWS_ACCESS_KEY_ID")) {
			LOG.error("AWS_ACCESS_KEY_ID is undefined in the current environment");
			return -1;
		}
		if (!System.getenv().containsKey("AWS_SECRET_ACCESS_KEY")) {
			LOG.error("AWS_SECRET_ACCESS_KEY is undefined in the current environment");
			return -2;
		}

		/**
		 * Start by loading cluster configuration file and creating a
		 * ClusterSpec object
		 * 
		 * You can find the file in the resources folder.
		 */
		clusterSpec = new ClusterSpec(new PropertiesConfiguration(
				"whirr-ycsb-cassandra-example.properties"));

		/**
		 * Create an instance of the generic cluster controller
		 */
		controller = factory.create(clusterSpec.getServiceName());

	    LOG.info("Starting cluster {}", clusterSpec.getClusterName());
	    Cluster cluster = controller.launchCluster(clusterSpec);
	    
	    waitForCassandra();
		
		// wrap up
		controller.destroyCluster(clusterSpec);
		return 0;
	}
	
	private Cassandra.Client client(Instance instance) throws TException {
	    TTransport trans = new TFramedTransport(new TSocket(
	        instance.getPublicIp(),
	        CassandraClusterActionHandler.CLIENT_PORT));
	    trans.open();
	    TBinaryProtocol protocol = new TBinaryProtocol(trans);
	    return new Cassandra.Client(protocol);
	  }

	  private void waitForCassandra() {
	    LOG.info("Waiting for Cassandra to start");
	    Set<Instance> cassandraInstances = cluster.getInstancesMatching(RolePredicates.role(CassandraClusterActionHandler.CASSANDRA_ROLE));
	    for (Instance instance : cassandraInstances) {
	      int tries = 0;
	      while (tries < 30) {
	        try {
	          Cassandra.Client client = client(instance);
	          client.describe_cluster_name();
	          client.getOutputProtocol().getTransport().close();
	          LOG.info(instance.getPublicIp() + " is up and running");
	          break;

	        } catch (TException e) {
	          try {
	            LOG.warn(instance.getPublicIp() + " not reachable, try #" + tries + ", waiting 1s");
	            Thread.sleep(10000);
	          } catch (InterruptedException e1) {
	            break;
	          }
	          tries += 1;
	        }
	      }
	      if (tries == 10) {
	        LOG.error("Instance " + instance.getPublicIp() + " is still unavailable after 10 retries");
	      }
	    }
	  }

}
