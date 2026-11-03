package mdt.model.sm;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.experimental.Accessors;

import utils.Preconditions;

import mdt.model.ReferenceUtils;

/**
 * SubmodelElement의 값을 제외한 메타데이터(idShort, 모델 타입, value 타입, semanticId 등)를
 * 담는 불변 클래스.
 * <p>
 * 각 필드는 Lombok {@code @Getter}가 생성하는 접근자({@code getIdShort()}, {@code getModelType()},
 * {@code getValueType()}, {@code getSemanticId()}, {@code getTypeValueListElement()})로 읽는다.
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
	
	/**
	 * 주어진 SubmodelElement로부터 메타데이터를 추출하여 객체를 생성한다.
	 * <p>
	 * 모델 타입은 {@link SubmodelElementTypeDescriptor}로 판별한다.
	 * {@code valueType}은 {@code Property}인 경우에만, {@code typeValueListElement}는
	 * {@code SubmodelElementList}인 경우에만 설정되고 그 외에는 {@code null}이다.
	 * {@code semanticId}가 정의되지 않은 경우 해당 필드는 {@code null}이다.
	 *
	 * @param sme	메타데이터를 추출할 SubmodelElement.
	 * @throws IllegalStateException	{@code Property}에 {@code valueType}이 없거나,
	 * 			{@code SubmodelElementList}에 {@code typeValueListElement}가 없는 경우.
	 */
	public Metadata(SubmodelElement sme) {
		m_idShort = sme.getIdShort();
		m_modelType = SubmodelElementTypeDescriptor.getTypeDescriptor(sme).getType(); 
		if ( m_modelType.equals(AasSubmodelElements.PROPERTY) ) {
			m_valueType = ((Property)sme).getValueType();
			Preconditions.checkState(m_valueType != null, "Property SubmodelElement should have 'valueType'");
		}
		else {
			m_valueType = null;
		}
		if ( m_modelType.equals(AasSubmodelElements.SUBMODEL_ELEMENT_LIST) ) {
			m_typeValueListElement = ((SubmodelElementList) sme).getTypeValueListElement();
			Preconditions.checkState(m_typeValueListElement != null,
									"SubmodelElementList should have 'typeValueListElement'");
		}
		else {
			m_typeValueListElement = null;
		}
		
		m_semanticId = ReferenceUtils.getSemanticIdStringOrNull(sme.getSemanticId());
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
