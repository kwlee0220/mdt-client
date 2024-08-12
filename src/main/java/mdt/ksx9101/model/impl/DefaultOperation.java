package mdt.ksx9101.model.impl;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Operation;
import mdt.ksx9101.model.Parameter;
import mdt.ksx9101.model.ParameterValue;
import mdt.ksx9101.model.ProductionOrder;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;
import mdt.model.SMLField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperation extends AbstractMDTSubmodelElementCollection implements Operation {
	@PropertyField(idShort="OperationID") private String operationId;
	@PropertyField(idShort="OperationName") private String operationName;
	@PropertyField(idShort="OperationType") private String operationType;
	@PropertyField(idShort="UseIndicator") private String useIndicator;

	@SMLField(idShort="ProductionOrders", elementClass=DefaultProductionOrder.class)
	private List<ProductionOrder> productionOrders = Lists.newArrayList();
	
	@SMLField(idShort="OperationParameters", elementClass=DefaultOperationParameter.class)
	private List<Parameter> parameters = Lists.newArrayList();
	@SMLField(idShort="OperationParameterValues", elementClass=DefaultOperationParameterValue.class)
	private List<ParameterValue> parameterValues = Lists.newArrayList();
	
	public DefaultOperation() {
		super("Operation", null);
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
