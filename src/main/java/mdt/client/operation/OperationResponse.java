package mdt.client.operation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import lombok.Builder;
import lombok.Getter;

import utils.InternalException;
import utils.func.Try;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValues;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class OperationResponse {
	private final String session;
	private final OperationStatus status;
	@Nullable private String outputValuesString;
	@Nullable private final String message;
	
	@JsonCreator
	public OperationResponse(@JsonProperty("session") String session,
								@JsonProperty("status") OperationStatus status,
								@JsonProperty("outputValuesString") String outputValuesString,
								@JsonProperty("message") String message) {
		this.session = session;
		this.status = status;
		this.outputValuesString = outputValuesString;
		this.message = message;
	}
	
	@JsonIgnore
	public Map<String,SubmodelElement> getOutputValues() {
		try {
			Map<String,SubmodelElement> outputValues = Maps.newHashMap();
			
			if ( this.outputValuesString != null ) {
				JsonNode paramMapNode = MDTModelSerDe.readJsonNode(outputValuesString);
				Iterator<Entry<String,JsonNode>> iter = paramMapNode.fields();
				while ( iter.hasNext() ) {
					Entry<String,JsonNode> ent = iter.next();
                    String key = ent.getKey();
                    JsonNode valueNode = ent.getValue();
                    
                    SubmodelElement updated = MDTModelSerDe.readValue(valueNode, SubmodelElement.class);
                    outputValues.put(key, updated);
				}
			}
			
			return outputValues;
		}
		catch ( Exception e ) {
			throw new InternalException(e);
		}
	}
	
	public Throwable toJavaException() {
		int idx = this.message.indexOf(':');
		if ( idx < 0 ) {
			throw new IllegalStateException("Invalid Error message: " + this.message);
		}
		
		String code = this.message.substring(0, idx);
		String details = this.message.substring(idx+1);
		
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
			
			return new Exception(this.message);
		}
		catch ( Exception e ) {
			throw new InternalException(e);
		}
	}
	
	@Override
	public String toString() {
		Map<String,SubmodelElement> outputs = getOutputValues();
		String valuesStr = FStream.from(outputs)
									.mapValue(ElementValues::getValue)
									.map(kv -> String.format("%s:%s", kv.key(), kv.value()))
									.join(", ");
		String msgStr = (this.message != null) ? String.format(", message=%s", this.message) : "";
		return String.format("[%s] status=%s, outputs={%s}%s", this.session, this.status, valuesStr, msgStr);
	}
	
	public static <T> OperationResponse running(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.RUNNING)
								.outputValuesString(null)
								.message(msg)
								.build();
	}
	
	public static <T> OperationResponse completed(String sessionId, Map<String,SubmodelElement> outputValues) {
		JsonNode root = FStream.from(outputValues)
						        .mapValue(MDTModelSerDe::toJsonNode)
						        .fold(MDTModelSerDe.MAPPER.createObjectNode(), (r,kv) -> r.set(kv.key(), kv.value()));
		
		try {
			String outputValuesString = MDTModelSerDe.toJsonString(root);
			return OperationResponse.builder()
									.session(sessionId)
									.status(OperationStatus.COMPLETED)
									.outputValuesString(outputValuesString)
									.build();
		}
		catch ( IOException e ) {
			throw new InternalException(e);
		}
	}
	
	public static <T> OperationResponse failed(String sessionId, Throwable cause) {
		String errorMsg = String.format("%s: %s", cause.getClass().getName(), cause.getMessage());
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.FAILED)
								.outputValuesString(null)
								.message(errorMsg)
								.build();
	}
	
	public static <T> OperationResponse cancelled(String sessionId, String msg) {
		return OperationResponse.builder()
								.session(sessionId)
								.status(OperationStatus.CANCELLED)
								.outputValuesString(null)
								.message(msg)
								.build();
	}
}
