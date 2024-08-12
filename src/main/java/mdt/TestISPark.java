package mdt;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import utils.func.Tuple;

import mdt.cli.tree.ksx9101.DataSubmodelNode;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.ksx9101.KSX9101SubmodelService;
import mdt.ksx9101.model.Data;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestISPark {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
//		Tuple<String,Integer> ep = Tuple.of("KR3", 11000);
		Tuple<String,Integer> ep = Tuple.of("CRF", 10507);
//		Tuple<String,Integer> ep = Tuple.of("KRCW-01ESUU001", 10102);
//		Tuple<String,Integer> ep = Tuple.of("Innercase", 10702);
		
		String smIdShort = "Data";
		
		String url;
		if ( ep._1.equals("Innercase") ) {
			String id = AASUtils.encodeBase64UrlSafe(String.format("http://www.lg.co.kr/refrigerator/Innercase/Data"));
			url = String.format("https://localhost:%d/api/v3.0/submodels/%s", ep._2, id);
		}
		else {
			String id = AASUtils.encodeBase64UrlSafe(String.format("https://example.com/ids/%s/sm/%s",
																	ep._1, smIdShort));
			url = String.format("https://localhost:%d/api/v3.0/submodels/%s", ep._2, id);
		}
		
		HttpSubmodelServiceClient baseSubmodelService = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		KSX9101SubmodelService ksx9101SubmodelService = new KSX9101SubmodelService(baseSubmodelService);
		
		Submodel submodel = ksx9101SubmodelService.getSubmodel();
		System.out.println(ser.write(submodel));
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
//		InformationModel info = KSX9101SubmodelService.asInformationModelSubmodel(submodel);
		
		Data data = KSX9101SubmodelService.asDataSubmodel(submodel);
		Node root = new DataSubmodelNode(data);
		System.out.print("\033[2J\033[1;1H");
		System.out.print(TextTree.newInstance(opts).render(root));
	}
}
