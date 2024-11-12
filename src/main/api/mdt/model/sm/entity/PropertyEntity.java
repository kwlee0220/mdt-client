package mdt.model.sm.entity;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import com.google.common.base.Preconditions;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class PropertyEntity implements SubmodelElementEntity {
	private String m_idShort;
	private DataTypeDefXsd m_valueType;
	private String m_value;
	
	public String getIdShort() {
		return m_idShort;
	}
	
	public void setIdShort(String idShort) {
		m_idShort = idShort;
	}
	
	public String getValue() {
		return m_value;
	}
	
	public void setValue(String value) {
		m_value = value;
	}
	
	public DataTypeDefXsd getValueType() {
		return m_valueType;
	}
	
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

	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		Preconditions.checkArgument(sme != null);
		
		if ( sme instanceof Property prop ) {
			m_value = prop.getValue();
			m_valueType = prop.getValueType();
		}
		else {
			throw new IllegalArgumentException("Not Property, but=" + sme);
		}
	}

	@Override
	public void updateAasModel(SubmodelElement sme) {
		Preconditions.checkArgument(sme != null);

		if ( sme instanceof Property prop ) {
			prop.setValue(m_value);
			prop.setValueType(m_valueType);
		}
		else {
			throw new IllegalArgumentException("Not Property, but=" + sme);
		}
	}
}
