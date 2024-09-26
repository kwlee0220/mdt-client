package mdt.sample;

import java.io.File;
import java.util.List;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.instance.MDTInstance;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleListInstances {
	private static final String ENDPOINT = "http://localhost:12985";
	@SuppressWarnings("unused")
	private static final File MODEL_DIR = new File("D:\\Dropbox\\Temp\\fa3st-repository\\ispark\\models");
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();
		
		List<MDTInstance> instList = manager.getAllInstances();
		for ( MDTInstance inst: instList ) {
			System.out.println(inst);
		}
	}
}
