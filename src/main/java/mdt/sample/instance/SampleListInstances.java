package mdt.sample.instance;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.instance.MDTInstance;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleListInstances {
	private static final String ENDPOINT = "http://61.98.138.54:10133";
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();
		
		List<HttpMDTInstanceClient> instList = manager.getInstanceAll();
		System.out.println("------------------------------------------");
		for ( MDTInstance inst: instList ) {
			System.out.println("ID: " + inst.getId());
			
			StringBuilder builder = new StringBuilder();
			for ( SubmodelDescriptor smDesc: inst.getSubmodelDescriptorAll() ) {
				builder.append(",").append(smDesc.getIdShort());
			}
			System.out.println("SUB_MODELS: " + builder.toString().substring(1));
			
			System.out.println("STATUS: " + inst.getStatus());
			System.out.println("------------------------------------------");
		}
	}
}
