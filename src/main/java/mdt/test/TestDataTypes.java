package mdt.test;

import java.time.Instant;

import mdt.model.sm.value.PropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestDataTypes {
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
