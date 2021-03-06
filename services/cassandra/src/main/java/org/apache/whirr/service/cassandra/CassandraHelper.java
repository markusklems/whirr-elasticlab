package org.apache.whirr.service.cassandra;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.ClusterSpec;
import org.jclouds.scriptbuilder.domain.Statement;

public class CassandraHelper extends CassandraClusterActionHandler {

	public List<Statement> getStatements(ClusterSpec clusterSpec)
			throws IOException {
		List<Statement> toReturn = new ArrayList<Statement>();
		Configuration conf = clusterSpec.getConfiguration();
		toReturn.add(call(getInstallFunction(conf, "java", "install_openjdk")));
		toReturn.add(call("install_tarball"));
		toReturn.add(call("install_service"));
		//toReturn.add(call("remove_service"));
		String tarball = conf.getString(BIN_TARBALL,null);
		String major = conf.getString(MAJOR_VERSION, null);

		toReturn.add(call("install_cassandra", major, tarball));

		return toReturn;
	}

}
