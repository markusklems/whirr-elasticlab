package org.apache.whirr.service.ycsb;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.ClusterSpec;
import org.jclouds.scriptbuilder.domain.Statement;

public class YcsbHelper extends YcsbClusterActionHandler {
	
	public List<Statement> getStatements(ClusterSpec clusterSpec)
			throws IOException {
		List<Statement> toReturn = new ArrayList<Statement>();
		Configuration conf = clusterSpec.getConfiguration();
			
		toReturn.add(call(getInstallFunction(conf, "java", "install_openjdk")));
		toReturn.add(call("install_tarball_no_md5"));
		toReturn.add(call("install_service"));
		String tarball = conf.getString(BIN_TARBALL,null);
		String major = conf.getString(MAJOR_VERSION, null);
		if (tarball != null && major != null) {
			toReturn.add(call("install_ycsb", major, tarball));
		} else {
			toReturn.add(call("install_ycsb"));
		}
		
		toReturn.add(call("install_git"));
		String repo = conf.getString(EXPERIMENT_REPO, null);
		// clone the workload repository
		toReturn.add(call("update_workload_repo", repo));


		return toReturn;
	}

}
