package mdt.model.sm.data;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperation extends SubmodelElementCollectionEntity implements Operation {
	@PropertyField(idShort="OperationID") private String operationId;
	@PropertyField(idShort="OperationName") private String operationName;
	@PropertyField(idShort="OperationType") private String operationType;
	@PropertyField(idShort="UseIndicator") private String useIndicator;

	@SMListField(idShort="ProductionOrders", elementClass=DefaultProductionOrder.class)
	private List<ProductionOrder> productionOrders = Lists.newArrayList();
	
	@SMListField(idShort="OperationParameters", elementClass=DefaultOperationParameter.class)
	private List<Parameter> parameterList = Lists.newArrayList();
	@SMListField(idShort="OperationParameterValues", elementClass=DefaultOperationParameterValue.class)
	private List<ParameterValue> parameterValueList = Lists.newArrayList();

	public DefaultOperation() {
		setIdShort("Operation");
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( this == null || !(this instanceof Operation) ) {
			return false;
		}
		
		Operation other = (Operation)obj;
		return Objects.equal(getOperationId(), other.getOperationId());
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getOperationId());
	}
}
