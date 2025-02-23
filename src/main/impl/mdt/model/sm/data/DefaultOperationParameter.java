package mdt.model.sm.data;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperationParameter extends SubmodelElementCollectionEntity
										implements OperationParameter {
	@PropertyField(idShort="OperationID") private String operationId;
	@PropertyField(idShort="ParameterID") private String parameterId;
	@PropertyField(idShort="ParameterName") private String parameterName;
	@PropertyField(idShort="ParameterType") private String parameterType = "String";
	@PropertyField(idShort="ParameterGrade") private String parameterGrade;
	@PropertyField(idShort="ParameterUOMCode") private String parameterUOMCode;
	@PropertyField(idShort="LSL") private String LSL;
	@PropertyField(idShort="USL") private String USL;
	@PropertyField(idShort="PeriodicDataCollectionIndicator") private String periodicDataCollectionIndicator;
	@PropertyField(idShort="DataCollectionPeriod")private String dataCollectionPeriod;
	
	public DefaultOperationParameter() {
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
		
		DefaultOperationParameter other = (DefaultOperationParameter)obj;
		return Objects.equal(getEntityId(), other.getEntityId())
				&& Objects.equal(this.getParameterId(), other.getParameterId());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getEntityId(), getParameterId());
	}
	
	@Override
	public String toString() {
		return String.format("OperationParameter[%s.%s](%s)",
							this.operationId, this.parameterId, this.parameterType);
	}
}
