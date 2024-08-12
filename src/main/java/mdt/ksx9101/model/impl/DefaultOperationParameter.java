package mdt.ksx9101.model.impl;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.OperationParameter;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperationParameter extends AbstractMDTSubmodelElementCollection
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
