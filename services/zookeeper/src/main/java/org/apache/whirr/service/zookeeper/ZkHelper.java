package org.apache.whirr.service.zookeeper;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.ClusterSpec;
import org.jclouds.scriptbuilder.domain.Statement;

public class ZkHelper extends ZooKeeperClusterActionHandler {
	
	  public List<Statement> getStatements(ClusterSpec clusterSpec) throws IOException {
		  List<Statement> toReturn = new ArrayList<Statement>();
		    Configuration config = getConfiguration(clusterSpec);

		    toReturn.add(call(getInstallFunction(config, "java", "install_openjdk")));
		    toReturn.add(call("install_tarball"));
		    toReturn.add(call("install_service"));

		    String tarurl = config.getString("whirr.zookeeper.tarball.url");
		    toReturn.add(call(getInstallFunction(config),
		      "-u", tarurl)
		    );
		    
		    return toReturn;
		  }
	

}
