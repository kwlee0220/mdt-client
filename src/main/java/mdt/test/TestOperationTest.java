package mdt.test;

import java.time.Duration;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;

import mdt.client.operation.AASOperationClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.PropertyUtils;

/**
 * Test 인스턴스의 {@code AddAndSleep} AAS 연산을 {@code AASOperationClient}로 호출하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperationTest {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://mdt.etri.re.kr/mdt/Test/sm/AddAndSleep");
		String url = String.format("https://localhost:19000/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		AASOperationClient opClient = new AASOperationClient(svc, "Operation", Duration.ofSeconds(1));
		opClient.setInoutputVariable("IncAmount", PropertyUtils.INT("IncAmount", 33));
		opClient.setInoutputVariable("SleepTime", PropertyUtils.FLOAT("SleepTime", 2.3f));
		
		OperationResult result = opClient.run();
		
//		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(5*60*1000);
//		OperationResult result = svc.invokeOperationSync("SimulationOperation", inputVars, List.of(), jtimeout);
		System.out.printf("inoutputs=%s, outputs=%s%n", result.getInoutputArguments(), result.getOutputArguments());
	}
}
