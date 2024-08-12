package mdt;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.ksx9101.KSX9101SubmodelService;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestLine {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		String id = AASUtils.encodeBase64UrlSafe("https://example.com/ids/KR3/sm/Data");
		String url = String.format("https://localhost:11000/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient baseSubmodelService = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		KSX9101SubmodelService ksx9101SubmodelService = new KSX9101SubmodelService(baseSubmodelService);
		
		Submodel submodel = ksx9101SubmodelService.getSubmodel();
		System.out.println(ser.write(submodel));
		
//		Line line = (Line)ksx9101SubmodelService.getEntity(AASModelEntityType.Line);
//		System.out.println(line);
		
//		Parameter param = ksx9101SubmodelService.getParameter(EntityType.Equipment, "Mean");
//		System.out.println(param);
//		
//		ParameterValue value = ksx9101SubmodelService.getParameterValue(EntityType.Equipment, "Mean");
//		System.out.println(value);
//		
//		ksx9101SubmodelService.updateParameterValue(EntityType.Equipment, "Mean", "3.1");
//		System.out.println(ksx9101SubmodelService.getParameterValue(EntityType.Equipment, "Mean"));
	}
}
