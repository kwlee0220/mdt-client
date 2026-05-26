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
	private static final String INSTANCE_URL = "https://192.168.0.2:19009/api/v3.0";
	private static final String SUBMODEL_ID = "http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/Data";
	private static final String JPEG_FILE_PATH = "/home/kwlee/mdt/models/innercase/inspector/test_images/Innercase05-5.jpg";
	
	public static final void main(String... args) throws Exception {	
		String id = AASUtils.encodeBase64UrlSafe(SUBMODEL_ID);
		String url = String.format("%s/submodels/%s", INSTANCE_URL, id);
		
		String idShortPath = "DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue";
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		SubmodelElement sme = svc.getSubmodelElementByPath(idShortPath);
		
		File jpegFile = new File(JPEG_FILE_PATH);
		FileValue aasFile = new FileValue(jpegFile.getName(), "image/jpg");
		long length = svc.putAttachmentByPath(idShortPath, aasFile, jpegFile);
		System.out.println(jpegFile + ", length=" + length);
		
//		svc.deleteAttachmentByPath(idShortPath);
//		mdtFile = svc.getFileByPath(idShortPath);
	}
}
