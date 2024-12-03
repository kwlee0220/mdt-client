package mdt.test;

import java.io.File;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAddZippedInstanceDir {
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		String instId = "heater";
		File zipFile = new File("/home/kwlee/tmp/models/heater.zip");
		HttpMDTInstanceClient inst = manager.addZippedInstance(instId, 19090, zipFile);
		System.out.println("instance: aas-id=" + inst.getAasId());
	}
}
