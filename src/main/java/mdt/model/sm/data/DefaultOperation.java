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
 * {@link Operation} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 * <p>
 * 이 밖에 {@code ProductionOrders}, {@code OperationParameters}, {@code OperationParameterValues}
 * SubmodelElementList를 각각 {@link ProductionOrder}, {@link Parameter}, {@link ParameterValue} 목록으로
 * 관리한다. SubmodelElementCollection의 idShort는 {@code Operation}이다.
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

	/**
	 * idShort가 {@code Operation}인 빈 {@code DefaultOperation} 객체를 생성한다.
	 */
	public DefaultOperation() {
		setIdShort("Operation");
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof Operation other) ) {
			return false;
		}

		return Objects.equal(getOperationId(), other.getOperationId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getOperationId());
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getOperationId());
	}
}
