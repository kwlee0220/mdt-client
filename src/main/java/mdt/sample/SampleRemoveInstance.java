package mdt.sample;

import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleRemoveInstance {
	private static final String ENDPOINT = "http://localhost:12985/instance-manager";
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTInstanceManagerClient.connect(ENDPOINT);

		if ( manager.existsInstance("KR3") ) {
			manager.removeInstance("KR3");
		}
		assert !manager.existsInstance("KR3");
		
		manager.removeAllInstances();
		assert manager.countInstances() == 0;
	}
}
