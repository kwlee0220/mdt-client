package mdt.client.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import okhttp3.OkHttpClient;

import mdt.workflow.Workflow;

/**
 * {@link HttpWorkflowManager#startWorkflow(String, String)}가 실행 식별자를 요청에 싣는지 확인한다.
 */
public class HttpWorkflowManagerTest {
	private HttpServer m_server;
	private final List<String> m_requests = new ArrayList<>();		// "METHOD path?query"
	private HttpWorkflowManager m_manager;

	@BeforeEach
	public void setUp() throws Exception {
		m_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		m_server.createContext("/", exchange -> {
			m_requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getRawPath()
							+ (exchange.getRequestURI().getRawQuery() != null
								? "?" + exchange.getRequestURI().getRawQuery() : ""));
			byte[] body = "{\"name\": \"m1__r-1\", \"modelId\": \"m1\", \"status\": \"RUNNING\"}"
								.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		m_server.start();
		m_manager = new HttpWorkflowManager(new OkHttpClient(),
											"http://127.0.0.1:" + m_server.getAddress().getPort() + "/wf");
	}

	@AfterEach
	public void tearDown() {
		m_server.stop(0);
	}

	@Test
	public void startWithRunIdSendsItAsQueryParameter() {
		Workflow wf = m_manager.startWorkflow("m1", "insp.2026-11-09_a b");
		assertEquals("m1__r-1", wf.getName());
		assertEquals(List.of("POST /wf/models/m1/start?runId=insp.2026-11-09_a+b"), m_requests);
	}

	@Test
	public void startWithoutRunIdSendsNoQuery() {
		m_manager.startWorkflow("m1");
		assertEquals(List.of("POST /wf/models/m1/start"), m_requests);
	}
}
