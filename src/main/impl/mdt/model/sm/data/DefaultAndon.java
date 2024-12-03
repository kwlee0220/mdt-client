package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
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
