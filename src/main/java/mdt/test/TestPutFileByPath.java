package mdt.test;

import java.io.File;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.AASFile;
import mdt.model.sm.DefaultAASFile;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestPutFileByPath {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/AI/ThicknessInspection");
		String url = String.format("https://localhost:19009/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		DefaultAASFile file = DefaultAASFile.from(new File("misc/mdt_client_config.yaml"));
		svc.putFileByPath("testFile", file);
		System.out.println(file);
		
		AASFile mdtFile = svc.getFileByPath("testFile");
		System.out.println(mdtFile);

		System.out.println(svc.getFileContentByPath("testFile").length);
		
		svc.deleteFileByPath("testFile");
		mdtFile = svc.getFileByPath("testFile");
	}
}
