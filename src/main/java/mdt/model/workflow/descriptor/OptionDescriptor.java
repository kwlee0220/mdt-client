package mdt.model.workflow.descriptor;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import utils.Named;
import utils.func.FOption;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class OptionDescriptor implements Named {
	private final String m_name;
	private boolean m_required;
	@Nullable private String m_description;
	@Nullable private String m_value;

	@JsonCreator
	public OptionDescriptor(@JsonProperty("name") String name,
							@JsonProperty("required") Boolean required,
							@JsonProperty("description") String description,
							@JsonProperty("value") String value) {
		Preconditions.checkArgument(name != null);
		
		this.m_name = name;
		this.m_required = FOption.getOrElse(required, true);
		this.m_description = description;
		this.m_value = value;
	}

	public OptionDescriptor(String name) {
		this(name, true, null, null);
	}

	@Override
	public String getName() {
		return m_name;
	}

	public boolean isRequired() {
		return m_required;
	}
	public void setRequired(boolean required) {
		m_required = required;
	}

	public String getDescription() {
		return m_description;
	}
	public void setDescription(String desc) {
		m_description = desc;
	}

	public String getValue() {
		return m_value;
	}
	public void setValue(String value) {
		m_value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		OptionDescriptor other = (OptionDescriptor)obj;
		return Objects.equal(m_name, other.m_name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_name);
	}
	
	@Override
	public String toString() {
		String vstr = (m_value != null) ? String.format(", value=%s", m_value) : "";
		return String.format("OptionDescriptor(name=%s, required=%s)%s", m_name, m_required, vstr);
	}
}
