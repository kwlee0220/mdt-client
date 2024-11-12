package mdt.sample.instance;

import java.io.File;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleAddInstance {
	private static final String ENDPOINT = "http://61.98.138.54:10133";
	
	public static final void main(String... args) throws Exception {
		// MDTInstanceManager에 접속함.
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();
		
		// 'Test' 인스턴스 배포
		File modelFile = new File("/home/kwlee/mdt/models/test/model.json");
		File configFile = new File("/home/kwlee/mdt/models/test/config.json");
		manager.addInstance("Test", 19000, null, modelFile, configFile);
		
		// 'surface' 인스턴스 배포
		File modelFile2 = new File("/home/kwlee/mdt/models/surface/model.json");
		File configFile2 = new File("/home/kwlee/mdt/models/surface/config.json");
		manager.addInstance("surface", 19005, null, modelFile2, configFile2);
	}
}
