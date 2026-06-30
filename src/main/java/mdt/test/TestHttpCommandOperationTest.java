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
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestHttpCommandOperationTest {
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
