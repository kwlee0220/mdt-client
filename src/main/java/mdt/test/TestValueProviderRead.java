package mdt.test;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.collect.Lists;

import utils.StopWatch;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.SubmodelUtils;

/**
 * Data 서브모델의 파라미터 값을 반복 조회하여 평균 읽기 시간을 측정하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestValueProviderRead {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		int port = 20001;
		String assetId = "KRCW-01EATT018";
		String submodelId = String.format("https://example.com/ids/KRCW-01EATT018/sm/Data", assetId);
		
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port,
									AASUtils.encodeBase64UrlSafe(submodelId));
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		for ( int i =0; i < 10; ++i ) {
			readField(svc);
		}

		StopWatch watch = StopWatch.start();
		for ( int i =1; i <= 500; ++i ) {
			List<String> row = readWholeRow(svc);
			System.out.println(row);
			
			if ( i % 50 == 0 ) {
				System.out.printf("%03d: %ss%n", i, watch.stopAndGetElpasedTimeString());
			}
		}
		watch.stop();
//		System.out.println(watch.stopAndGetElpasedTimeString());
		long millis = watch.getElapsedInMillis() / 500;
		System.out.println(millis);
	}
	
	@SuppressWarnings("unused")
	private static List<String> readAllFields(HttpSubmodelServiceClient svc) {
		String pattern = "DataInfo.Equipment.EquipmentParameterValues[%d].ParameterValue";
		
		List<String> values = Lists.newArrayList();
		for ( int i =0; i < 4; ++i ) {
			String path = String.format(pattern, i);
			SubmodelElement sme = svc.getSubmodelElementByPath(path);
			String value = ((Property)sme).getValue();
			values.add(value);
		}
		
		return values;
	}
	
	private static List<String> readWholeRow(HttpSubmodelServiceClient svc) {
		String pattern = "DataInfo.Equipment.EquipmentParameterValues";
		SubmodelElementList sml = (SubmodelElementList)svc.getSubmodelElementByPath(pattern);
		
		List<String> values = Lists.newArrayList();
		for ( int i =0; i < sml.getValue().size(); ++i ) {
			String subPath = String.format("[%d].ParameterValue", i);
			Property prop = SubmodelUtils.traverse(sml, subPath, Property.class);
			String value = prop.getValue();
			values.add(value);
		}
		
		return values;
	}
	
	private static List<String> readField(HttpSubmodelServiceClient svc) {
		String path = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		
		SubmodelElement sme = svc.getSubmodelElementByPath(path);
		String value = ((Property)sme).getValue();
		
		return List.of(value);
	}
	
	@SuppressWarnings("unused")
	private static List<String> readRowMemory(HttpSubmodelServiceClient svc) {
		String pattern = "DataInfo.Equipment.EquipmentParameters";
		SubmodelElementList sml = (SubmodelElementList)svc.getSubmodelElementByPath(pattern);
		
		List<String> values = Lists.newArrayList();
		for ( int i =0; i < 4; ++i ) {
			String subPath = String.format("[%d].ParameterID", i);
			Property prop = SubmodelUtils.traverse(sml, subPath, Property.class);
			String value = prop.getValue();
			values.add(value);
		}
		
		return values;
	}
}
