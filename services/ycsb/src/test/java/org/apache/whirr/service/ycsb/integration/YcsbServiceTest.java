package org.apache.whirr.service.ycsb.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YcsbServiceTest {

	private static final Logger LOG = LoggerFactory.getLogger(YcsbServiceTest.class);
	   
//	  private ClusterSpec clusterSpec;
//	  private ClusterController controller;
//	  private Cluster cluster;
//
//	  @Before
//	  public void setUp() throws Exception {
//	    CompositeConfiguration config = new CompositeConfiguration();
//	    if (System.getProperty("config") != null) {
//	      config.addConfiguration(new PropertiesConfiguration(System.getProperty("config")));
//	    }
//	    config.addConfiguration(new PropertiesConfiguration("whirr-ycsb-test.properties"));
//	    clusterSpec = ClusterSpec.withTemporaryKeys(config);
//	    
//	    controller = new ClusterController();
//	    cluster = controller.launchCluster(clusterSpec);
//	    waitForCassandra();
//	  }
//
//	  private Cassandra.Client client(Instance instance) throws TException {
//	    TTransport trans = new TFramedTransport(new TSocket(
//	        instance.getPublicIp(),
//	        CassandraClusterActionHandler.CLIENT_PORT));
//	    trans.open();
//	    TBinaryProtocol protocol = new TBinaryProtocol(trans);
//	    return new Cassandra.Client(protocol);
//	  }
//
//	  private void waitForCassandra() {
//	    LOG.info("Waiting for Cassandra to start");
//	    for (Instance instance : cluster.getInstances()) {
//	      int tries = 0;
//	      while (tries < 30) {
//	        try {
//	          Cassandra.Client client = client(instance);
//	          client.describe_cluster_name();
//	          client.getOutputProtocol().getTransport().close();
//	          LOG.info(instance.getPublicIp() + " is up and running");
//	          break;
//
//	        } catch (TException e) {
//	          try {
//	            LOG.warn(instance.getPublicIp() + " not reachable, try #" + tries + ", waiting 1s");
//	            Thread.sleep(10000);
//	          } catch (InterruptedException e1) {
//	            break;
//	          }
//	          tries += 1;
//	        }
//	      }
//	      if (tries == 10) {
//	        LOG.error("Instance " + instance.getPublicIp() + " is still unavailable after 10 retries");
//	      }
//	    }
//	  }
//
//	  @Test(timeout = TestConstants.ITEST_TIMEOUT)
//	  public void testInstances() throws Exception {
//	    Set<String> endPoints = Sets.newLinkedHashSet();
//	    for (Instance instance : cluster.getInstances()) {
//	      Cassandra.Client client = client(instance);
//	      Map<String,List<String>> tr = client.describe_schema_versions();
//	      for (List<String> version : tr.values()) {
//	        endPoints.addAll(version);
//	      }
//	      client.getOutputProtocol().getTransport().close();
//	    }
//	    LOG.info("List of endpoints: " + endPoints);
//	    
//	    for (Instance instance : cluster.getInstances()) {
//	      String address = instance.getPrivateAddress().getHostAddress();
//	      assertTrue(address + " not in cluster!", endPoints.remove(address));
//	    }
//	    assertTrue("Unknown node returned: " + endPoints.toString(), endPoints.isEmpty());
//	  }
//	  @After
//	  public void tearDown() throws IOException, InterruptedException {
//	    if (controller != null) {
//	      controller.destroyCluster(clusterSpec);      
//	    }
//	  }
	
}
