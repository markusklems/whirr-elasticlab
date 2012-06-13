package org.apache.whirr.service.hbase;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.ClusterSpec;
import org.jclouds.scriptbuilder.domain.Statement;

public class HBaseRegionServerHelper extends HBaseRegionServerClusterActionHandler {

	public List<Statement> getStatements(ClusterSpec clusterSpec)
			throws IOException {
		List<Statement> toReturn = new ArrayList<Statement>();
		Configuration conf = clusterSpec.getConfiguration();
		toReturn.add(call(getInstallFunction(conf, "java", "install_openjdk")));
		toReturn.add(call("install_tarball"));
		toReturn.add(call("install_service"));
		String tarurl = getConfiguration(clusterSpec).getString(HBaseConstants.KEY_TARBALL_URL);

		toReturn.add(call(
	  	      getInstallFunction(getConfiguration(clusterSpec)),
		      HBaseConstants.PARAM_TARBALL_URL, tarurl)
		    );

		return toReturn;
	}
	
}
