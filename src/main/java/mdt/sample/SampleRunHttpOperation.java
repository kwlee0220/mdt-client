package mdt.sample;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import utils.async.AsyncResult;

import mdt.client.SSLUtils;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.operation.HttpOperationClient;
import okhttp3.OkHttpClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleRunHttpOperation {
	private static final String ENDPOINT = "http://localhost:12985/instance-manager";
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTInstanceManagerClient.connect(ENDPOINT);
		
		OkHttpClient http = SSLUtils.newTrustAllOkHttpClientBuilder().build();
		String request = "{ \"submodelEndpoint\": \"https://localhost:10502/api/v3.0/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMv64K07ZWoX-yEse2YlS9zbS9TaW11bGF0aW9uL1Byb2Nlc3NPcHRpbWl6YXRpb24=\" }";
		HttpOperationClient opClient = HttpOperationClient.builder()
														.setHttpClient(http)
														.setEndpoint("http://localhost:12987/simulator")
														.setRequestBodyJson(request)
														.setPollInterval(Duration.ofSeconds(1))
														.setTimeout(Duration.ofSeconds(10))
														.build();
		opClient.start();
		AsyncResult<JsonNode> result = opClient.waitForFinished(5, TimeUnit.SECONDS);
		if ( result.isRunning() ) {
			System.out.println("Cancelling...");
			opClient.cancel(true);
			result = opClient.poll();
		}
		System.out.println(result);
	}
}
