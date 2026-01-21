package mdt.test;

import java.time.Duration;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;

import mdt.client.operation.AASOperationClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.value.PropertyValue.FloatPropertyValue;
import mdt.model.sm.value.PropertyValue.IntegerPropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperationTest {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://mdt.etri.re.kr/mdt/Test/sm/AddAndSleep");
		String url = String.format("https://localhost:19000/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		AASOperationClient opClient = new AASOperationClient(svc, "Operation", Duration.ofSeconds(1));
		opClient.setInoutputVariableValue("IncAmount", new IntegerPropertyValue(33));
		opClient.setInoutputVariableValue("SleepTime", new FloatPropertyValue(2.3f));
		
		OperationResult result = opClient.run();
		
//		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(5*60*1000);
//		OperationResult result = svc.invokeOperationSync("SimulationOperation", inputVars, List.of(), jtimeout);
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}
