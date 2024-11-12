package mdt.task;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import mdt.model.sm.SubmodelElementReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public final class Parameter {
	@JsonProperty("name") private final String m_name;
	@Nullable @JsonProperty("reference") private final SubmodelElementReference m_reference;
	@Nullable @JsonProperty("element") private final SubmodelElement m_element;
	
	public static Parameter of(String name, SubmodelElementReference ref) {
		return new Parameter(name, ref, null);
	}
	
	public static Parameter of(String name, SubmodelElement element) {
		return new Parameter(name, null, element);
	}

	@JsonCreator
	public Parameter(@JsonProperty("name") String name,
					@JsonProperty("reference") SubmodelElementReference reference,
					@JsonProperty("element") SubmodelElement element) {
		Preconditions.checkArgument((reference != null && element == null)
									|| (reference == null && element != null));
		
		m_name = name;
		m_reference = reference;
		m_element = element;
	}
	
	public String getName() {
		return m_name;
	}
	
	public SubmodelElementReference getReference() {
		return m_reference;
	}
	
	public SubmodelElement getElement() {
		return m_element;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_name);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		Parameter other = (Parameter)obj;
		return Objects.equal(m_name, other.m_name);
	}
	
	@Override
	public String toString() {
		String str = (m_reference != null) ? "" + m_reference : "" + m_element;
		return String.format("[%s] %s", m_name, str);
	}
}
