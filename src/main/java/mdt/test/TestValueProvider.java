package mdt.test;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.collect.Lists;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
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
		
		String pattern = "DataInfo.Equipment.EquipmentParameterValues[%d].ParameterValue";
		while ( true ) {
			List<String> values = Lists.newArrayList();
			for ( int i =0; i < 4; ++i ) {
				String path = String.format(pattern, i);
				SubmodelElement sme = svc.getSubmodelElementByPath(path);
				String value = ((Property)sme).getValue();
				values.add(value);
			}
			System.out.println(values);
			
			float oldValue = values.get(1).equals("") ? 0.0f : Float.parseFloat(values.get(1));
			float newValue = oldValue + 0.5f;
			
			String path2 = String.format(pattern, 1);
			SubmodelElement sme = svc.getSubmodelElementByPath(path2);
			svc.patchSubmodelElementValueByPath(path2, new PropertyValue("" + newValue));
			
			Thread.sleep(100);
		}
	}
}
