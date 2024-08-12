package mdt.model.resource;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
@NoArgsConstructor
public class DefaultProperty extends DefaultSubmodelElement implements Property {
	private DataTypeDefXsd valueType;
	private Reference valueId;
	private String value;
	
	public Map<String,Object> toCompactJsonObject() {
		Map<String,Object> jsonObj = super.toCompactJsonObject();

		jsonObj.put("modelType", "Property");
		jsonObj.put("valueType", valueType);
		if ( this.valueId != null ) {
			jsonObj.put("valueId", valueId);
		}
		if ( this.value != null ) {
			jsonObj.put("value", value);
		}
		
		return jsonObj;
	}
}
