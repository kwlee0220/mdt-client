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

import utils.http.RESTfulRemoteException;

import mdt.model.MessageTypeEnum;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonPropertyOrder({"messageType", "text", "code", "timestamp"})
@JsonInclude(Include.NON_NULL)
public class Fa3stMessage {
    private MessageTypeEnum messageType;
    private String text;
    private String code;
    private String timestamp;
	
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
    	this.messageType = messageType;
    	this.text = text;
    	this.code = code;
    	this.timestamp = timestamp;
    }
	
	public RESTfulRemoteException toClientException() {
		if ( this.text != null ) {
			throw new RESTfulRemoteException("code=" + this.code + ", details=" + this.text);
		}
		else {
			throw new RESTfulRemoteException("code=" + this.code);
		}
	}
}
