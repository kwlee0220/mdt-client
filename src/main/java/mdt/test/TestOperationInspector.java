package mdt.test;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.PropertyUtils;


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
		
		OperationVariable defect;
		defect = new DefaultOperationVariable.Builder()
												.value(PropertyUtils.STRING("Defect", "0,0,0,0,0,0,0,0,0"))
												.build();
		OperationVariable defectList;
		defectList = new DefaultOperationVariable.Builder()
													.value(PropertyUtils.STRING("DefectList", "0,0,1"))
													.build();
		
		OperationResult result;
		result = aiSvc.runOperation("Operation", List.of(defect), List.of(defectList),
									Duration.ofMinutes(5), Duration.ofSeconds(5));
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());


		defect = new DefaultOperationVariable.Builder()
												.value(PropertyUtils.STRING("Defect", "0,0,0,0,0,0,0,0,0"))
												.build();
		defectList = new DefaultOperationVariable.Builder()
													.value(PropertyUtils.STRING("DefectList", "0,0,1,0,0,0,0,1,1,0"))
													.build();
		result = aiSvc.runOperation("Operation", List.of(defect), List.of(defectList),
									Duration.ofMinutes(5), Duration.ofSeconds(5));
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}
