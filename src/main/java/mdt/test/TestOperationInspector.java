package mdt.test;

import java.time.Duration;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;

import mdt.client.operation.AASOperationClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperationInspector {
	private static final String SM_ID_PREFIX = "http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/AI";
	public static final void main(String... args) throws Exception {
		String aiId = AASUtils.encodeBase64UrlSafe(SM_ID_PREFIX +"/UpdateDefectList");
		String aiUrl = String.format("https://localhost:19009/api/v3.0/submodels/%s", aiId);
		HttpSubmodelServiceClient aiSvc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(aiUrl);
		
		AASOperationClient opClient = new AASOperationClient(aiSvc, "Operation", Duration.ofSeconds(1));
		opClient.setInputVariableValue("Defect", new StringPropertyValue("0,1,0,0,1,0,1,0,0"));
		opClient.setInputVariableValue("DefectList", new StringPropertyValue("0,0,1"));
		OperationResult result = opClient.run();
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());

		opClient = new AASOperationClient(aiSvc, "Operation", Duration.ofSeconds(1));
		opClient.setInputVariableValue("Defect", new StringPropertyValue("0,0,0,0,0,0,0,0,0"));
		opClient.setInputVariableValue("DefectList", new StringPropertyValue("0,0,1,0,0,0,0,1,1,0"));
		result = opClient.run();
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}