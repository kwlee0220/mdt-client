package mdt.client.operation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

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
 * HTTP 연산 서버가 연산 실행 요청 또는 세션 상태 조회에 대해 반환하는 응답 메시지.
 * <p>
 * JSON으로는 {@code session}(세션 식별자), {@code status}({@link OperationStatus}),
 * {@code outputArguments}(출력 인자 맵), {@code message}(부가 메시지) 필드로 직렬화되며,
 * {@code null}인 필드는 생략된다. 연산이 실패한 경우 {@code message}는
 * {@code "<예외 클래스 이름>: <예외 메시지>"} 형식을 가지며, {@link #toJavaException()}으로 예외 객체를 복원할 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 * @deprecated	{@code mdt-operation-http}의 {@code RpcServerDispatcher}와 URL 및 메시지 형식이 맞지 않아
 * 			사용할 수 없다. 대신 {@link utils.rpc.restful.RESTfulAsyncRpcClient}를 사용한다.
 */
@Deprecated(forRemoval = true)
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
	
	/**
	 * 주어진 값들로 응답 객체를 생성한다. JSON 역직렬화에 사용된다.
	 *
	 * @param sessionId	세션 식별자.
	 * @param status	연산 수행 상태.
	 * @param outputArguments	출력 인자 맵. 연산이 완료되지 않은 경우 {@code null}일 수 있다.
	 * @param message	부가 메시지. {@code null}일 수 있다.
	 */
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
	
	/**
	 * 연산을 수행 중인 세션의 식별자를 반환한다.
	 *
	 * @return	세션 식별자.
	 */
	@JsonProperty("session")
	public String getSessionId() {
		return m_sessionId;
	}
	
	/**
	 * 연산의 수행 상태를 반환한다.
	 *
	 * @return	연산 수행 상태.
	 */
	public OperationStatus getStatus() {
		return m_status;
	}
	
	/**
	 * 연산의 출력 인자 맵을 반환한다.
	 *
	 * @return	출력 인자 이름을 키로 하는 {@link SubmodelElement} 맵. 설정되지 않은 경우 {@code null}.
	 */
	public Map<String,SubmodelElement> getOutputArguments() {
		return m_outputArguments;
	}
	
	/**
	 * 응답의 부가 메시지를 반환한다.
	 *
	 * @return	부가 메시지. 설정되지 않은 경우 {@code null}.
	 */
	public String getMessage() {
		return m_message;
	}
	
	/**
	 * JSON 문자열을 {@link OperationResponse} 객체로 변환한다.
	 *
	 * @param json	JSON 문자열.
	 * @return	변환된 {@link OperationResponse} 객체.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static OperationResponse fromJsonString(String json) throws IOException {
		return MDTModelSerDe.readValue(json, OperationResponse.class);
	}
	
	/**
	 * 본 응답을 JSON 문자열로 변환한다.
	 *
	 * @return	JSON 문자열.
	 */
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}
	
	/**
	 * 실패 응답의 메시지로부터 원래의 예외 객체를 복원한다.
	 * <p>
	 * 메시지를 첫 번째 {@code ':'}를 기준으로 예외 클래스 이름과 상세 메시지로 나눈 뒤, 해당 클래스를 적재하여
	 * 문자열 인자 생성자 또는 기본 생성자로 예외 객체를 생성한다. 두 생성자가 모두 없으면 메시지 전체를 담은
	 * {@link Exception}을 반환한다. 서버가 보낸 클래스 이름을 적재하므로 신뢰할 수 있는 서버에 대해서만 사용해야 한다.
	 * 클래스는 초기화하지 않고 적재하며, {@link Throwable}의 하위 클래스가 아니면 메시지 전체를 담은 {@link Exception}을
	 * 반환한다.
	 *
	 * @return	복원된 예외 객체.
	 * @throws IllegalStateException	메시지가 없거나 {@code ':'}가 없는 경우.
	 * @throws InternalException	예외 클래스를 적재하거나 생성하는 데 실패한 경우.
	 */
	public Throwable toJavaException() {
		if ( this.m_message == null ) {
			throw new IllegalStateException("No error message: " + this);
		}
		int idx = this.m_message.indexOf(':');
		if ( idx < 0 ) {
			throw new IllegalStateException("Invalid Error message: " + this.m_message);
		}
		
		String code = this.m_message.substring(0, idx);
		String details = this.m_message.substring(idx+1);
		
		try {
			// 원격에서 받은 이름이므로 static 초기화를 실행하지 않고 적재하고, 예외 클래스인지 확인한다.
			Class<?> loaded = Class.forName(code, false, OperationResponse.class.getClassLoader());
			if ( !Throwable.class.isAssignableFrom(loaded) ) {
				return new Exception(this.m_message);
			}
			Class<? extends Throwable> errorCls = loaded.asSubclass(Throwable.class);
			Constructor<? extends Throwable> ctor1 = Try.of(() -> errorCls.getDeclaredConstructor(String.class)).getOrNull();
			if ( ctor1 != null ) {
				return ctor1.newInstance(details);
			}
			Constructor<? extends Throwable> ctor0 = Try.of(() -> errorCls.getDeclaredConstructor()).getOrNull();
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
	
	/**
	 * {@link OperationStatus#RUNNING} 상태의 응답을 생성한다.
	 *
	 * @param <T>	사용되지 않는 타입 파라미터.
	 * @param sessionId	세션 식별자.
	 * @param msg	부가 메시지.
	 * @return	생성된 응답 객체.
	 */
	public static <T> OperationResponse running(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.RUNNING)
								.message(msg)
								.build();
	}
	
	/**
	 * {@link OperationStatus#COMPLETED} 상태의 응답을 생성한다.
	 *
	 * @param <T>	사용되지 않는 타입 파라미터.
	 * @param sessionId	세션 식별자.
	 * @param outputArguments	연산 출력 인자 맵.
	 * @return	생성된 응답 객체.
	 */
	public static <T> OperationResponse completed(String sessionId, Map<String,SubmodelElement> outputArguments) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.COMPLETED)
								.outputArguments(outputArguments)
								.build();
	}
	
	/**
	 * {@link OperationStatus#FAILED} 상태의 응답을 생성한다.
	 * <p>
	 * 메시지는 {@code "<예외 클래스 이름>: <예외 메시지>"} 형식으로 설정되며, 출력 인자는 {@code null}이다.
	 *
	 * @param <T>	사용되지 않는 타입 파라미터.
	 * @param sessionId	세션 식별자.
	 * @param cause	연산 실패 원인 예외.
	 * @return	생성된 응답 객체.
	 */
	public static <T> OperationResponse failed(String sessionId, Throwable cause) {
		String errorMsg = String.format("%s: %s", cause.getClass().getName(), cause.getMessage());
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.FAILED)
								.outputArguments(null)
								.message(errorMsg)
								.build();
	}
	
	/**
	 * {@link OperationStatus#CANCELLED} 상태의 응답을 생성한다. 출력 인자는 {@code null}이다.
	 *
	 * @param <T>	사용되지 않는 타입 파라미터.
	 * @param sessionId	세션 식별자.
	 * @param msg	부가 메시지.
	 * @return	생성된 응답 객체.
	 */
	public static <T> OperationResponse cancelled(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.CANCELLED)
								.outputArguments(null)
								.message(msg)
								.build();
	}
	
	/**
	 * {@link OperationResponse} 객체를 생성하기 위한 빌더를 반환한다.
	 *
	 * @return	새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link OperationResponse} 객체 생성을 위한 빌더.
	 * <p>
	 * 출력 인자의 기본값은 빈 맵이다.
	 */
	public static class Builder {
		/** 비동기적으로 수행하는 경우 수행 중인 연산의 식별자 */
		private String m_sessionId;
		/** 연산의 수행 상태 */
		private OperationStatus m_status;
		/** 연산 수행 결과 (성공적으로 연산이 완료된 경우) */
		private @Nullable Map<String,SubmodelElement> m_outputArguments = Map.of();
		/** 연산 수행 결과 메시지 (연산 수행이 성공적으로 완료되지 못한 경우) */
		private @Nullable String m_message;
		
		/**
		 * 설정된 값으로 {@link OperationResponse} 객체를 생성한다.
		 *
		 * @return	생성된 {@link OperationResponse} 객체.
		 */
		public OperationResponse build() {
			return new OperationResponse(this);
		}
		
		/**
		 * 세션 식별자를 설정한다.
		 *
		 * @param session	세션 식별자.
		 * @return	빌더 자신.
		 */
		public Builder session(String session) {
			m_sessionId = session;
			return this;
		}
		
		/**
		 * 연산 수행 상태를 설정한다.
		 *
		 * @param status	연산 수행 상태.
		 * @return	빌더 자신.
		 */
		public Builder status(OperationStatus status) {
			m_status = status;
			return this;
		}
		
		/**
		 * 출력 인자 맵을 설정한다.
		 *
		 * @param outputArguments	출력 인자 맵.
		 * @return	빌더 자신.
		 */
		public Builder outputArguments(Map<String,SubmodelElement> outputArguments) {
			m_outputArguments = outputArguments;
			return this;
		}
		
		/**
		 * 부가 메시지를 설정한다.
		 *
		 * @param message	부가 메시지.
		 * @return	빌더 자신.
		 */
		public Builder message(String message) {
			m_message = message;
			return this;
		}
	}
}
