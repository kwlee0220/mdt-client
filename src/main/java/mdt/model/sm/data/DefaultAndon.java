package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link Andon} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultAndon extends SubmodelElementCollectionEntity implements Andon {
	@PropertyField(idShort="AndonID") private Long andonID;
	@PropertyField(idShort="GroupID") private String groupID;
	@PropertyField(idShort="OperationID") private String operationID;
	@PropertyField(idShort="StartDateTime") private String startDateTime;
	@PropertyField(idShort="StopDateTime") private String stopDateTime;
	@PropertyField(idShort="EndDateTime") private String endDateTime;
	@PropertyField(idShort="CauseNO") private String causeNO;
	@PropertyField(idShort="CauseName") private String causeName;
	@PropertyField(idShort="LineStopType") private String lineStopType;
	@PropertyField(idShort="LineStopName") private String lineStopName;
	@PropertyField(idShort="TypeCode") private String typeCode;
	@PropertyField(idShort="TypeName") private String typeName;
	
	@Override
	public String getIdShort() {
		return "" + this.andonID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getAndonID());
	}
}
