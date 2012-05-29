package org.apache.whirr.service.ycsb;

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;


public class YcsbClusterActionHandler extends ClusterActionHandlerSupport {

	  private static final Logger LOG =
			    LoggerFactory.getLogger(YcsbClusterActionHandler.class);
			    
			  public static final String YCSB_ROLE = "ycsb-client";
			  //public static final int YCSB_PORT = xxx;
			  public static final int HTTP_PORT = 80;
			  
			  public static final String BIN_TARBALL = "whirr.ycsb.tarball.url";
			  public static final String MAJOR_VERSION = "whirr.ycsb.version.major";
			  public static final String DB = "whirr.ycsb.db";
			  public static final String WORKLOAD_FILE = "whirr.ycsb.workload.file";			  			  
			  
			  @Override
			  public String getRole() {
			    return YCSB_ROLE;
			  }

			  @Override
			  protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
			    ClusterSpec clusterSpec = event.getClusterSpec();
			    Configuration conf = clusterSpec.getConfiguration();
			    // ...
			  }

			  @Override
			  protected void beforeConfigure(final ClusterActionEvent event)
			    throws IOException, InterruptedException {
			    Cluster cluster = event.getCluster();
			    // ...
			  }
			  
//			  protected Configuration getConfiguration(ClusterSpec spec)
//			    throws IOException {
//			    return getConfiguration(spec, "whirr-ycsb-default.properties");
//			  }
//
//			  /**
//			   * Install openjdk and YCSB.
//			   */
//			  @Override
//			  protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
//			    ClusterSpec clusterSpec = event.getClusterSpec();
//			    Configuration config = getConfiguration(clusterSpec);
//
//			    addStatement(event, call(getInstallFunction(config, "java", "install_openjdk")));
//			    addStatement(event, call("install_tarball"));
//
//			    String tarball = prepareRemoteFileUrl(event, config.getString(BIN_TARBALL, null));
//			    String major = config.getString(MAJOR_VERSION, null);
//
//			    if (tarball != null && major != null) {
//			      addStatement(event, call("install_ycsb", major, tarball));
//			    } else {
//			      addStatement(event, call("install_ycsb"));
//			    }
//			  }
//
//			  @Override
//			  protected void beforeConfigure(ClusterActionEvent event) throws IOException, InterruptedException {
//			    event.getFirewallManager().addRule(
//			        Rule.create().destination(role(YCSB_ROLE)).ports(HTTP_PORT)
//			    );
//			  }
//			  
//			  static List<String> getHosts(Set<Instance> instances) {
//			      return Lists.transform(Lists.newArrayList(instances),
//			          new Function<Instance, String>() {
//			        @Override
//			        public String apply(Instance instance) {
//			          try {
//			            return instance.getPublicHostName();
//			          } catch (IOException e) {
//			            throw new IllegalArgumentException(e);
//			          }
//			        }
//			      });
//			    }

}
