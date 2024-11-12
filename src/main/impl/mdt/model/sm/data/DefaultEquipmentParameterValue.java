package mdt.model.sm.data;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;
import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMElementField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.tree.CustomNodeTransform;
import mdt.tree.sm.data.ParameterValueNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultEquipmentParameterValue extends SubmodelElementCollectionEntity
											implements EquipmentParameterValue {
	@PropertyField(idShort="EquipmentID") private String equipmentId;
	@PropertyField(idShort="ParameterID") private String parameterId;
	@SMElementField(idShort="ParameterValue") private SubmodelElement parameterValue;
	@PropertyField(idShort="EventDateTime") private String eventDateTime;
	@PropertyField(idShort="ValidationResultCode") private String validationResultCode;
	
	@Override
	public String getIdShort() {
		return this.parameterId;
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
		return String.format("EquipmentParameterValue[%s.%s]=%s",
							this.equipmentId, this.parameterId, this.parameterValue);
	}
}
