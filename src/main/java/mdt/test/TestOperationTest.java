package mdt.test;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import utils.KeyValue;
import utils.stream.FStream;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.PropertyUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperationTest {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("https://www.samcheon.com/mdt/Test/sm/Simulation");
		String url = String.format("https://localhost:19000/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		List<KeyValue<String,String>> inputs = List.of(
			KeyValue.of("IncAmount", "33"),
			KeyValue.of("SleepTime", "1")
		);
		List<OperationVariable> inputVars
			= FStream.from(inputs)
						.map(kv -> PropertyUtils.STRING(kv.key(), kv.value()))
						.map(prop -> new DefaultOperationVariable.Builder().value(prop).build())
						.cast(OperationVariable.class)
						.toList();
		
		List<KeyValue<String,String>> inoutputs = List.of(KeyValue.of("Data", "10"));
		List<OperationVariable> inoutputVars
			= FStream.from(inoutputs)
						.map(kv -> PropertyUtils.STRING(kv.key(), kv.value()))
						.map(prop -> new DefaultOperationVariable.Builder().value(prop).build())
						.cast(OperationVariable.class)
						.toList();
		
		OperationResult result = svc.runOperation("Operation", inputVars, inoutputVars,
															Duration.ofMinutes(5), Duration.ofSeconds(5));
//		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(5*60*1000);
//		OperationResult result = svc.invokeOperationSync("SimulationOperation", inputVars, List.of(), jtimeout);
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}
