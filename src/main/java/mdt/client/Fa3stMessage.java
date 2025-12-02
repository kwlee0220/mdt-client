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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.func.FOption;
import utils.http.RESTfulRemoteException;

import mdt.model.MessageTypeEnum;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Accessors(prefix="m_")
@JsonPropertyOrder({"messageType", "text", "code", "timestamp"})
@JsonInclude(Include.NON_NULL)
public class Fa3stMessage {
    private MessageTypeEnum m_messageType;
    private String m_text;
    private String m_code;
    private String m_timestamp;
	
	public static Fa3stMessage from(Throwable e) {
		ZonedDateTime now = Instant.now().atZone(ZoneOffset.systemDefault());
		String nowStr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
		return new Fa3stMessage(MessageTypeEnum.Exception, e.getMessage(), e.getClass().getName(), nowStr);
	}
    
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
    
    public String getCode() {
    	return m_code;
    }
	
	public RESTfulRemoteException toClientException() {
		String msg = FOption.mapOrSupply(m_text,
										t -> String.format("[%s] code=%s, details: %s", m_messageType, m_code, t),
										() -> String.format("[%s] code=%s", m_messageType, m_code));
		throw new RESTfulRemoteException(msg);
	}
}
