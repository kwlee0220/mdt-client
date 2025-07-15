package mdt.model.sm.variable;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.sm.value.ElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = Variables.Serializer.class)
@JsonDeserialize(using = Variables.Deserializer.class)
public interface Variable {
	/**
	 * 변수의 이름을 반환한다.
	 *
	 * @return	변수 이름
	 */
	public String getName();
	
	/**
	 * 변수의 설명을 반환한다.
	 *
	 * @return 변수 설명
	 */
	public String getDescription();
	
	/**
	 * 변수에 저장된 {@link SubmodelElement}를 반환한다.
	 *
	 * @return SubmodelElement 객체
	 * @throws IOException	변수 값을 읽는 과정에서 오류가 발생한 경우.
	 */
	public SubmodelElement read() throws IOException;

	/**
	 * 변수에 저장된 {@link SubmodelElement}의 값 부분 {@link ElementValue} 영역을 반환한다.
	 *
	 * @return ElementValue 객체
	 * @throws IOException 변수 값을 읽는 과정에서 오류가 발생한 경우.
	 */
	public ElementValue readValue() throws IOException;
	
	/**
	 * 주어진 {@link SubmodelElement}로 변수의 값을 갱신한다.
	 *
	 * @param sme	새 값.
	 * @throws IOException	변수 값을 갱신하는 과정에서 오류가 발생한 경우.
	 */
	public void update(SubmodelElement sme) throws IOException;
	
	/**
	 * 주어진 {@link ElementValue}로 변수의 값을 갱신한다.
	 *
	 * @param value	새 값.
	 * @throws IOException	변수 값을 갱신하는 과정에서 오류가 발생한 경우.
	 */
	public void updateValue(ElementValue value) throws IOException;
	
	/**
	 * JSON 문자열로 표현된 SubmodelElement의 값으로 변수를 갱신한다.
	 *
	 * @param valueJsonString 새 값의 JSON 문자열 표현.
	 * @throws IOException 변수 값을 갱신하는 과정에서 오류가 발생한 경우.
	 */
	public void updateWithValueJsonString(String valueJsonString) throws IOException;

	public String getSerializationType();
	public void serializeFields(JsonGenerator gen) throws IOException;
}
