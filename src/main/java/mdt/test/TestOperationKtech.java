package mdt.test;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.PropertyUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperationKtech {
	private static final String SM_ID_PREFIX = "http://www.koreatech.ac.kr/refrigerator/QualityInspectionEquipment/sm";
	public static final void main(String... args) throws Exception {
		String dataId = AASUtils.encodeBase64UrlSafe(SM_ID_PREFIX +"/Data");
		String dataUrl = String.format("https://localhost:19004/api/v3.0/submodels/%s", dataId);
		HttpSubmodelServiceClient dataSvc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(dataUrl);
		
		String aiId = AASUtils.encodeBase64UrlSafe(SM_ID_PREFIX +"/SurfaceErrorDetection");
		String aiUrl = String.format("https://localhost:19004/api/v3.0/submodels/%s", aiId);
		HttpSubmodelServiceClient aiSvc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(aiUrl);
		
		SubmodelElement fileSme = dataSvc.getSubmodelElementByPath(
//													"DataInfo");
													"DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		fileSme.setIdShort("TestImage");
		OperationVariable opv = new DefaultOperationVariable.Builder().value(fileSme).build();
		OperationVariable error = new DefaultOperationVariable.Builder()
											.value(PropertyUtils.INT("ErrorTypeClass", -1))
											.build();
		
		OperationResult result = aiSvc.runOperation("Operation", List.of(opv), List.of(),
															Duration.ofMinutes(5), Duration.ofSeconds(5));
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}
