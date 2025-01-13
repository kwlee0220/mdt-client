package mdt.test;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.collect.Lists;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.value.PropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestValueProvider {
	public static final void main(String... args) throws Exception {
		int port = 20001;
		String assetId = "KRCW-01EATT018";
		String submodelId = String.format("https://example.com/ids/KRCW-01EATT018/sm/Data", assetId);
		
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port,
									AASUtils.encodeBase64UrlSafe(submodelId));
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		while ( true ) {
			List<String> values = readRow2(svc);
			System.out.println(values);
			
			update(svc, values, 1);
//			Thread.sleep(1000);
		}
	}
	
	private static void update(HttpSubmodelServiceClient svc, List<String> values, int paramIdx) {
		String pattern = "DataInfo.Equipment.EquipmentParameterValues[%d].ParameterValue";
		String value = values.get(paramIdx);

		float oldValue = value.equals("") ? 0.0f : Float.parseFloat(value);
		float newValue = oldValue + 0.5f;
		
		String path2 = String.format(pattern, paramIdx);
		svc.patchSubmodelElementValueByPath(path2, new PropertyValue("" + newValue));
		
		svc.patchSubmodelElementValueByPath("DataInfo.Equipment.EquipmentParameters[0].ParameterName",
											new PropertyValue("xxx"));
	}
	
	private static List<String> readRow2(HttpSubmodelServiceClient svc) {
		String pattern = "DataInfo.Equipment.EquipmentParameterValues";
		SubmodelElementList sml = (SubmodelElementList)svc.getSubmodelElementByPath(pattern);
		
		List<String> values = Lists.newArrayList();
		for ( int i =0; i < 4; ++i ) {
			String subPath = String.format("[%d].ParameterValue", i);
			SubmodelElement sme = SubmodelUtils.traverse(sml, subPath);
			String value = ((Property)sme).getValue();
			values.add(value);
		}
		
		return values;
	}
}