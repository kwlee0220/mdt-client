package mdt.test;

import java.io.File;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.value.FileValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestPutFileByPath {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/Data");
		String url = String.format("https://localhost:19009/api/v3.0/submodels/%s", id);
		
		String idShortPath = "DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue";
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		SubmodelElement sme = svc.getSubmodelElementByPath(idShortPath);
		
		File file = new File("/home/kwlee/tmp/video0.mp4");
		FileValue aasFile = new FileValue("video0.mp4", "video/mp4");
		long length = svc.putAttachmentByPath(idShortPath, aasFile, file);
		System.out.println(file + ", length=" + length);
		
//		svc.deleteAttachmentByPath(idShortPath);
//		mdtFile = svc.getFileByPath(idShortPath);
	}
}
