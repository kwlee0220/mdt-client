package mdt.test;

import java.io.File;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAddInstanceBundle {
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		String instId = "test";
		File instanceDir = new File("/home/kwlee/mdt/models/test");
		HttpMDTInstanceClient inst = manager.addInstance(instId, 19090, instanceDir);
		System.out.println("instance: aas-id=" + inst.getAasId());
	}
}
