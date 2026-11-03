package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.data.DataInfo;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.entity.SubmodelElementEntity;

/**
 * 인스턴스의 Data 서브모델을 읽어 {@code DefaultData}로 변환하고, 최상위 엔티티와
 * Operation 파라미터 값을 출력하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestData {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
//		JsonSerializer ser = new JsonSerializer();

		int port = 20307;
		String assetId = "CRF";
		String submodelId = String.format("https://example.com/ids/%s/sm/Data", assetId);

//		int port = 20307;
//		String assetId = "Welder";
//		String submodelId = String.format("https://www.samcheon.com/mdt/%s/sm/Data", assetId);
		
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
			if ( entity instanceof Operation op ) {
				for ( ParameterValue pv: op.getParameterValueList() ) {
					System.out.println(pv.getParameterId() + ": " +  pv.getParameterValue());
				}
			}
		}
		
//		KSX9101Node root = KSX9101Node.builder()
//										.mdtId("KRCW-02ER1A101")
//										.informationModelSubmodel(adaptor)
//										.build();
//		System.out.print("\033[2J\033[1;1H");
//		System.out.print(TextTree.newInstance(opts).render(root));
	}
}
