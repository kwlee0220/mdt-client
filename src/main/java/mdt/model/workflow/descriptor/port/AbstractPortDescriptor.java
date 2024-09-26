package mdt.model.workflow.descriptor.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public abstract class AbstractPortDescriptor implements PortDescriptor {
	@JsonProperty("name") private String m_name;
	@JsonProperty("description") private String m_description;
	@JsonProperty("portType") private PortType m_type;
	@JsonProperty("valueOnly") private boolean m_valueOnly = false;
	
	protected AbstractPortDescriptor() { }
	protected AbstractPortDescriptor(String name, String description, PortType type, boolean valueOnly) {
		m_name = name;
		m_description = description;
		m_type = type;
		m_valueOnly = valueOnly;
	}
	
	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void setName(String name) {
		m_name = name;
	}

	@Override
	public String getDescription() {
		return m_description;
	}

	@Override
	public void setDescription(String desc) {
		m_description = desc;
	}

	@Override
	public PortType getPortType() {
		return m_type;
	}
	@JsonProperty("portType")
	private String getPortTypeForJackson() {
		return m_type.getId();
	}
	
	public void setPortType(PortType type) {
		m_type = type;
	}
	@JsonProperty("portType")
	private void setPortTypeForJackson(String id) {
		m_type = PortType.fromId(id);
	}

	@Override
	public Boolean isValueOnly() {
		return m_valueOnly;
	}
	
	public void setValueOnly(Boolean vonly) {
		m_valueOnly = vonly;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		PortDescriptor other = (PortDescriptor)obj;
		return Objects.equal(m_name, other.getName());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_name);
	}

	
	@Override
	public String toString() {
		return toStringExpr();
	}
}
