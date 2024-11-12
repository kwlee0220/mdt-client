package mdt.workflow.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import lombok.Getter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@JsonInclude(Include.NON_NULL)
public class OptionDescriptor {
	private final String name;
	private final boolean required;
	private OptionValueType valueType;
	@Nullable private String description;
	
	@JsonCreator
	public OptionDescriptor(@JsonProperty("name") String name,
							@JsonProperty("required") boolean required,
							@JsonProperty("valueType") OptionValueType valueType,
							@JsonProperty("name") String description) {
		this.name = name;
		this.required = required;
		this.valueType = valueType;
		this.description = description;
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
		return Objects.equal(this.name, other.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.name);
	}
	
	@Override
	public String toString() {
		return String.format("OptionDescriptor(name=%s, type=%s, required=%s)%s",
								this.name, this.valueType, this.required);
	}
}
