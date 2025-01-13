package mdt.test;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.DefaultMDTInstanceInfo;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMDTInstanceInfo {
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		HttpMDTInstanceClient instance = manager.getInstance("inspector");
		DefaultMDTInstanceInfo info = DefaultMDTInstanceInfo.builder(instance).build();
		
		String jsonStr = MDTModelSerDe.toJsonString(info);
		System.out.println(jsonStr);
	}
}
