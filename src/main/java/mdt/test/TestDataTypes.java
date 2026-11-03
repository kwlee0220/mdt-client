package mdt.test;

import java.time.Instant;

import mdt.model.sm.value.PropertyValue;

/**
 * {@code PropertyValue}의 DATE_TIME/INTEGER/STRING 값에 대한 JSON 직렬화 결과를 출력하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestDataTypes {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		Instant now = Instant.now();
		
		var value = PropertyValue.DATE_TIME(now);
		System.out.println(value.toJsonString());
		System.out.println(value.toValueJsonString());
		System.out.println(value.toString());
		
		var value2 = PropertyValue.INTEGER(10);
		System.out.println(value2.toJsonString());	
		System.out.println(value2.toValueJsonString());
		System.out.println(value2.toString());
		
		var value3 = PropertyValue.STRING("Hello");
		System.out.println(value3.toJsonString());
		System.out.println(value3.toValueJsonString());
		System.out.println(value3.toString());
	}
}
