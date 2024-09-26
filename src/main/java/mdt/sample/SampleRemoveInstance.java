package mdt.sample;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleRemoveInstance {
	private static final String ENDPOINT = "http://localhost:12985";
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();

		if ( manager.existsInstance("KR3") ) {
			manager.removeInstance("KR3");
		}
		assert !manager.existsInstance("KR3");
		
		manager.removeAllInstances();
		assert manager.countInstances() == 0;
	}
}
