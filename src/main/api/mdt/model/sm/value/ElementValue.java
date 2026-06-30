package mdt.model.sm.value;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * ElementValue는 AAS {@link SubmodelElement}에서 값에 해당하는 부분을 표현하는 인터페이스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ElementValue {
	/**
	 * ElementValue에 해당하는 자바 객체를 반환한다.
	 * <p>
	 * 예를들어 {@link PropertyValue}의 경우에는 {@link String}, {@link Integer},
	 * {@link Double} 등의 자바 객체를 반환하고, {@link ElementCollectionValue}의 경우에는
	 * {@link Map} 객체를 반환한다.
	 *
	 * @return 	자바 객체.
	 */
	public Object toValueObject();
	
	/**
	 * Polymorphic한 JSON 표현을 포함한 전체 JSON 문자열을 반환한다.
	 * <p>
	 * 반환된 JSON에는 ElementValue에 해당하는 값에 대한 JSON 표현과 함께,
	 * ElementValue의 타입을 나타내는 정보가 포함된다.
	 *
	 * @return	JSON 표현
	 */
	public String toJsonString();
	
	public JsonNode toJsonNode();
	
	/**
	 * Polymorphic한 JSON 표현을 제외한 값에 해당하는 부분의 JSON 문자열을 반환한다.
	 * <p>
	 * {@link #toValueObject()} 메소드에서 반환되는 자바 객체를 JSON으로 변환한 문자열을 반환한다.
	 * 
	 * @return	값에 해당하는 부분의 JSON 문자열
	 */
	public String toValueJsonString();
	
	public JsonNode toValueJsonNode();
	
	/**
	 * ElementValue에 해당하는 값을 디버그 등의 목적으로 사람이 읽기 편한 문자열로 반환한다.
	 * 
	 * @return	사람이 읽을 수 있는 문자열
	 */
	public String toDisplayString();
}
