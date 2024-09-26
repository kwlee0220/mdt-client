package mdt.test;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMDTInstance2 {
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdtClient = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient client = mdtClient.getInstanceManager();
		
		client.removeInstance("abc");
	}
}
