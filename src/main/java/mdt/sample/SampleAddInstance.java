package mdt.sample;

import java.io.File;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleAddInstance {
	private static final String ENDPOINT = "http://localhost:12985";
	private static final File MODEL_DIR = new File("D:\\Dropbox\\Temp\\fa3st-repository\\ispark\\models_postgresql");
	
	static class CustomClass {
		public void somePublicMethod() { }
		public final void notOverridablePublicMethod() { }
	}
	
	@SuppressWarnings("unused")
	public static final void main(String... args) throws Exception {
		// MDTInstanceManager에 접속함.
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();

		HttpMDTInstanceClient kr3 = addInstance(manager, "KR3");
		HttpMDTInstanceClient innercaseProcess = addInstance(manager, "내함_성형");
		HttpMDTInstanceClient A101 = addInstance(manager, "KRCW-02ER1A101");
		HttpMDTInstanceClient A102 = addInstance(manager, "KRCW-02ER1A102");
		HttpMDTInstanceClient A103 = addInstance(manager, "KRCW-02ER1A103");
		HttpMDTInstanceClient A104 = addInstance(manager, "KRCW-02ER1A104");
	}
	
	private static HttpMDTInstanceClient addInstance(HttpMDTInstanceManagerClient manager, String id) {
		File modelFile = new File(MODEL_DIR, String.format("model_%s.json", id));
		File confFile = new File(MODEL_DIR, String.format("conf_%s.json", id));
		
		return manager.addInstance(id, null, null, modelFile, confFile);
	}
}
