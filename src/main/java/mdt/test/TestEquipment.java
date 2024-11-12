package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import utils.stream.FStream;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.data.DataInfo;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.data.Equipment;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestEquipment {
	public static final void main(String... args) throws Exception {
//		JsonSerializer ser = new JsonSerializer();
		
//		String assetId = "KRCW-02ER1A101";
//		int port = 10117;
//		String submodelId = AASUtils.encodeBase64UrlSafe(String.format("https://example.com/ids/%s/sm/Data", assetId));

		int port = 19001;
		String assetId = "Welder";
		String submodelId = String.format("https://www.samcheon.com/mdt/%s/sm/Data", assetId);
		
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port,
									AASUtils.encodeBase64UrlSafe(submodelId));
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		Submodel submodel = svc.getSubmodel();
		
		DefaultData data = DefaultData.from(submodel);
		System.out.println(data);
		
		DataInfo dataInfo = data.getDataInfo();
		Equipment equip = dataInfo.getFirstSubmodelElementEntityByClass(Equipment.class);
		System.out.println(equip);
		
		String paramNameCsv = FStream.from(equip.getParameterList())
									.map(p -> String.format("%s(%s)", p.getParameterId(), p.getParameterType()))
									.join(", ");
		System.out.printf("%s: %s%n", svc.getSubmodel().getIdShort(), paramNameCsv);
	}
}
