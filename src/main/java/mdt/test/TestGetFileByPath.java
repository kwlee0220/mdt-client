package mdt.test;

import utils.io.TempFile;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestGetFileByPath {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/Data");
		String url = String.format("https://localhost:19009/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
//		DefaultMDTFile file = DefaultMDTFile.from(new File("misc/mdt_client_config.yaml"));
//		svc.putFileByPath("A", file);
//		System.out.println(file);
//		
//		SubmodelElement sme = svc.getSubmodelElementByPath("testFile");

		String idShortPath = "DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue";
		try ( TempFile tempFile = svc.getAASFileByPath(idShortPath) ) {
			System.out.println("Downloaded file: " + tempFile.getFile() + ", size=" + tempFile.getFile().length());
		}
	}
}
