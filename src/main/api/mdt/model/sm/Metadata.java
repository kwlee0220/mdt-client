package mdt.model.sm;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Accessors(prefix="m_")
@Getter
public class Metadata {
	private final String m_idShort;
	private final AasSubmodelElements m_modelType;
	@Nullable private final DataTypeDefXsd m_valueType;		// 'Property'가 아닌 경우는 null
	@Nullable private final String m_semanticId;			// 'semanticId'가 정의되지 않은 경우는 null
	@Nullable private final AasSubmodelElements m_typeValueListElement;	// 'SubmodelElementList' 가 아닌 경우는 null
	
	public Metadata(SubmodelElement sme) {
		m_idShort = sme.getIdShort();
		m_modelType = SubmodelUtils.getSubmodelElementType(sme);
		if ( m_modelType.equals(AasSubmodelElements.PROPERTY) ) {
			m_valueType = ((Property)sme).getValueType();
			Preconditions.checkState(m_valueType != null, "Property SubmodelElement should have 'valueType'");
		}
		else {
			m_valueType = null;
		}
		if ( m_modelType.equals(AasSubmodelElements.SUBMODEL_ELEMENT_LIST) ) {
			m_typeValueListElement = ((SubmodelElementList) sme).getTypeValueListElement();
			Preconditions.checkState(m_valueType != null, "SubmodelElementList should have 'typeValueListElement'");
		}
		else {
			m_typeValueListElement = null;
		}
		
		m_semanticId = SubmodelUtils.getSemanticIdStringOrNull(sme.getSemanticId());
	}
	
	@Override
	public String toString() {
		String prefix = "Metadata[idShort=" + m_idShort + ", modelType=" + m_modelType;
		if ( m_semanticId != null ) {
			prefix +=  ", semanticId=" + m_semanticId + "]";
		}
		else {
			prefix += "]";
		}
		return prefix;
	}
}
