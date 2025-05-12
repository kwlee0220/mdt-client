package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestSimulation {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		String mdtId = "내함_성형";
		int port = 10502;
		
		String id = AASUtils.encodeBase64UrlSafe(String.format("https://example.com/ids/%s/sm/Simulation/ProcessOptimization", mdtId));
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port, id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
//		System.out.println(ser.write(submodel));
		
		SubmodelElement sme;
		sme = svc.getSubmodelElementByPath("SimulationInfo.Inputs[1]");
		System.out.println(ser.write(sme));
		
//		DefaultInformationModel adaptor = new DefaultInformationModel();
//		adaptor.fromAasModel(submodel);
//		
//		TreeOptions opts = new TreeOptions();
//		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
//		opts.setMaxDepth(5);
//		
//		KSX9101Node root = KSX9101Node.builder()
//										.mdtId("KRCW-02ER1A101")
//										.informationModelSubmodel(adaptor)
//										.build();
//		System.out.print("\033[2J\033[1;1H");
//		System.out.print(TextTree.newInstance(opts).render(root));
	}
}
