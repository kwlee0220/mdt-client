package mdt.ksx9101.model.impl;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.OperationParameterValue;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperationParameterValue extends AbstractMDTSubmodelElementCollection
											implements OperationParameterValue {
	@PropertyField(idShort="OperationID") private String operationId;
	@PropertyField(idShort="ParameterID") private String parameterId;
	@PropertyField(idShort="ParameterValue", keepNullField=true) private String parameterValue;
	@PropertyField(idShort="EventDateTime") private String eventDateTime;
	@PropertyField(idShort="ValidationResultCode") private String validationResultCode;
	
	public DefaultOperationParameterValue() {
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
		return String.format("%OperationParameterValue[%s.%s]=%s",
							this.operationId, this.parameterId, this.parameterValue);
	}
}
