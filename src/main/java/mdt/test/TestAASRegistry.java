package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;

import mdt.aas.ShellRegistry;
import mdt.client.HttpMDTManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAASRegistry {
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdtClient = HttpMDTManagerClient.connect("http://localhost:12985");
		
		ShellRegistry registry = mdtClient.getAssetAdministrationShellRegistry();
		for ( AssetAdministrationShellDescriptor aasDesc: registry.getAllAssetAdministrationShellDescriptors() ) {
			System.out.println(aasDesc);
		}
		
		AssetAdministrationShellDescriptor desc
			= registry.getAssetAdministrationShellDescriptorById("https://www.samcheon.com/mdt/Test");
		System.out.println("Found Shell: " + desc.getId());
		
		for ( AssetAdministrationShellDescriptor aasDesc:
						registry.getAllAssetAdministrationShellDescriptorsByIdShort("Heater") ) {
			System.out.println("Found Shell: " + aasDesc.getId());
		}
		
		for ( AssetAdministrationShellDescriptor aasDesc:
			registry.getAllAssetAdministrationShellDescriptorByAssetId("QualityInspectionEquipment") ) {
			System.out.println("Found Shell: " + aasDesc.getId());
		}
	}
}
