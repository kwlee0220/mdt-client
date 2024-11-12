package mdt.workflow.model;

import javax.annotation.Nullable;

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
public class ParameterDescriptor implements Named {
	private final String m_name;
	@Nullable private String m_value;
	@Nullable private String m_description;

	@JsonCreator
	public ParameterDescriptor(@JsonProperty("name") String name,
								@JsonProperty("value") String value,
								@JsonProperty("description") String description) {
		Preconditions.checkArgument(name != null);
		
		m_name = name;
		m_value = value;
		m_description = description;
	}

	public ParameterDescriptor(String name, String description) {
		this(name, null, description);
	}

	@Override
	public String getName() {
		return m_name;
	}
	
	public String getValue() {
		return m_value;
	}

	public String getDescription() {
		return m_description;
	}
	
	public void setDescription(String desc) {
		m_description = desc;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		ParameterDescriptor other = (ParameterDescriptor)obj;
		return Objects.equal(m_name, other.m_name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_name);
	}
	
	@Override
	public String toString() {
		return String.format("ParameterDescriptor(name=%s)", m_name);
	}
}
