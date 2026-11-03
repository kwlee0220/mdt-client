package mdt.model.sm.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 이름과 값 타입(예: {@code xs:int})의 쌍을 표현하는 클래스.
 * <p>
 * JSON으로는 {@code {"name": ..., "valueType": ...}} 형태로 직렬화되며, 문자열 표현은
 * {@code "<name> (<valueType>)"} 형식이다({@link #toString()}, {@link #parseString(String)}).
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"name", "valueType"})
public final class NamedValueType {
	private final String m_name;
	private final String m_valueType;
	
	/**
	 * 주어진 이름과 값 타입으로 {@code NamedValueType} 객체를 생성한다.
	 *
	 * @param name		이름.
	 * @param valueType	값 타입 문자열(예: {@code xs:int}).
	 */
	public NamedValueType(@JsonProperty("name") String name,
							@JsonProperty("valueType") String valueType) {
		m_name = name;
		m_valueType = valueType;
	}
	
	/**
	 * 이름을 반환한다.
	 *
	 * @return	이름.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * 값 타입 문자열을 반환한다.
	 *
	 * @return	값 타입 문자열.
	 */
	public String getValueType() {
		return m_valueType;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", m_name, m_valueType);
	}
	
	private static final Pattern NAME_TYPE_PATTERN = Pattern.compile("(.*?) \\((.*?)\\)");
	/**
	 * {@code "<name> (<valueType>)"} 형식의 문자열을 파싱하여 {@code NamedValueType} 객체를 생성한다.
	 * <p>
	 * 입력 문자열과 추출된 이름/값 타입의 앞뒤 공백은 제거된다. {@link #toString()}의 역변환이다.
	 *
	 * @param expr	파싱할 문자열(예: {@code "speed (xs:double)"}).
	 * @return	파싱된 {@code NamedValueType} 객체.
	 * @throws IllegalArgumentException	{@code expr}이 형식에 맞지 않는 경우.
	 */
	public static NamedValueType parseString(String expr) {
        Matcher matcher = NAME_TYPE_PATTERN.matcher(expr.trim());
        if (matcher.matches()) {
        	return new NamedValueType(matcher.group(1).trim(), matcher.group(2).trim());
        }
        else {
            throw new IllegalArgumentException("Invalid NamedValueType string: " + expr);
        }
	}
}
