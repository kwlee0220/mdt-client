package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * ElementValue는 AAS {@link SubmodelElement}에서 값에 해당하는 부분을 표현하는 인터페이스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ElementValue {
	/**
	 * ElementValue에 해당하는 자바 객체를 반환한다.
	 *
	 * @return 	자바 객체.
	 */
	public Object toValueObject();
	
	/**
	 * Polymorphic한 JSON 표현을 포함한 전체 JSON 문자열을 반환한다.
	 *
	 * @return	JSON 표현
	 * @throws IOException	JSON 변환에 실패한 경우
	 */
	public String toJsonString() throws IOException;
	
	/**
	 * Polymorphic한 JSON 표현을 제외한 값에 해당하는 부분의 JSON 문자열을 반환한다.
	 * 
	 * @return	값에 해당하는 부분의 JSON 문자열
	 */
	public String toValueJsonString();
	
	public String toDisplayString();
}
