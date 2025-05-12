package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"name", "valueType"})
public class DefaultAssetParameterDescriptor implements MDTParameterDescriptor {
	private final String m_name;
	private final String m_valueType;
	
	public DefaultAssetParameterDescriptor(@JsonProperty("name") String name,
											@JsonProperty("valueType") String valueType) {
		Preconditions.checkArgument(name != null, "null id");
		Preconditions.checkArgument(valueType != null, "null type");
		
		m_name = name;
		m_valueType = valueType;
	}
	
	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public String getValueType() {
		return m_valueType;
	}
}
