package mdt.test;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import utils.KeyValue;
import utils.stream.FStream;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperation {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/Simulation/ProcessOptimization");
		String url = String.format("https://localhost:14435/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		List<OperationVariable> inputVars
			= FStream.from(List.of(KeyValue.of("HT_ProcessTime", "30"), KeyValue.of("HT_FaultRate", "0.7")))
						.map(kv -> SubmodelUtils.newStringProperty(kv.key(), kv.value()))
						.map(prop -> new DefaultOperationVariable.Builder().value(prop).build())
						.cast(OperationVariable.class)
						.toList();
//		List<SubmodelElement> outputs = svc.runOperationAsync("Operation", inputValues,
//															Duration.ofMinutes(5), Duration.ofSeconds(5));
//		System.out.println(outputs);
		
		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(5*60*1000);
		OperationResult result = svc.invokeOperationSync("Operation2", inputVars, List.of(), jtimeout);
		System.out.println(result.getOutputArguments());
	}
}
