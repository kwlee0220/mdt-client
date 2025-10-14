package mdt.client.operation;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.InternalException;
import utils.func.Try;
import utils.stream.FStream;

import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.Variable;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"session", "status", "result", "message"})
@JsonInclude(Include.NON_NULL)
public class OperationResponse {
	/** 비동기적으로 수행하는 경우 수행 중인 연산의 식별자 */
	private final String m_sessionId;
	/** 연산의 수행 상태 */
	private final OperationStatus m_status;
	/** 연산 수행 결과 (성공적으로 연산이 완료된 경우) */
	private final @Nullable List<Variable> m_result;
	/** 연산 수행 결과 메시지 (연산 수행이 성공적으로 완료되지 못한 경우) */
	private final @Nullable String m_message;
	
	@JsonCreator
	public OperationResponse(@JsonProperty("session") String sessionId,
								@JsonProperty("status") OperationStatus status,
								@JsonProperty("result") List<Variable> result,
								@JsonProperty("message") String message) {
		m_sessionId = sessionId;
		m_status = status;
		m_result = result;
		m_message = message;
	}
	
	private OperationResponse(Builder builder) {
		m_sessionId = builder.m_sessionId;
		m_status = builder.m_status;
		m_result = builder.m_result;
		m_message = builder.m_message;
	}
	
	@JsonProperty("session")
	public String getSessionId() {
		return m_sessionId;
	}
	
	public OperationStatus getStatus() {
		return m_status;
	}
	
	public List<Variable> getResult() {
		return m_result;
	}
	
	public String getMessage() {
		return m_message;
	}
	
	public Throwable toJavaException() {
		int idx = this.m_message.indexOf(':');
		if ( idx < 0 ) {
			throw new IllegalStateException("Invalid Error message: " + this.m_message);
		}
		
		String code = this.m_message.substring(0, idx);
		String details = this.m_message.substring(idx+1);
		
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Throwable> errorCls = Try.get(() -> (Class<? extends Throwable>)Class.forName(code)).get();
			Constructor<? extends Throwable> ctor1 = Try.get(() -> errorCls.getDeclaredConstructor(String.class)).getOrNull();
			if ( ctor1 != null ) {
				return ctor1.newInstance(details);
			}
			Constructor<? extends Throwable> ctor0 = Try.get(() -> errorCls.getDeclaredConstructor()).getOrNull();
			if ( ctor0 != null ) {
				return ctor0.newInstance();
			}
			
			return new Exception(this.m_message);
		}
		catch ( Exception e ) {
			throw new InternalException(e);
		}
	}
	
	@Override
	public String toString() {
		String valuesStr = (m_result != null)
								? FStream.from(m_result)
										.map(Variable::toString)
										.join(", ")
								: "";
		String msgStr = (this.m_message != null) ? String.format(", message=%s", this.m_message) : "";
		return String.format("[session=%s] status=%s, outputs={%s}%s",
							this.m_sessionId, this.m_status, valuesStr, msgStr);
	}
	
	public static <T> OperationResponse running(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.RUNNING)
								.result(null)
								.message(msg)
								.build();
	}
	
	public static <T> OperationResponse completed(String sessionId, List<Variable> outputVariables) {
		// Result 중에서 'ReferenceVariable'이 아닌 경우만 뽑아서 result를 구성한다.
		List<Variable> result = FStream.from(outputVariables)
										.filter(var -> !(var instanceof ReferenceVariable))
										.toList();
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.COMPLETED)
								.result(result)
								.build();
	}
	
	public static <T> OperationResponse failed(String sessionId, Throwable cause) {
		String errorMsg = String.format("%s: %s", cause.getClass().getName(), cause.getMessage());
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.FAILED)
								.result(null)
								.message(errorMsg)
								.build();
	}
	
	public static <T> OperationResponse cancelled(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.CANCELLED)
								.result(null)
								.message(msg)
								.build();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		/** 비동기적으로 수행하는 경우 수행 중인 연산의 식별자 */
		private String m_sessionId;
		/** 연산의 수행 상태 */
		private OperationStatus m_status;
		/** 연산 수행 결과 (성공적으로 연산이 완료된 경우) */
		private @Nullable List<Variable> m_result;
		/** 연산 수행 결과 메시지 (연산 수행이 성공적으로 완료되지 못한 경우) */
		private @Nullable String m_message;
		
		public OperationResponse build() {
			return new OperationResponse(this);
		}
		
		public Builder session(String session) {
			m_sessionId = session;
			return this;
		}
		
		public Builder status(OperationStatus status) {
			m_status = status;
			return this;
		}
		
		public Builder result(List<Variable> result) {
			m_result = result;
			return this;
		}
		
		public Builder message(String message) {
			m_message = message;
			return this;
		}
	}
}
