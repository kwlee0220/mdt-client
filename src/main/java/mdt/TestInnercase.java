package mdt;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.cli.tree.ksx9101.SimulationSubmodelNode;
import mdt.client.resource.ExtendedSubmodelService;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.ksx9101.simulation.DefaultSimulation;
import mdt.ksx9101.simulation.Simulation;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestInnercase {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		int port = 10702;
		
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/Simulation/ProcessOptimization");
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port, id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		ExtendedSubmodelService xsvc = ExtendedSubmodelService.from(svc);
		xsvc.setPropertyValueByPath(Simulation.IDSHORT_PATH_ENDPOINT, "XXXXX");
		
		Submodel submodel = svc.getSubmodel();
//		System.out.println(ser.write(submodel));
		
		DefaultSimulation adaptor = new DefaultSimulation();
		adaptor.fromAasModel(submodel);
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
		Node root = new SimulationSubmodelNode(adaptor);
		System.out.print("\033[2J\033[1;1H");
		System.out.print(TextTree.newInstance(opts).render(root));
		
//		System.out.println("SimulatorEndpoint:--------------------------");
//		SubmodelElement sme = ksx9101SubmodelService.getSubmodelElementByPath("SimulationInfo.SimulationTool.SimulatorEndpoint");
//		System.out.println(ser.write(sme));
//
//		System.out.println("Outputs[0]:--------------------------");
//		SubmodelElement outputs
//			= ksx9101SubmodelService.getSubmodelElementByPath("SimulationInfo.Outputs[0].OutputValue");
//		System.out.println(ser.write(outputs));
	}
}
