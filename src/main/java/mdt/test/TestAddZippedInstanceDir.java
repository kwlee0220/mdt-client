package mdt.test;

import java.io.File;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstance;
import mdt.client.instance.HttpMDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAddZippedInstanceDir {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		String instId = "heater";
		File zipFile = new File("/home/kwlee/tmp/models/heater.zip");
		HttpMDTInstance inst = manager.addZippedInstance(instId, 19090, zipFile);
		System.out.println("instance: aas-id=" + inst.getAasId());
	}
}
