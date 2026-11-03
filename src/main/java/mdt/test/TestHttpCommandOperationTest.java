package mdt.test;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;

import utils.json.JacksonUtils;
import utils.rpc.restful.RpcRequestMessage;
import utils.rpc.restful.RpcResponseMessage;
import utils.rpc.restful.RpcState;
import utils.rpc.restful.process.DefaultCommandVariableSerDe;
import utils.rpc.restful.process.RESTfulCommandExecutionServer;

/**
 * {@code RESTfulCommandExecutionServer}에 직접 요청을 보내 외부 명령 연산을 시작하고,
 * 완료될 때까지 상태를 폴링하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestHttpCommandOperationTest {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		File opDescFile = new File("/home/kwlee/mdt/mdt-operation-http/operations/AddAndSleep/operation.json");
		DefaultCommandVariableSerDe serde = new DefaultCommandVariableSerDe(JacksonUtils.MAPPER);
		RESTfulCommandExecutionServer server = new RESTfulCommandExecutionServer(opDescFile, serde);
		
		Map<String,JsonNode> inputs = Map.of("IncAmount", new IntNode(10),
											"SleepTime", new IntNode(5),
											"Data", new IntNode(1));
		RpcRequestMessage req = new RpcRequestMessage(inputs);
		
		RpcResponseMessage resp;
		resp = server.start(req);
		System.out.println("resp=" + resp);
		String session = resp.getSessionEndpoint();
		
		while ( resp.getState() == RpcState.RUNNING ) {
			Thread.sleep(500);
			resp = server.status(session);
			System.out.println("resp=" + resp);
		}
	}
}
