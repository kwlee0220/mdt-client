package mdt.model.sm.entity;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import utils.Preconditions;


/**
 * AAS {@code Property} SubmodelElement의 idShort, value 타입, 값 문자열을 담는 엔티티.
 * <p>
 * 주로 {@link SMListField}가 붙은 기본 타입 리스트 필드를 변환할 때 원소 Property를 읽는 데 사용된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class PropertyEntity implements SubmodelElementEntity {
	private String m_idShort;
	private DataTypeDefXsd m_valueType;
	private String m_value;
	
	/**
	 * Property의 idShort를 반환한다.
	 *
	 * @return	idShort.
	 */
	public String getIdShort() {
		return m_idShort;
	}
	
	/**
	 * Property의 idShort를 설정한다.
	 *
	 * @param idShort	idShort.
	 */
	public void setIdShort(String idShort) {
		m_idShort = idShort;
	}
	
	/**
	 * Property의 값 문자열을 반환한다.
	 *
	 * @return	값 문자열.
	 */
	public String getValue() {
		return m_value;
	}
	
	/**
	 * Property의 값 문자열을 설정한다.
	 *
	 * @param value	값 문자열.
	 */
	public void setValue(String value) {
		m_value = value;
	}
	
	/**
	 * Property의 value 타입을 반환한다.
	 *
	 * @return	value 타입.
	 */
	public DataTypeDefXsd getValueType() {
		return m_valueType;
	}
	
	/**
	 * Property의 value 타입을 설정한다.
	 *
	 * @param valueType	value 타입.
	 */
	public void setValueType(DataTypeDefXsd valueType) {
		m_valueType = valueType;
	}
	
	@Override
	public Property newSubmodelElement() {
		DefaultProperty smc = new DefaultProperty.Builder()
													.idShort(getIdShort())
													.value(m_value)
													.valueType(m_valueType)
													.build();
		updateAasModel(smc);
		return smc;
	}

	/**
	 * 주어진 Property의 값과 value 타입으로 이 엔티티를 갱신한다. idShort는 변경하지 않는다.
	 *
	 * @param sme	읽어 들일 Property.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 Property가 아닌 경우.
	 */
	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		Preconditions.checkNotNullArgument(sme, "sme is null");
		
		if ( sme instanceof Property prop ) {
			m_value = prop.getValue();
			m_valueType = prop.getValueType();
		}
		else {
			throw new IllegalArgumentException("Not Property, but=" + sme);
		}
	}

	/**
	 * 이 엔티티의 값과 value 타입을 주어진 Property에 설정한다. idShort는 변경하지 않는다.
	 *
	 * @param sme	갱신할 Property.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 Property가 아닌 경우.
	 */
	@Override
	public void updateAasModel(SubmodelElement sme) {
		Preconditions.checkNotNullArgument(sme, "sme is null");

		if ( sme instanceof Property prop ) {
			prop.setValue(m_value);
			prop.setValueType(m_valueType);
		}
		else {
			throw new IllegalArgumentException("Not Property, but=" + sme);
		}
	}
}
