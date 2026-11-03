package mdt.model;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.jetbrains.annotations.Nullable;

import utils.http.RESTfulRemoteException;


/**
 * 서버에서 발생한 예외를 JSON으로 전달하기 위한 오류 엔티티이다.
 * <p>
 * 메시지 종류({@code messageType}), 예외 클래스 이름({@code code}), 예외 메시지({@code text}),
 * 발생 시각({@code timestamp})으로 구성되며, JSON 직렬화 시 {@code null}인 속성은 생략되고
 * 역직렬화 시 알 수 없는 속성은 무시된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"messageType", "text", "code", "timestamp"})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MDTExceptionEntity {
	@JsonProperty("messageType") 
	private MessageTypeEnum m_messageType;
	@Nullable @JsonProperty("code")
	private String m_code;
	@Nullable @JsonProperty("text") 
	private String m_text;
	@Nullable @JsonProperty("timestamp")
	private String m_timestamp;
	
	/**
	 * 주어진 예외로부터 오류 엔티티를 생성한다.
	 * <p>
	 * 메시지 종류는 {@link MessageTypeEnum#Exception}, {@code code}는 예외 클래스의 이름, {@code text}는 예외
	 * 메시지, {@code timestamp}는 현재 시각(시스템 시간대, {@code yyyy-MM-dd'T'HH:mm:ss.SSSXXX} 형식)으로
	 * 설정된다.
	 *
	 * @param e	대상 예외.
	 * @return	생성된 오류 엔티티.
	 */
	public static MDTExceptionEntity from(Throwable e) {
		MDTExceptionEntity entity = new MDTExceptionEntity();
		entity.m_messageType = MessageTypeEnum.Exception;
		entity.m_text = e.getMessage();
		entity.m_code = e.getClass().getName();

		ZonedDateTime zdt = Instant.now().atZone(ZoneOffset.systemDefault());
		entity.m_timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(zdt);
		
		return entity;
	}
	
	/** 모든 속성이 {@code null}인 빈 오류 엔티티를 생성한다. JSON 역직렬화에 사용된다. */
	public MDTExceptionEntity() { }
	/**
	 * 주어진 오류 코드와 메시지로 오류 엔티티를 생성한다.
	 * <p>
	 * 메시지 종류는 {@link MessageTypeEnum#Exception}, {@code timestamp}는 현재 시각으로 설정된다.
	 *
	 * @param code	오류 코드 (일반적으로 예외 클래스 이름).
	 * @param text	오류 메시지.
	 */
	public MDTExceptionEntity(String code, String text) {
		m_messageType = MessageTypeEnum.Exception;
		m_code = code;
		m_text = text;
		
		ZonedDateTime zdt = Instant.now().atZone(ZoneOffset.systemDefault());
		m_timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(zdt);
	}

	/**
	 * 메시지 종류를 반환한다.
	 *
	 * @return	메시지 종류.
	 */
	@JsonProperty("messageType") 
	public MessageTypeEnum getMessageType() {
		return m_messageType;
	}
	/**
	 * 메시지 종류를 설정한다.
	 *
	 * @param messageType	메시지 종류.
	 */
	@JsonProperty("messageType")
	public void setMessageType(MessageTypeEnum messageType) {
		m_messageType = messageType;
	}
	
	/**
	 * 오류 코드(예외 클래스 이름)를 반환한다.
	 *
	 * @return	오류 코드. 설정되지 않은 경우는 {@code null}.
	 */
	public String getCode() {
		return m_code;
	}
	/**
	 * 오류 코드(예외 클래스 이름)를 설정한다.
	 *
	 * @param code	오류 코드.
	 */
	public void setCode(String code) {
		m_code = code;
	}
	
	/**
	 * 오류 메시지를 반환한다.
	 *
	 * @return	오류 메시지. 설정되지 않은 경우는 {@code null}.
	 */
	public String getText() {
		return m_text;
	}
	/**
	 * 오류 메시지를 설정한다.
	 *
	 * @param text	오류 메시지.
	 */
	public void setText(String text) {
		m_text = text;
	}
	
	/**
	 * 오류 발생 시각 문자열을 반환한다.
	 *
	 * @return	발생 시각. 설정되지 않은 경우는 {@code null}.
	 */
	@Nullable @JsonProperty("timestamp")
	public String getTimestamp() {
		return m_timestamp;
	}
	/**
	 * 오류 발생 시각 문자열을 설정한다.
	 *
	 * @param ts	발생 시각.
	 */
	@JsonProperty("timestamp")
	public void setTimestamp(@Nullable String ts) {
		m_timestamp = ts;
	}
	
	/**
	 * 오류 엔티티 내용으로 {@link RESTfulRemoteException}을 만들어 반환한다.
	 * <p>
	 * 예외 메시지는 {@code "code=<code>, details=<text>"} (또는 {@code text}가 없으면 {@code "code=<code>"})이다.
	 *
	 * @return	생성된 예외 객체.
	 */
	public RESTfulRemoteException toClientException() {
		if ( m_text != null ) {
			return new RESTfulRemoteException("code=" + m_code + ", details=" + m_text);
		}
		else {
			return new RESTfulRemoteException("code=" + m_code);
		}
	}
	
	/**
	 * 오류 코드에 기록된 클래스 이름으로 원래 예외 객체를 재구성하여 반환한다.
	 * <p>
	 * {@code code}에 해당하는 클래스를 로드하여, {@code text}가 있으면 문자열 인자 하나를 받는 생성자로,
	 * 없으면 인자 없는 생성자로 예외 객체를 만든다. 클래스를 찾을 수 없으면 {@link RESTfulRemoteException}이
	 * 사용되고, 해당 생성자가 없거나 생성 중 오류가 발생하면 {@link #toClientException()}의 결과가 반환된다.
	 * 원격에서 받은 클래스 이름을 그대로 로드하므로 신뢰할 수 있는 서버의 응답에만 사용해야 한다.
	 *
	 * @return	재구성된 예외 객체.
	 */
	public Throwable toException() {
		Class<? extends Throwable> cls = loadThrowableClass();
		try {
			if ( m_text != null ) {
				Constructor<? extends Throwable> ctor = getSingleStringContructor(cls);
				if ( ctor != null ) {
					return ctor.newInstance(m_text);
				}
				else {
					return new RESTfulRemoteException(m_text);
				}
			}
			else {
				Constructor<? extends Throwable> ctor = getNoArgContructor(cls);
				if ( ctor != null ) {
					return ctor.newInstance();
				}
				else {
					return new RESTfulRemoteException("code=" + m_code);
				}
			}
		}
		catch ( Exception e ) {
			return toClientException();
		}
	}
	
	private Constructor<? extends Throwable>
	getNoArgContructor(Class<? extends Throwable> cls) {
		try {
			return cls.getDeclaredConstructor();
		}
		catch ( Throwable e ) {
			return null;
		}
	}
	
	private Constructor<? extends Throwable>
	getSingleStringContructor(Class<? extends Throwable> cls) {
		try {
			return cls.getDeclaredConstructor(String.class);
		}
		catch ( Throwable e ) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Throwable> loadThrowableClass() {
		try {
			return (Class<? extends Throwable>)Class.forName(m_code);
		}
		catch ( ClassNotFoundException e ) {
			return RESTfulRemoteException.class;
		}
	}
}
