package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import utils.func.FOption;

import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ElementReferences.Serializer.class)
@JsonDeserialize(using = ElementReferences.Deserializer.class)
public interface ElementReference {
	/**
	 * 참조가 가리키는 {@link SubmodelElement}을 반환한다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	public SubmodelElement read() throws IOException;

	/**
	 * 참조가 가리키는 {@link SubmodelElement}을 읽어서 그 중 값에 해당하는 부분만 반환한다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	public default ElementValue readValue() throws IOException {
		return FOption.map(read(), ElementValues::getValue);
	}
	
	/**
	 * 참조가 가리키는 SubmodelElement을 주어진 SubmodelElement으로 갱신한다. 
	 *
	 * @param newElm	갱신할 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void write(SubmodelElement newElm) throws IOException;

	/**
	 * 참조가 가리키는 SubmodelElement의 값 부분을 주어진 SubmodelElement의 값으로 갱신한다.
	 *
	 * @param sme	갱신할 값을 포함한 SubmodelElement 객체.
	 * @return    갱신된 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void update(SubmodelElement sme) throws IOException;

	/**
	 * 참조가 가리키는 SubmodelElement의 값 부분을 주어진 ElementValue으로 갱신한다.
	 *
	 * @param smev    갱신할 값.
	 * @return    갱신된 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateValue(ElementValue smev) throws IOException;

	/**
	 * 주어진 Json 문자열을 이용하여 참조가 가리키는 SubmodelElement의 값 부분을 갱신한다.
	 * 
	 * @param valueJsonString    New Json string
	 * @return    갱신된 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateValue(String valueJsonString) throws IOException;

	public String toStringExpr();

	public String getSerializationType();
	
	/**
	 * JsonGenerator를 이용하여 참조가 가리키는 SubmodelElement의 값을 Json으로 직렬화한다.
	 * 
	 * @param gen	Json 정렬화 과정에서 사용할 JsonGenerator.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public void serializeFields(JsonGenerator gen) throws IOException;
	
	/**
	 * 참조가 가리키는 SubmodelElement의 값을 Json 문자열로 직렬화한다.
	 * 
	 * @return	Json 문자열.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public String toJsonString() throws IOException;
	
	/**
	 * 참조가 가리키는 SubmodelElement의 값을 JsonNode로 직렬화한다.
	 * 
	 * @return	JsonNode 객체.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public JsonNode toJsonNode() throws IOException;
}
