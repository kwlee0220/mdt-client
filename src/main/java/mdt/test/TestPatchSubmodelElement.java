package mdt.test;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.value.PropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestPatchSubmodelElement {
	public static final void main(String... args) throws Exception {
		int port = 19009;
		String assetId = "inspector";
		String submodelId = "http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/AI/ThicknessInspection";
		
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port,
									AASUtils.encodeBase64UrlSafe(submodelId));
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		svc.patchSubmodelElementValueByPath("AIInfo.Model.LastExecutionTime", new PropertyValue("PT3S"));
	}
}
