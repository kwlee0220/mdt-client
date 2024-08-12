package mdt.model.resource;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.collect.Lists;

import utils.stream.FStream;

import lombok.Getter;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class DefaultSubmodelElementList extends DefaultSubmodelElement
												implements SubmodelElementList {
	private boolean orderRelevant;
	private Reference semanticIdListElement;
	private AasSubmodelElements typeValueListElement;
	private DataTypeDefXsd valueTypeListElement;
	private List<SubmodelElement> value = Lists.newArrayList();
	
	public Map<String,Object> toCompactJsonObject() {
		Map<String,Object> jsonObj = super.toCompactJsonObject();

		jsonObj.put("modelType", "SubmodelElementList");
		jsonObj.put("orderRelevant", orderRelevant);
		if ( this.semanticIdListElement != null ) {
			jsonObj.put("semanticIdListElement", semanticIdListElement);
		}
		jsonObj.put("typeValueListElement", typeValueListElement);
		if ( this.valueTypeListElement != null ) {
			jsonObj.put("valueTypeListElement", valueTypeListElement);
		}
		List<Map<String,Object>> compactValue = FStream.from(value)
														.cast(DefaultSubmodelElement.class)
														.map(DefaultSubmodelElement::toCompactJsonObject)
														.toList();
		jsonObj.put("value", compactValue);
		
		return jsonObj;
	}

	@Override
	public boolean getOrderRelevant() {
		return this.orderRelevant;
	}
}
