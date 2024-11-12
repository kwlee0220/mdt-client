package mdt.test;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.MDTFile;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestGetFileByPath {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/AI/ThicknessInspection");
		String url = String.format("https://localhost:19009/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
//		DefaultMDTFile file = DefaultMDTFile.from(new File("misc/mdt_client_config.yaml"));
//		svc.putFileByPath("A", file);
//		System.out.println(file);
//		
//		SubmodelElement sme = svc.getSubmodelElementByPath("testFile");
		
		MDTFile mdtFile = svc.getFileByPath("AIInfo.Inputs[0].InputValue");
		System.out.println(mdtFile);

		System.out.println(svc.getFileContentByPath("AIInfo.Inputs[0].InputValue").length);
		
		svc.deleteFileByPath("AIInfo.Inputs[0].InputValue");
		mdtFile = svc.getFileByPath("AIInfo.Inputs[0].InputValue");
		System.out.println(mdtFile);
	}
}
