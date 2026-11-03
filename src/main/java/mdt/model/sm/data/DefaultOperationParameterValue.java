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
 * {@link OperationParameterValue} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 * 파라미터 값 자체는 idShort가 {@code ParameterValue}인 SubmodelElement로 저장된다.
 * SubmodelElementCollection의 idShort는 파라미터 식별자 ({@code ParameterID})이다.
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
	
	/**
	 * semantic id가 {@link ParameterValue#SEMANTIC_ID_REFERENCE}인 빈 {@code DefaultOperationParameterValue}
	 * 객체를 생성한다.
	 */
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
