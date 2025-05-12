package mdt.model.sm.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"name", "valueType"})
public final class NamedValueType {
	private final String m_name;
	private final String m_valueType;
	
	public NamedValueType(@JsonProperty("name") String name,
							@JsonProperty("valueType") String valueType) {
		m_name = name;
		m_valueType = valueType;
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getValueType() {
		return m_valueType;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", m_name, m_valueType);
	}
	
	private static final Pattern NAME_TYPE_PATTERN = Pattern.compile("(.*?) \\((.*?)\\)");
	public static NamedValueType parseString(String expr) {
        Matcher matcher = NAME_TYPE_PATTERN.matcher(expr);
        if (matcher.find()) {
        	return new NamedValueType(matcher.group(1).trim(), matcher.group(2).trim());
        }
        else {
            throw new IllegalArgumentException("Invalid NamedElementType string: " + expr);
        }
	}
}
