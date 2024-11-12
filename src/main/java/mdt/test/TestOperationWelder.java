package mdt.test;

import java.time.Duration;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperationWelder {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("https://www.samcheon.com/mdt/Welder/sm/Simulation/ProductivityPrediction");
		String url = String.format("https://localhost:19001/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		List<String> inVarNames = List.of("AvgDefectRate", "DefectVolume", "AvgProcessingTime",
											"QuantityProduced", "AvgWaitingTime");
		List<OperationVariable> inputVars = Lists.newArrayList();
		for ( int i =0; i < inVarNames.size(); ++i ) {
			String path = String.format("SimulationInfo.Inputs[%d].InputValue", i);
			SubmodelElement param = svc.getSubmodelElementByPath(path);
			param.setIdShort(inVarNames.get(i));
			inputVars.add(new DefaultOperationVariable.Builder().value(param).build());
		}
		
//		List<KeyValue<String,String>> inputs = List.of(
//			KeyValue.of("AvgDefectRate", "0"), KeyValue.of("DefectVolume", "0"),
//			KeyValue.of("AvgProcessingTime", "8"), KeyValue.of("QuantityProduced", "2"),
//			KeyValue.of("AvgWaitingTime", "7")
//		);
//		List<OperationVariable> inputVars
//			= FStream.from(inputs)
//						.map(kv -> SubmodelUtils.newStringProperty(kv.key(), kv.value()))
//						.map(prop -> new DefaultOperationVariable.Builder().value(prop).build())
//						.cast(OperationVariable.class)
//						.toList();
		
		OperationResult result = svc.runOperation("SimulationOperation", inputVars, List.of(),
															Duration.ofMinutes(5), Duration.ofSeconds(5));
//		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(5*60*1000);
//		OperationResult result = svc.invokeOperationSync("SimulationOperation", inputVars, List.of(), jtimeout);
		System.out.println(result.getOutputArguments());
	}
}
