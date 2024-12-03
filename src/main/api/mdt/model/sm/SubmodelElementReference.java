package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = SubmodelElementReferences.Serializer.class)
@JsonDeserialize(using = SubmodelElementReferences.Deserializer.class)
public interface SubmodelElementReference {
	/**
	 * SME 참조에 해당하는 {@link SubmodelElement}을 읽는다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 * @throws	IOException	읽는 과정에서 예외가 발생한 경우.
	 */
	public SubmodelElement read() throws IOException;

	/**
	 * SME 참조에 해당하는 {@link SubmodelElement}을 읽어서 value-only serialization 값을 반환한다.
	 * 
	 * @return	{@link SubmodelElementValue} 객체.
	 * @throws	IOException	읽는 과정에서 예외가 발생한 경우.
	 */
	public SubmodelElementValue readValue() throws IOException;
	
	/**
	 * 주어진 SubmodelElement으로 SME 참조에 해당하는 SubmodelElement을 overwrite 시킨다. 
	 *
	 * @param newElm	갱신할 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void write(SubmodelElement newElm) throws IOException;

	/**
	 * 주어진 SubmodelElement의 값으로 대상 SubmodelElement의 값을 갱신한다.
	 *
	 * @param sme	갱신할 새 값을 포함한 SubmodelElement.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void update(SubmodelElement sme) throws IOException;

	/**
	 * 주어진 값(SubmodelElementValue)으로 대상 SubmodelElement의 값을 갱신한다.
	 *
	 * @param smev	갱신할 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void update(SubmodelElementValue smev) throws IOException;
	
	/**
	 * 주어진 JsonNode로 표현된 새 값으로 SubmodelElement의 값을 갱신한다.
	 * 
	 * @param valueNode		새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateWithValueJsonNode(JsonNode valueNode) throws IOException;

	/**
	 * 주어진 Json 문자열로 표현된 새 값으로 SubmodelElement의 값을 갱신한다.
	 * 
	 * @param valueJsonString	Json으로 표현된 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateWithValueJsonString(String valueJsonString) throws IOException;
	
	public void updateWithExternalString(String rawString) throws IOException;
	
	public String toExternalString() throws IOException ;
	
	public void serialize(JsonGenerator gen) throws IOException;
}
