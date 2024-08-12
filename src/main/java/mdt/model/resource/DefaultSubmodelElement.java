package mdt.model.resource;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class DefaultSubmodelElement implements SubmodelElement {
	private String idShort;
	private List<LangStringNameType> displayName = Lists.newArrayList();
	private List<LangStringTextType> description = Lists.newArrayList();
	private String category;
	private Reference semanticId;
	private List<Reference> supplementalSemanticIds = Lists.newArrayList();
	private List<Qualifier> qualifiers = Lists.newArrayList();
	private List<Extension> extensions = Lists.newArrayList();
	private List<EmbeddedDataSpecification> embeddedDataSpecifications = Lists.newArrayList();
	
	public Map<String,Object> toCompactJsonObject() {
		Map<String,Object> jsonObj = Maps.newHashMap();
		
		jsonObj.put("modelType", "SubmodelElement");
		jsonObj.put("idShort", idShort);
		if ( this.displayName != null && this.displayName.size() > 0 ) {
			jsonObj.put("displayName", displayName);
		}
		if ( this.description != null && this.description.size() > 0 ) {
			jsonObj.put("description", description);
		}
		if ( this.category != null ) {
			jsonObj.put("category", category);
		}
		if ( this.semanticId != null ) {
			jsonObj.put("semanticId", semanticId);
		}
		if ( this.supplementalSemanticIds != null && this.supplementalSemanticIds.size() > 0 ) {
			jsonObj.put("supplementalSemanticIds", supplementalSemanticIds);
		}
		if ( this.qualifiers != null && this.qualifiers.size() > 0 ) {
			jsonObj.put("qualifiers", qualifiers);
		}
		if ( this.semanticId != null ) {
			jsonObj.put("semanticId", semanticId);
		}
		if ( this.extensions != null && this.extensions.size() > 0 ) {
			jsonObj.put("extensions", extensions);
		}
		if ( this.embeddedDataSpecifications != null && this.embeddedDataSpecifications.size() > 0 ) {
			jsonObj.put("embeddedDataSpecifications", embeddedDataSpecifications);
		}
		
		return jsonObj;
	}
}
