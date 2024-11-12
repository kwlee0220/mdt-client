package mdt.model.sm.data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Repair {
	public Long getRepairID();
	public void setRepairID(Long repairID);

	public String getGroupID();
	public void setGroupID(String groupID);

	public String getDefectRegOperationID();
	public void setDefectRegOperationID(String defectRegOperationID);

	public String getDefectRegEquipmentID();
	public void setDefectRegEquipmentID(String defectRegEquipmentID);

	public String getDefectRegDateTime();
	public void setDefectRegDateTime(String defectRegDateTime);

	public String getRepairDateTime();
	public void setRepairDateTime(String repairDateTime);

	public String getProductionItemSerialNO();
	public void setProductionItemSerialNO(String productionItemSerialNO);

	public String getDetectedProcess();
	public void setDetectedProcess(String detectedProcess);

	public String getInitialDefectLevel1();
	public void setInitialDefectLevel1(String initialDefectLevel1);

	public String getInitialDefectLevel2();
	public void setInitialDefectLevel2(String initialDefectLevel2);

	public String getInitialDefectLevel3();
	public void setInitialDefectLevel3(String initialDefectLevel3);
}
