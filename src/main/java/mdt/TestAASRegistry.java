package mdt;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.registry.AASRegistry;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAASRegistry {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		HttpMDTInstanceManagerClient mdtClient
							= HttpMDTInstanceManagerClient.connect("http://localhost:12985/instance-manager");
		
		AASRegistry registry = mdtClient.getAssetAdministrationShellRegistry();
		for ( AssetAdministrationShellDescriptor aasDesc: registry.getAllAssetAdministrationShellDescriptors() ) {
			System.out.println(aasDesc);
		}
		
		AssetAdministrationShellDescriptor desc
			= registry.getAssetAdministrationShellDescriptorById("https://example.com/ids/aas/KR3");
		System.out.println(ser.write(desc));
		
		for ( AssetAdministrationShellDescriptor aasDesc:
						registry.getAllAssetAdministrationShellDescriptorsByIdShort("KRCW-01ECEM003") ) {
			System.out.println(aasDesc);
		}
	}
}
