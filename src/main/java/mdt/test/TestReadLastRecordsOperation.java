package mdt.test;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import mdt.client.operation.AASOperationClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.PropertyUtils;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestReadLastRecordsOperation {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("https://www.samcheon.com/mdt/Welder/sm/TimeSeries/WelderAmpereLog");
		String url = String.format("https://192.168.0.7:19042/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		AASOperationClient opClient = new AASOperationClient(svc, "ReadLastRecords", Duration.ofSeconds(1));
//		opClient.setInputVariableValue("Range", new StringPropertyValue("last=15"));
		opClient.setInputVariable("Range", PropertyUtils.STRING("Range", "last=7s"));
//		opClient.setInputVariableValue("Columns", new StringPropertyValue("Time"));
		
		OperationResult result = opClient.run();
		
		List<OperationVariable> outputs = result.getOutputArguments();
		SubmodelElementCollection recordsSmc = (SubmodelElementCollection)outputs.get(0).getValue();
		for ( SubmodelElement output: recordsSmc.getValue()) {
			ElementValue rec = ElementValues.getValue(output);
			System.out.printf("output=%s%n", rec.toDisplayString());
		}
		
//		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(5*60*1000);
//		OperationResult result = svc.invokeOperationSync("SimulationOperation", inputVars, List.of(), jtimeout);
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}
