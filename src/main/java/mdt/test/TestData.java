package mdt.test;

import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.data.DataInfo;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.entity.SubmodelElementEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestData {
	public static final void main(String... args) throws Exception {
//		JsonSerializer ser = new JsonSerializer();

//		int port = 10117;
//		String assetId = "KRCW-02ER1A101";
//		String submodelId = String.format("https://example.com/ids/%s/sm/Data", assetId);

		int port = 19001;
		String assetId = "Welder";
		String submodelId = String.format("https://www.samcheon.com/mdt/%s/sm/Data", assetId);
		
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port,
									AASUtils.encodeBase64UrlSafe(submodelId));
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		Submodel submodel = svc.getSubmodel();
		
		DefaultData data = DefaultData.from(submodel);
		System.out.println(data);

		DataInfo info = data.getDataInfo();
		System.out.println(info);
		
		for ( SubmodelElementEntity entity: info.getSubmodelElementEntityAll() ) {
			System.out.println("Top-level Entity: " + entity);
		}
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
//		KSX9101Node root = KSX9101Node.builder()
//										.mdtId("KRCW-02ER1A101")
//										.informationModelSubmodel(adaptor)
//										.build();
//		System.out.print("\033[2J\033[1;1H");
//		System.out.print(TextTree.newInstance(opts).render(root));
	}
}
