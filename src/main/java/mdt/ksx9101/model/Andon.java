package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Andon {
	public Long getAndonID();
	public void setAndonID(Long andonID);

	public String getGroupID();
	public void setGroupID(String groupID);

	public String getOperationID();
	public void setOperationID(String operationID);

	public String getStartDateTime();
	public void setStartDateTime(String startDateTime);

	public String getCauseNO();
	public void setCauseNO(String causeNO);

	public String getCauseName();
	public void setCauseName(String causeName);

	public String getLineStopType();
	public void setLineStopType(String lineStopType);

	public String getLineStopName();
	public void setLineStopName(String lineStopName);

	public String getTypeCode();
	public void setTypeCode(String typeCode);

	public String getTypeName();
	public void setTypeName(String typeName);

	public String getEndDateTime();
	public void setEndDateTime(String endDateTime);

//	public String getStopDateTime();
//	public void setStopDateTime(String stopDateTime);
}
