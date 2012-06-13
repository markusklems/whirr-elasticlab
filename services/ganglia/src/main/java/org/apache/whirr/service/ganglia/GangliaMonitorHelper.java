package org.apache.whirr.service.ganglia;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.ClusterSpec;
import org.jclouds.scriptbuilder.domain.Statement;

public class GangliaMonitorHelper extends GangliaMonitorClusterActionHandler {

	public List<Statement> getStatements(ClusterSpec clusterSpec)
			throws IOException {
		List<Statement> toReturn = new ArrayList<Statement>();
		Configuration config = getConfiguration(clusterSpec);

		toReturn.add(call(getInstallFunction(config), "-r",
				GANGLIA_MONITOR_ROLE));

		return toReturn;
	}

}
