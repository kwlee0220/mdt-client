package mdt.test;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import utils.stream.FStream;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.ksx9101.KSX9101SubmodelService;
import mdt.ksx9101.model.Equipment;
import mdt.ksx9101.model.Parameter;
import mdt.ksx9101.model.ParameterValue;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestEquipment {
	public static final void main(String... args) throws Exception {
		JsonSerializer ser = new JsonSerializer();
		
		String equipId = "KRCW-02ER1A101";
		int port = 10117;
		
		String id = AASUtils.encodeBase64UrlSafe(String.format("https://example.com/ids/%s/sm/Data", equipId));
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port, id);
		
		HttpSubmodelServiceClient baseSubmodelService = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		KSX9101SubmodelService ksx9101SubmodelService = new KSX9101SubmodelService(baseSubmodelService);
		
		Submodel submodel = ksx9101SubmodelService.getSubmodel();
		System.out.println(ser.write(submodel));
		
		Equipment equip = (Equipment)ksx9101SubmodelService.getEntity(Equipment.class);
		String paramNameCsv = FStream.from(equip.getParameters())
									.map(p -> String.format("%s(%s)", p.getParameterId(), p.getParameterType()))
									.join(", ");
		System.out.printf("%s: %s%n", equip, paramNameCsv);
		
		Parameter param = equip.getParameter("Mean").get();
		ParameterValue value = equip.getParameterValue("Mean").get();
		
//		System.out.println("" + param + ": " + param.parseParameterValue(value));
//	
//		float fvalue = (value.getParameterValue() != null) ? (float)param.parseParameterValue(value) : 0.0f;
//		value = param.toParameterValue(fvalue + 1);
//		ksx9101SubmodelService.updateParameterValue(value);
//		
//		value = ksx9101SubmodelService.getParameterValue(AASModelEntityType.Equipment, "Mean");
//		System.out.println("" + param + ": " + param.parseParameterValue(value));
	}
}
