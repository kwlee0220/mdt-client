package mdt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.KeyValue;
import utils.Named;


/**
 * 이름과 문자열 값의 쌍을 표현하는 불변 클래스이다.
 * <p>
 * {@link KeyValue}의 key를 이름으로, value를 값으로 사용하며, JSON으로는
 * {@code {"name": ..., "value": ...}} 형태로 직렬화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class NameValue extends KeyValue<String,String> implements Named {
	/**
	 * 주어진 이름과 값으로 {@link NameValue} 객체를 생성한다.
	 *
	 * @param name	이름.
	 * @param value	값.
	 * @return	생성된 {@link NameValue} 객체.
	 */
	public static NameValue of(String name, String value) {
		return new NameValue(name, value);
	}

	/**
	 * 주어진 이름과 값으로 {@link NameValue} 객체를 생성한다.
	 *
	 * @param name	이름.
	 * @param value	값.
	 */
	@JsonCreator
	public NameValue(@JsonProperty("name") String name,
					@JsonProperty("value") String value) {
		super(name, value);
	}
	
	@Override
	public String getName() {
		return key();
	}

	/**
	 * 값을 반환한다.
	 *
	 * @return	값.
	 */
	public String getValue() {
		return value();
	}
}