package mdt.sample;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import utils.async.AsyncResult;
import utils.http.OkHttpClientUtils;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.operation.HttpOperationClient;
import mdt.client.operation.OperationRequestBody;
import mdt.task.Parameter;
import okhttp3.OkHttpClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleRunHttpOperation {
	private static final String ENDPOINT = "http://localhost:12985";
	
	@SuppressWarnings("unused")
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTManagerClient.connect(ENDPOINT)
																	.getInstanceManager();
		
		OkHttpClient http = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
		
		OperationRequestBody req = new OperationRequestBody();
		req.setSubmodelEndpoint("https://localhost:10502/api/v3.0/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMv64K07ZWoX-yEse2YlS9zbS9TaW11bGF0aW9uL1Byb2Nlc3NPcHRpbWl6YXRpb24=");
		
		HttpOperationClient opClient = HttpOperationClient.builder()
														.setHttpClient(http)
														.setEndpoint("http://localhost:12987/simulator")
														.setRequestBody(req)
														.setPollInterval(Duration.ofSeconds(1))
														.setTimeout(Duration.ofSeconds(10))
														.build();
		opClient.start();
		AsyncResult<List<Parameter>> result = opClient.waitForFinished(5, TimeUnit.SECONDS);
		if ( result.isRunning() ) {
			System.out.println("Cancelling...");
			opClient.cancel(true);
			result = opClient.poll();
		}
		System.out.println(result);
	}
}
