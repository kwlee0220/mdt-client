package mdt.task;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.InMemorySMEReference;
import mdt.model.sm.SubmodelElementReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public final class Parameter {
	@JsonProperty("name") private final String m_name;
	@JsonProperty("reference") private final SubmodelElementReference m_reference;
	
	public static Parameter of(String name, SubmodelElementReference ref) {
		return new Parameter(name, ref);
	}
	
	public static Parameter of(String name, SubmodelElement element) {
		return new Parameter(name, InMemorySMEReference.of(element));
	}

	@JsonCreator
	public Parameter(@JsonProperty("name") String name,
					@JsonProperty("reference") SubmodelElementReference reference) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(reference);
		
		m_name = name;
		m_reference = reference;
	}
	
	public String getName() {
		return m_name;
	}
	
	public SubmodelElementReference getReference() {
		Preconditions.checkState(m_reference != null);
		
		return m_reference;
	}
	
	public void activate(MDTInstanceManager manager) {
		if ( m_reference != null && m_reference instanceof MDTInstanceManagerAwareReference aware ) {
			aware.activate(manager);
		}
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
		return String.format("[%s] %s", m_name, m_reference);
	}
}
