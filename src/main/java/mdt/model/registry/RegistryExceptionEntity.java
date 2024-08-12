package mdt.model.registry;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"messageType", "text", "code", "timestamp"})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryExceptionEntity {
	@JsonProperty("messageType") 
	private MessageTypeEnum m_messageType;
	@JsonProperty("text") 
	private String m_text;
	@Nullable @JsonInclude(Include.NON_NULL) @JsonProperty("code")
	private String m_code;
	@Nullable @JsonInclude(Include.NON_NULL) @JsonProperty("timestamp")
	private String m_timestamp;
	
	public static RegistryExceptionEntity from(Exception e) {
		RegistryExceptionEntity entity = new RegistryExceptionEntity();
		entity.m_messageType = MessageTypeEnum.Exception;
		entity.m_text = e.getMessage();
		entity.m_code = e.getClass().getName();

		ZonedDateTime zdt = Instant.now().atZone(ZoneOffset.systemDefault());
		entity.m_timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(zdt);
		
		return entity;
	}
	
	public RegistryExceptionEntity() { }

	@JsonProperty("messageType") 
	public MessageTypeEnum getMessageType() {
		return m_messageType;
	}
	@JsonProperty("messageType")
	public void setMessageType(MessageTypeEnum messageType) {
		m_messageType = messageType;
	}
	
	@JsonProperty("text") 
	public String getText() {
		return m_text;
	}
	@JsonProperty("text")
	public void setText(String text) {
		m_text = text;
	}
	
	@Nullable @JsonInclude(Include.NON_NULL) @JsonProperty("code")
	public String getCode() {
		return m_code;
	}
	@JsonInclude(Include.NON_NULL) @JsonProperty("code")
	public void setCode(@Nullable String code) {
		m_code = code;
	}
	
	@Nullable @JsonInclude(Include.NON_NULL) @JsonProperty("timestamp")
	public String getTimestamp() {
		return m_timestamp;
	}
	@JsonInclude(Include.NON_NULL) @JsonProperty("timestamp")
	public void setTimestamp(@Nullable String ts) {
		m_timestamp = ts;
	}
}
