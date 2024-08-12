package mdt.model.resource;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.stream.FStream;

import lombok.Getter;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class DefaultSubmodelElementCollection extends DefaultSubmodelElement
												implements SubmodelElementCollection {
	private List<SubmodelElement> value;
	
	public DefaultSubmodelElementCollection(List<SubmodelElement> value) {
		this.value = value;
	}
	
	public Map<String,Object> toCompactJsonObject() {
		Map<String,Object> jsonObj = super.toCompactJsonObject();

		jsonObj.put("modelType", "SubmodelElementCollection");
		List<Map<String,Object>> compactValue = FStream.from(value)
														.cast(DefaultSubmodelElement.class)
														.map(DefaultSubmodelElement::toCompactJsonObject)
														.toList();
		jsonObj.put("value", compactValue);
		
		return jsonObj;
	}
}
