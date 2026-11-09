package mdt.client.operation;

import java.io.IOException;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import utils.stream.FStream;

import mdt.model.MDTModelSerDe;


/**
 * HTTP 연산 서버에 전송하는 연산 실행 요청 메시지.
 * <p>
 * JSON으로는 {@code operation}(연산 식별자), {@code inputArguments}(입력 인자 이름과 값의 맵),
 * {@code async}(비동기 실행 여부) 필드로 직렬화되며, {@code null}인 필드는 생략된다.
 *
 * @author Kang-Woo Lee (ETRI)
 * @deprecated	{@code mdt-operation-http}의 {@code OperationController}와 URL 및 메시지 형식이 맞지 않아
 * 			사용할 수 없다. 대신 {@link utils.rpc.restful.RESTfulAsyncRpcClient}를 사용한다.
 */
@Deprecated(forRemoval = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "operation", "inputArguments", "async" })
public class OperationRequest {
	@JsonProperty("operation") private String m_opId;
	@JsonProperty("inputArguments")
	private Map<String,SubmodelElement> m_inputArguments = Maps.newHashMap();
	@JsonProperty("async") private boolean m_async = true;
	
	/**
	 * 실행할 연산의 식별자를 반환한다.
	 *
	 * @return	연산 식별자.
	 */
	public String getOperation() {
		return m_opId;
	}
	
	/**
	 * 실행할 연산의 식별자를 설정한다.
	 *
	 * @param id	연산 식별자.
	 */
	public void setOperation(String id) {
		m_opId = id;
	}
	
	/**
	 * 입력 인자 맵을 반환한다.
	 * <p>
	 * 내부 맵이 그대로 반환되므로, 반환된 맵을 변경하면 요청에도 반영된다.
	 *
	 * @return	입력 인자 이름을 키로 하는 {@link SubmodelElement} 맵.
	 */
	public Map<String,SubmodelElement> getInputArguments() {
		return m_inputArguments;
	}
	
	/**
	 * 입력 인자 맵을 설정한다. 주어진 맵은 복사되어 저장된다.
	 *
	 * @param arguments	입력 인자 이름을 키로 하는 {@link SubmodelElement} 맵.
	 */
	public void setInputArguments(Map<String,SubmodelElement> arguments) {
		m_inputArguments = Maps.newHashMap(arguments);
	}
	
	/**
	 * 연산을 비동기로 실행할지 여부를 반환한다. 기본값은 {@code true}이다.
	 *
	 * @return	비동기 실행 여부.
	 */
	public boolean isAsync() {
		return m_async;
	}
	
	/**
	 * 연산을 비동기로 실행할지 여부를 설정한다.
	 *
	 * @param async	비동기 실행 여부.
	 */
	public void setAsync(boolean async) {
		m_async = async;
	}
	
	/**
	 * JSON 문자열을 {@link OperationRequest} 객체로 변환한다.
	 *
	 * @param jsonStr	JSON 문자열.
	 * @return	변환된 {@link OperationRequest} 객체.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static OperationRequest parseJsonString(String jsonStr) throws IOException {
		return MDTModelSerDe.readValue(jsonStr, OperationRequest.class);
	}
	
	/**
	 * 본 요청을 JSON 문자열로 변환한다.
	 *
	 * @return	JSON 문자열.
	 * @throws IOException	JSON 직렬화에 실패한 경우.
	 */
	public String toJsonString() throws IOException {
		return MDTModelSerDe.toJsonString(this);
	}
	
	public String toString() {
		String inArgNames = FStream.from(m_inputArguments.keySet()).join(", ");

		return String.format("(%s)", inArgNames);
	}
}
