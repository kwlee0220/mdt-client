package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import utils.stream.FStream;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.data.DataInfo;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.data.Equipment;

/**
 * Data 서브모델에서 {@code Equipment} 엔티티를 찾아 파라미터 목록을 출력하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestEquipment {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
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
