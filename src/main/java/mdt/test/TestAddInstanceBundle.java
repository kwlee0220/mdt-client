package mdt.test;

import java.io.File;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAddInstanceBundle {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		String instId = "test";
		File instanceDir = new File("/home/kwlee/mdt/models/test");
		HttpMDTInstanceClient inst = manager.addInstance(instId, instanceDir);
		System.out.println("instance: aas-id=" + inst.getAasId());
	}
}
