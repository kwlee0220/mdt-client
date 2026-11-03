package mdt.model.sm.data;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link EquipmentParameter} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 * SubmodelElementCollection의 idShort는 파라미터 식별자 ({@code ParameterID})이다.
 * {@code ParameterType}의 초기값은 {@code "String"}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultEquipmentParameter extends SubmodelElementCollectionEntity
										implements EquipmentParameter {
	@PropertyField(idShort="EquipmentID") private String equipmentId;
	@PropertyField(idShort="ParameterID") private String parameterId;
	@PropertyField(idShort="ParameterName") private String parameterName;
	@PropertyField(idShort="ParameterType") private String parameterType = "String";
	@PropertyField(idShort="ParameterGrade") private String parameterGrade;
	@PropertyField(idShort="ParameterUOMCode") private String parameterUOMCode;
	@PropertyField(idShort="LSL") private String LSL;
	@PropertyField(idShort="USL") private String USL;
	@PropertyField(idShort="PeriodicDataCollectionIndicator") private String periodicDataCollectionIndicator;
	@PropertyField(idShort="DataCollectionPeriod")private String dataCollectionPeriod;
	
	/**
	 * semantic id가 {@link Parameter#SEMANTIC_ID_REFERENCE}인 빈 {@code DefaultEquipmentParameter}
	 * 객체를 생성한다.
	 */
	public DefaultEquipmentParameter() {
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
		
		DefaultEquipmentParameter other = (DefaultEquipmentParameter)obj;
		return Objects.equal(getEntityId(), other.getEntityId())
				&& Objects.equal(this.getParameterId(), other.getParameterId());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getEntityId(), getParameterId());
	}
	
	@Override
	public String toString() {
		return String.format("EquipmentParameter[%s.%s](%s)",
							this.equipmentId, this.parameterId, this.parameterType);
	}
}
