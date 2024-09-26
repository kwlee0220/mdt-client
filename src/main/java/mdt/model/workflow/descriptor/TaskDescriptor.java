package mdt.model.workflow.descriptor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.NameValue;
import mdt.model.workflow.descriptor.port.PortBinding;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class TaskDescriptor {
	@JsonProperty("id") private final String m_id;
	@JsonProperty("template") private final String m_template;
	@JsonProperty("dependencies") private final KeyedValueList<String,String> m_dependencies;
	@JsonProperty("arguments") private final KeyedValueList<String,NameValue> m_arguments;
	@JsonProperty("portBindings") private final KeyedValueList<String,PortBinding> m_portBindings;
	@JsonProperty("labels") private KeyedValueList<String,NameValue> m_labels;

	@JsonCreator
	public TaskDescriptor(@JsonProperty("id") String id,
							@JsonProperty("template") String template,
							@JsonProperty("dependencies") List<String> dependencies,
							@JsonProperty("arguments") List<NameValue> arguments,
							@JsonProperty("portBindings") List<PortBinding> portBindings,
							@JsonProperty("labels") List<NameValue> labels) {
		Preconditions.checkArgument(id != null);
		Preconditions.checkArgument(template != null);
		
		m_id = id;
		m_template = template;
		m_dependencies = (dependencies != null) ? KeyedValueList.from(dependencies, v -> v)
												: new KeyedValueList<>(v -> v);
		m_arguments = (arguments != null)
						? KeyedValueList.from(arguments, NameValue::getName)
						: new KeyedValueList<>(NameValue::getName);
		m_portBindings = (portBindings != null) ? KeyedValueList.from(portBindings, PortBinding::getName)
												: new KeyedValueList<>(PortBinding::getName);
		m_labels = (labels != null)
					? KeyedValueList.from(labels, NameValue::getName)
					: new KeyedValueList<>(NameValue::getName);
	}

	/**
	 * 태스크 인스턴스의 식별자를 반환한다.
	 * 
	 * @return	태스크 인스턴스 식별자.
	 */
	public String getId() {
		return m_id;
	}

	public String getTemplate() {
		return m_template;
	}

	public List<String> getDependencies() {
		return m_dependencies;
	}
	@JsonProperty("dependencies")
	public List<String> getDependenciesForJackson() {
		return ( m_dependencies.size() > 0 ) ? m_dependencies : null;
	}

	public List<NameValue> getArguments() {
		return m_arguments;
	}
	@JsonProperty("arguments")
	public List<NameValue> getArgumentsForJackson() {
		return ( m_arguments.size() > 0 ) ? m_arguments : null;
	}

	public List<PortBinding> getPortBindings() {
		return m_portBindings;
	}
	@JsonProperty("portBindings")
	public List<PortBinding> getPortBindingsForJackson() {
		return ( m_portBindings.size() > 0 ) ? m_portBindings : null;
	}

	public List<NameValue> getLabels() {
		return m_labels;
	}
	@JsonProperty("labels")
	private List<NameValue> getLabelsForJackson() {
		return ( m_labels.size() > 0 ) ? m_labels : null;
	}
	
	@Override
	public String toString() {
		String nvStr = FStream.from(m_arguments).map(NameValue::toString).join(',');
		return String.format("Task[id=%s, template=%s]: %s", m_id, m_template, nvStr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		TaskDescriptor other = (TaskDescriptor)obj;
		return Objects.equal(m_id, other.m_id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_id);
	}
}
