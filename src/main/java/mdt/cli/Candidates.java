package mdt.cli;

import java.util.ArrayList;
import java.util.List;

import utils.stream.FStream;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Candidates {
	@SuppressWarnings("serial")
	public static class StartableCandidates extends ArrayList<String> {
		public StartableCandidates() {
			super(listMDTInstanceAll("instance.status = 'STOPPED' or instance.status = 'FAILED'"));
		}
	};
	
	@SuppressWarnings("serial")
	public static class RunningInstanceCandidates extends ArrayList<String> {
		public RunningInstanceCandidates() {
			super(listMDTInstanceAll("instance.status = 'RUNNING'"));
		}
	};
	
	private static List<String> listMDTInstanceAll(String filter) {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connectWithDefault();
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		List<HttpMDTInstanceClient> instanceList
					= (filter != null) ? manager.getInstanceAllByFilter(filter) : manager.getInstanceAll();
		return FStream.from(instanceList).map(HttpMDTInstanceClient::getId).toList();
	}
}
