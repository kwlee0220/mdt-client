package mdt.model.sm.data;

import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMElementField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperationParameterValue extends SubmodelElementCollectionEntity
											implements OperationParameterValue {
	@PropertyField(idShort="OperationID") private String operationId;
	@PropertyField(idShort="ParameterID") private String parameterId;
	@SMElementField(idShort="ParameterValue") private SubmodelElement parameterValue;
	@PropertyField(idShort="EventDateTime") private Instant eventDateTime;
	@PropertyField(idShort="ValidationResultCode") private String validationResultCode;
	
	public DefaultOperationParameterValue() {
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
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
		
		DefaultOperationParameterValue other = (DefaultOperationParameterValue)obj;
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
		return String.format("OperationParameterValue[%s.%s]=%s",
							this.operationId, this.parameterId, this.parameterValue);
	}
}
