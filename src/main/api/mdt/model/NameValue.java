package mdt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import utils.Named;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public final class NameValue implements Named {
	private final String m_name;
	private final String m_value;
	
	public static NameValue of(String name, String value) {
		return new NameValue(name, value);
	}

	@JsonCreator
	public NameValue(@JsonProperty("name") String name,
					@JsonProperty("value") String value) {
		Preconditions.checkArgument(name != null);
		
		this.m_name = name;
		this.m_value = value;
	}
	
	@Override
	public String getName() {
		return m_name;
	}

	public String getValue() {
		return m_value;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		NameValue other = (NameValue)obj;
		return Objects.equal(m_name, other.m_name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_name);
	}
	
	@Override
	public String toString() {
		String vstr = (m_value != null) ? String.format("=%s", m_value) : "";
		return String.format("%s%s", m_name, vstr);
	}
}