package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.aas.SubmodelRegistry;
import mdt.client.HttpMDTManagerClient;
import mdt.model.MDTManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestSubmodelRegistry {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		HttpMDTManagerClient mdtClient = HttpMDTManagerClient.connect("http://localhost:12985");
		
		SubmodelRegistry registry = mdtClient.getSubmodelRegistry();
		for ( SubmodelDescriptor desc: registry.getAllSubmodelDescriptors() ) {
			System.out.println(desc);
		}
		
		SubmodelDescriptor desc
			= registry.getSubmodelDescriptorById("https://example.com/ids/KR3/sm/InformationModel");
		System.out.println(ser.write(desc));
		
		for ( SubmodelDescriptor aasDesc: registry.getAllSubmodelDescriptorsByIdShort("Data") ) {
			System.out.println(aasDesc);
		}
		
		desc.setIdShort("XXXXXX");
		registry.putSubmodelDescriptorById(desc);
	}
}
