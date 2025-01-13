package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public abstract class DefaultParameterInfo implements ParameterInfo {
	@JsonProperty("id") private final String m_id;
	@JsonProperty("type") private final String m_type;
	
	protected DefaultParameterInfo(String id, String type) {
		Preconditions.checkArgument(id != null, "Null id");
		Preconditions.checkArgument(type != null, "Null type");
		
        m_id = id;
        m_type = type;
	}

    public String getId() {
        return m_id;
    }

    public String getType() {
        return m_type;
    }
}
