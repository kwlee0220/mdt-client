package mdt.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@NoArgsConstructor
@JsonPropertyOrder({"name", "description", "valueReference"})
@JsonInclude(Include.NON_NULL)
public class VariableDescriptor {
//	public enum Kind { INPUT, INOUTPUT, OUTPUT };
	
	@JsonProperty("name") private String name;
//	@JsonProperty("kind") private Kind kind;
	@JsonProperty("description") private String description;
	
	@JsonProperty("valueReference") private ValueReferenceDescriptor valueReference;
	
	public static VariableDescriptor declare(String name, String description) {
		VariableDescriptor desc = new VariableDescriptor();
		desc.setName(name);
		desc.setDescription(description);
		
		return desc;
	}
	
	public static VariableDescriptor declare(String name) {
		return declare(name, null);
	}
	
	public static VariableDescriptor parseString(String name, String refExpr) {
		VariableDescriptor desc = new VariableDescriptor();
		desc.setName(name);
		desc.setDescription(null);
		desc.setValueReference(ValueReferenceDescriptor.parseString(refExpr));
		
		return desc;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", this.name, this.valueReference);
	}
}
