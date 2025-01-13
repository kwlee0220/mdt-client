package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import utils.func.FOption;

import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ElementReferenceUtils.Serializer.class)
@JsonDeserialize(using = ElementReferenceUtils.Deserializer.class)
public interface ElementReference {
	public static final String FIELD_REFERENCE_TYPE = "referenceType";
	
	/**
	 * Reads the {@link SubmodelElement} referred to by the ElementReference.
	 * 
	 * @return	{@link SubmodelElement} object.
	 * @throws	If an exception occurs during the reading process.
	 */
	public SubmodelElement read() throws IOException;

	/**
	 * Reads the {@link SubmodelElement} referred to by the ElementReference and
	 * returns its the value-only serialization value.
	 * 
	 * @return	{@link SubmodelElement} object.
	 * @throws	If an exception occurs during the reading process.
	 */
	public default SubmodelElementValue readValue() throws IOException {
		return FOption.map(read(), ElementValues::getValue);
	}
	
	/**
	 * Overwrites the SubmodelElement referred to by the ElementReference.
	 *
	 * @param newElm	갱신할 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void write(SubmodelElement newElm) throws IOException;

	/**
	 * Updates the target SubmodelElement with the value of the given SubmodelElement.
	 *
	 * @param sme	New SubmodelElement to update with.
	 * @return	Updated SubmodelElement.
	 * @throws	If an exception occurs during the update.
	 */
	public default SubmodelElement update(SubmodelElement sme) throws IOException {
		return update(ElementValues.getValue(sme));
	}

	/**
	 * Updates the value of the SubmodelElement referred to by this reference with the given SubmodelElementValue.
	 *
	 * @param smev	New SubmodelElementValue to update with.
	 * @return	Updated SubmodelElement.
	 * @throws	If an exception occurs during the update.
	 */
	public default SubmodelElement update(SubmodelElementValue smev) throws IOException {
		SubmodelElement proto = read();
		ElementValues.update(proto, smev);
		
		return proto;
	}
	
	/**
	 * Updates the value of the SubmodelElement referred to by this reference with the given JsonNode.
	 * 
	 * @param valueNode		New JsonNode
	 * @return	Updated SubmodelElement.
	 * @throws	If an exception occurs during the update.
	 */
	public SubmodelElement updateWithValueJsonNode(JsonNode valueNode) throws IOException;

	/**
	 * Updates the value of the SubmodelElement referred to by this reference with the given Json string.
	 * 
	 * @param valueJsonString	New Json string.
	 * @return	Updated SubmodelElement.
	 * @throws	If an exception occurs during the update.
	 */
	public SubmodelElement updateWithValueJsonString(String valueJsonString) throws IOException;
	
	 /**
	  * Updates the value of the SubmodelElement with the given string value.
	  * <p>
	  * While {@link #updateWithValueJsonString(String)}, which assumes the input string is Json,
	  * this method assumes the input string is the actual raw string.
	  * For example, to update the value of an int type Property to 10,
	  * {@link #updateWithValueJsonString(String)} requires a string {@code "10"},
	  * but this method can update it with string {@code 10}.
	  * <p>
	  * If the target {@link SubmodelElement} is not a {@link Property},
	  * this method takes a Json string as an argument, similar to {@link #updateWithValueJsonString(String)}.
	  *
	  * @param rawString    String to update.
	 * @return	Updated SubmodelElement.
	  * @throws    IOException If an exception occurs during the update process.
	  */
	public SubmodelElement updateWithRawString(String rawString) throws IOException;
	
	public void serialize(JsonGenerator gen) throws IOException;
	
	public String toJsonString() throws IOException;
	public JsonNode toJsonNode() throws IOException;
}
