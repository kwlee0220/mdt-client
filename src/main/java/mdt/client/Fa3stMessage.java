package mdt.client;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.func.Nullables;
import utils.http.RESTfulRemoteException;

import mdt.model.MessageTypeEnum;

/**
 * FA³ST 서버가 응답에 포함시키는 메시지({@code messages} 배열의 원소)에 대응하는 JSON 매핑 클래스.
 * <p>
 * 오류 응답인 경우 {@code code}에는 서버 측 예외 클래스 이름이, {@code text}에는 예외 메시지가 담긴다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"messageType", "text", "code", "timestamp"})
@JsonInclude(Include.NON_NULL)
public class Fa3stMessage {
    private MessageTypeEnum m_messageType;
    private String m_text;
    private String m_code;
    private String m_timestamp;
	
	/**
	 * 주어진 예외로부터 {@link MessageTypeEnum#Exception} 종류의 메시지를 생성한다.
	 * <p>
	 * {@code text}는 예외 메시지, {@code code}는 예외 클래스 이름, {@code timestamp}는 시스템 기본 시간대의
	 * 현재 시각({@code yyyy-MM-dd'T'HH:mm:ss.SSSXXX} 형식)으로 설정된다.
	 *
	 * @param e	메시지로 변환할 예외.
	 * @return	생성된 메시지 객체.
	 */
	public static Fa3stMessage from(Throwable e) {
		ZonedDateTime now = Instant.now().atZone(ZoneOffset.systemDefault());
		String nowStr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
		return new Fa3stMessage(MessageTypeEnum.Exception, e.getMessage(), e.getClass().getName(), nowStr);
	}
    
    /**
     * 메시지 객체를 생성한다.
     *
     * @param messageType	메시지 종류.
     * @param text	메시지 내용.
     * @param code	메시지 코드. 오류 메시지인 경우 예외 클래스 이름.
     * @param timestamp	메시지 생성 시각 문자열.
     */
    @JsonCreator
    public Fa3stMessage(@JsonProperty("messageType") MessageTypeEnum messageType,
    					@JsonProperty("text") String text,
    					@JsonProperty("code") String code,
    					@JsonProperty("timestamp") String timestamp) {
    	this.m_messageType = messageType;
    	this.m_text = text;
    	m_code = code;
    	this.m_timestamp = timestamp;
    }
    
    /**
     * 메시지 종류를 반환한다.
     *
     * @return	메시지 종류.
     */
    public MessageTypeEnum getMessageType() {
		return m_messageType;
	}
    
    /**
     * 메시지 종류를 설정한다.
     *
     * @param messageType	메시지 종류.
     */
    public void setMessageType(MessageTypeEnum messageType) {
		m_messageType = messageType;
    }
    
    /**
     * 메시지 내용을 반환한다.
     *
     * @return	메시지 내용.
     */
    public String getText() {
    	return m_text;
    }
    
    /**
     * 메시지 내용을 설정한다.
     *
     * @param text	메시지 내용.
     */
    public void setText(String text) {
		m_text = text;
	}
    
    /**
     * 메시지 코드를 반환한다.
     *
     * @return	메시지 코드. 오류 메시지인 경우 예외 클래스 이름.
     */
    public String getCode() {
    	return m_code;
    }
    
    /**
     * 메시지 코드를 설정한다.
     *
     * @param code	메시지 코드.
     */
    public void setCode(String code) {
    	m_code = code;
    }
    
    /**
     * 메시지 생성 시각 문자열을 반환한다.
     *
     * @return	메시지 생성 시각 문자열.
     */
    public String getTimestamp() {
		return m_timestamp;
	}
    
    /**
     * 메시지 생성 시각 문자열을 설정한다.
     *
     * @param timestamp	메시지 생성 시각 문자열.
     */
    public void setTimestamp(String timestamp) {
		m_timestamp = timestamp;
	}
	
	/**
	 * 메시지 종류, 코드, 내용으로 구성된 {@link RESTfulRemoteException}을 생성하여 반환한다.
	 *
	 * @return	생성된 예외 객체.
	 */
	public RESTfulRemoteException toClientException() {
		String msg = Nullables.mapOrGet(m_text,
										t -> String.format("[%s] code=%s, details: %s", m_messageType, m_code, t),
										() -> String.format("[%s] code=%s", m_messageType, m_code));
		return new RESTfulRemoteException(msg);
	}
}
