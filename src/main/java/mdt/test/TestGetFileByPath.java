package mdt.test;

import utils.io.TempFile;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;

/**
 * idShort 경로로 지정된 서브모델 File 요소의 첨부 파일을 다운로드하는 기능을 시험하는 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestGetFileByPath {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
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
