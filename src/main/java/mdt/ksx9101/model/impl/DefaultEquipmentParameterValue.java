package mdt.ksx9101.model.impl;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.EquipmentParameterValue;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultEquipmentParameterValue extends SubmodelElementCollectionEntity
											implements EquipmentParameterValue {
	@PropertyField(idShort="EquipmentID") private String equipmentId;
	@PropertyField(idShort="ParameterID") private String parameterId;
	@PropertyField(idShort="ParameterValue", keepNullField=true) private String parameterValue;
	@PropertyField(idShort="EventDateTime") private String eventDateTime;
	@PropertyField(idShort="ValidationResultCode") private String validationResultCode;
	
	public DefaultEquipmentParameterValue() {
		super(null, "parameterId");
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		DefaultEquipmentParameterValue other = (DefaultEquipmentParameterValue)obj;
		return Objects.equal(getEntityId(), other.getEntityId())
				&& Objects.equal(this.getParameterId(), other.getParameterId())
				&& Objects.equal(this.getParameterValue(), other.getParameterValue());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getEntityId(), getParameterId(), getParameterValue());
	}
	
	@Override
	public String toString() {
		return String.format("%EquipmentParameterValue[%s.%s]=%s",
							this.equipmentId, this.parameterId, this.parameterValue);
	}
}
