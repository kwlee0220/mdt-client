package mdt.sample.instance;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleRemoveInstance {
	private static final String ENDPOINT = "http://61.98.138.54:10133";
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();
		
		System.out.printf("# of instances: %s%n", manager.countInstances());
		System.out.println("MDTInstance Test: exists=" + manager.existsInstance("Test"));
		if ( manager.existsInstance("Test") ) {
			manager.removeInstance("Test");
		}
		System.out.println("MDTInstance Test: exists=" + manager.existsInstance("Test"));
		
		System.out.printf("# of instances: %s%n", manager.countInstances());
	}
}