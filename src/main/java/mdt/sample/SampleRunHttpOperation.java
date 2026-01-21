package mdt.sample;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import okhttp3.OkHttpClient;

import utils.async.AsyncResult;
import utils.http.OkHttpClientUtils;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.client.operation.HttpOperationClient;
import mdt.client.operation.OperationRequest;
import mdt.client.operation.OperationResponse;
import mdt.model.instance.MDTInstance;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.MDTParameterReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleRunHttpOperation {
	private static final String ENDPOINT = "http://localhost:12985";
	
	@SuppressWarnings("unused")
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManager manager = HttpMDTManager.connect(ENDPOINT)
																	.getInstanceManager();
		
		MDTInstance instance = manager.getInstance("test");
		MDTParameterReference data = MDTParameterReference.newInstance("test", "Data");
		data.activate(manager);
		SubmodelElement dataSme = data.read();
		
		MDTParameterReference incAmount = MDTParameterReference.newInstance("test", "IncAmount");
		incAmount.activate(manager);
		SubmodelElement incAmountSme = incAmount.read();
		
		MDTParameterReference sleepTime = MDTParameterReference.newInstance("test", "SleepTime");
		sleepTime.activate(manager);
		SubmodelElement sleepTimeSme = sleepTime.read();
//		Property sleepTimeSme = PropertyUtils.INT("SleepTime", 10);
		
		SubmodelElement output = SubmodelUtils.duplicate(dataSme);
		output.setIdShort("Output");
			
		OkHttpClient http = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
		
		OperationRequest req = new OperationRequest();
		req.setOperation("test/Simulation");
		req.setInputArguments(Map.of("Data", dataSme, "IncAmount", incAmountSme, "SleepTime", sleepTimeSme));
		req.setOutputArguments(Map.of("Output", output));
		
		HttpOperationClient opClient = HttpOperationClient.builder()
														.setHttpClient(http)
														.setEndpoint("http://localhost:12987")
														.setRequestBody(req)
														.setPollInterval(Duration.ofSeconds(1))
														.setTimeout(Duration.ofSeconds(10))
														.build();
		opClient.start();
		AsyncResult<OperationResponse> result = opClient.waitForFinished(5, TimeUnit.SECONDS);
		if ( result.isRunning() ) {
			System.out.println("Cancelling...");
			opClient.cancel(true);
			result = opClient.poll();
		}
		System.out.println(result);
	}
}
