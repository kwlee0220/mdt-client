package mdt.client.operation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.InternalException;
import utils.func.Try;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"session", "status", "outputArguments", "message"})
@JsonInclude(Include.NON_NULL)
public class OperationResponse {
	/** 비동기적으로 수행하는 경우 수행 중인 연산의 식별자 */
	private final String m_sessionId;
	/** 연산의 수행 상태 */
	private final OperationStatus m_status;
	/** 연산 수행 결과 (성공적으로 연산이 완료된 경우) */
	private final @Nullable Map<String,SubmodelElement> m_outputArguments;
	/** 연산 수행 결과 메시지 (연산 수행이 성공적으로 완료되지 못한 경우) */
	private final @Nullable String m_message;
	
	@JsonCreator
	public OperationResponse(@JsonProperty("session") String sessionId,
								@JsonProperty("status") OperationStatus status,
								@JsonProperty("outputArguments") Map<String,SubmodelElement> outputArguments,
								@JsonProperty("message") String message) {
		m_sessionId = sessionId;
		m_status = status;
		m_outputArguments = outputArguments;
		m_message = message;
	}
	
	private OperationResponse(Builder builder) {
		m_sessionId = builder.m_sessionId;
		m_status = builder.m_status;
		m_outputArguments = builder.m_outputArguments;
		m_message = builder.m_message;
	}
	
	@JsonProperty("session")
	public String getSessionId() {
		return m_sessionId;
	}
	
	public OperationStatus getStatus() {
		return m_status;
	}
	
	public Map<String,SubmodelElement> getOutputArguments() {
		return m_outputArguments;
	}
	
	public String getMessage() {
		return m_message;
	}
	
	public static OperationResponse fromJsonString(String json) throws IOException {
		return MDTModelSerDe.readValue(json, OperationResponse.class);
	}
	
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
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
		String valuesStr = (m_outputArguments != null)
								? FStream.from(m_outputArguments.keySet()).join(", ")
								: "";
		String msgStr = (this.m_message != null) ? String.format(", message=%s", this.m_message) : "";
		return String.format("[session=%s] status=%s, outputs={%s}%s",
							this.m_sessionId, this.m_status, valuesStr, msgStr);
	}
	
	public static <T> OperationResponse running(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.RUNNING)
								.message(msg)
								.build();
	}
	
	public static <T> OperationResponse completed(String sessionId, Map<String,SubmodelElement> outputArguments) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.COMPLETED)
								.outputArguments(outputArguments)
								.build();
	}
	
	public static <T> OperationResponse failed(String sessionId, Throwable cause) {
		String errorMsg = String.format("%s: %s", cause.getClass().getName(), cause.getMessage());
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.FAILED)
								.outputArguments(null)
								.message(errorMsg)
								.build();
	}
	
	public static <T> OperationResponse cancelled(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.CANCELLED)
								.outputArguments(null)
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
		private @Nullable Map<String,SubmodelElement> m_outputArguments = Map.of();
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
		
		public Builder outputArguments(Map<String,SubmodelElement> outputArguments) {
			m_outputArguments = outputArguments;
			return this;
		}
		
		public Builder message(String message) {
			m_message = message;
			return this;
		}
	}
}
