package mdt.workflow.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"name", "value"})
public class Option {
	private final String m_name;
	private final String m_value;

	public Option(@JsonProperty("name") String name, @JsonProperty("value") String value) {
		Preconditions.checkArgument(name != null, "name is null");
		
		m_name = name;
		m_value = value;
	}

	public String getName() {
		return m_name;
	}
	
	public String getValue() {
		return m_value;
	}

	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()), getValue());
	}
	
	@Override
	public String toString() {
		return String.format("%s=%s", getName(), getValue());
	}
}
