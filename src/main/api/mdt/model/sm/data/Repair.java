package mdt.model.sm.data;

/**
 * {@code Repair}는 불량품 수리 이력 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Repair {
	/**
	 * 수리 식별자({@code RepairID})를 반환한다.
	 *
	 * @return	수리 식별자.
	 */
	public Long getRepairID();
	/**
	 * 수리 식별자({@code RepairID})를 설정한다.
	 *
	 * @param repairID	설정할 수리 식별자.
	 */
	public void setRepairID(Long repairID);

	/**
	 * 그룹 식별자({@code GroupID})를 반환한다.
	 *
	 * @return	그룹 식별자.
	 */
	public String getGroupID();
	/**
	 * 그룹 식별자({@code GroupID})를 설정한다.
	 *
	 * @param groupID	설정할 그룹 식별자.
	 */
	public void setGroupID(String groupID);

	/**
	 * 불량을 등록한 공정의 식별자({@code DefectRegOperationID})를 반환한다.
	 *
	 * @return	불량을 등록한 공정의 식별자.
	 */
	public String getDefectRegOperationID();
	/**
	 * 불량을 등록한 공정의 식별자({@code DefectRegOperationID})를 설정한다.
	 *
	 * @param defectRegOperationID	설정할 불량을 등록한 공정의 식별자.
	 */
	public void setDefectRegOperationID(String defectRegOperationID);

	/**
	 * 불량을 등록한 설비의 식별자({@code DefectRegEquipmentID})를 반환한다.
	 *
	 * @return	불량을 등록한 설비의 식별자.
	 */
	public String getDefectRegEquipmentID();
	/**
	 * 불량을 등록한 설비의 식별자({@code DefectRegEquipmentID})를 설정한다.
	 *
	 * @param defectRegEquipmentID	설정할 불량을 등록한 설비의 식별자.
	 */
	public void setDefectRegEquipmentID(String defectRegEquipmentID);

	/**
	 * 불량 등록 일시({@code DefectRegDateTime})를 반환한다.
	 *
	 * @return	불량 등록 일시.
	 */
	public String getDefectRegDateTime();
	/**
	 * 불량 등록 일시({@code DefectRegDateTime})를 설정한다.
	 *
	 * @param defectRegDateTime	설정할 불량 등록 일시.
	 */
	public void setDefectRegDateTime(String defectRegDateTime);

	/**
	 * 수리 일시({@code RepairDateTime})를 반환한다.
	 *
	 * @return	수리 일시.
	 */
	public String getRepairDateTime();
	/**
	 * 수리 일시({@code RepairDateTime})를 설정한다.
	 *
	 * @param repairDateTime	설정할 수리 일시.
	 */
	public void setRepairDateTime(String repairDateTime);

	/**
	 * 생산품 일련번호({@code ProductionItemSerialNO})를 반환한다.
	 *
	 * @return	생산품 일련번호.
	 */
	public String getProductionItemSerialNO();
	/**
	 * 생산품 일련번호({@code ProductionItemSerialNO})를 설정한다.
	 *
	 * @param productionItemSerialNO	설정할 생산품 일련번호.
	 */
	public void setProductionItemSerialNO(String productionItemSerialNO);

	/**
	 * 불량이 검출된 공정({@code DetectedProcess})을 반환한다.
	 *
	 * @return	불량이 검출된 공정.
	 */
	public String getDetectedProcess();
	/**
	 * 불량이 검출된 공정({@code DetectedProcess})을 설정한다.
	 *
	 * @param detectedProcess	설정할 불량이 검출된 공정.
	 */
	public void setDetectedProcess(String detectedProcess);

	/**
	 * 초기 불량 분류 (1단계)({@code InitialDefectLevel1})를 반환한다.
	 *
	 * @return	초기 불량 분류 (1단계).
	 */
	public String getInitialDefectLevel1();
	/**
	 * 초기 불량 분류 (1단계)({@code InitialDefectLevel1})를 설정한다.
	 *
	 * @param initialDefectLevel1	설정할 초기 불량 분류 (1단계).
	 */
	public void setInitialDefectLevel1(String initialDefectLevel1);

	/**
	 * 초기 불량 분류 (2단계)({@code InitialDefectLevel2})를 반환한다.
	 *
	 * @return	초기 불량 분류 (2단계).
	 */
	public String getInitialDefectLevel2();
	/**
	 * 초기 불량 분류 (2단계)({@code InitialDefectLevel2})를 설정한다.
	 *
	 * @param initialDefectLevel2	설정할 초기 불량 분류 (2단계).
	 */
	public void setInitialDefectLevel2(String initialDefectLevel2);

	/**
	 * 초기 불량 분류 (3단계)({@code InitialDefectLevel3})를 반환한다.
	 *
	 * @return	초기 불량 분류 (3단계).
	 */
	public String getInitialDefectLevel3();
	/**
	 * 초기 불량 분류 (3단계)({@code InitialDefectLevel3})를 설정한다.
	 *
	 * @param initialDefectLevel3	설정할 초기 불량 분류 (3단계).
	 */
	public void setInitialDefectLevel3(String initialDefectLevel3);
}
