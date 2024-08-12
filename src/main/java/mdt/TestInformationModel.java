package mdt;

import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.cli.tree.ksx9101.KSX9101Node;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.ksx9101.model.impl.DefaultInformationModel;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestInformationModel {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		String assetId = "내함_성형";
		int port = 10502;
		
		String id = AASUtils.encodeBase64UrlSafe(String.format("https://example.com/ids/%s/sm/InformationModel", assetId));
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port, id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		Submodel submodel = svc.getSubmodel();
//		System.out.println(ser.write(submodel));
		
		DefaultInformationModel handle = new DefaultInformationModel();
		handle.fromAasModel(submodel);
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
		KSX9101Node root = KSX9101Node.builder()
										.mdtId("KRCW-02ER1A101")
										.informationModelSubmodel(handle)
										.build();
		System.out.print("\033[2J\033[1;1H");
		System.out.print(TextTree.newInstance(opts).render(root));
	}
}
