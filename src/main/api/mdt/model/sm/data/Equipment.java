package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 * {@code Equipment}는 Data 서브모델에 정의된 설비 정보와 설비 파라미터들을 정의한다.
 * <p>
 * 설비 파라미터 ({@link EquipmentParameter})와 파라미터 값 ({@link EquipmentParameterValue})은
 * {@link ParameterCollection} 인터페이스를 통해 접근한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Equipment extends ParameterCollection {
	/**
	 * Equipment SubmodelElementCollection의 semantic id 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.EQUIPMENT;
	/**
	 * {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}.
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 설비 식별자({@code EquipmentID})를 반환한다.
	 *
	 * @return	설비 식별자.
	 */
	public String getEquipmentId();
	/**
	 * 설비 식별자({@code EquipmentID})를 설정한다.
	 *
	 * @param id	설정할 설비 식별자.
	 */
	public void setEquipmentId(String id);
	
	/**
	 * 설비 이름({@code EquipmentName})을 반환한다.
	 *
	 * @return	설비 이름.
	 */
	public String getEquipmentName();
	/**
	 * 설비 이름({@code EquipmentName})을 설정한다.
	 *
	 * @param name	설정할 설비 이름.
	 */
	public void setEquipmentName(String name);
	
//	public String getParentEquipmentID();
//	public void setParentEquipmentID(String parentEquipmentID);
	
	/**
	 * 설비 유형({@code EquipmentType})을 반환한다.
	 *
	 * @return	설비 유형.
	 */
	public String getEquipmentType();
	/**
	 * 설비 유형({@code EquipmentType})을 설정한다.
	 *
	 * @param equipmentType	설정할 설비 유형.
	 */
	public void setEquipmentType(String equipmentType);
	
//	public String getEquipmentLargeClass();
//	public void setEquipmentLargeClass(String equipmentLargeClass);
//	
//	public String getEquipmentMediumClass();
//	public void setEquipmentMediumClass(String equipmentMediumClass);
//	
//	public String getEquipmentSmallClass();
//	public void setEquipmentSmallClass(String equipmentSmallClass);
	
	/**
	 * 설비 사용 여부 표시자({@code UseIndicator})를 반환한다.
	 *
	 * @return	설비 사용 여부 표시자.
	 */
	public String getUseIndicator();
	/**
	 * 설비 사용 여부 표시자({@code UseIndicator})를 설정한다.
	 *
	 * @param useIndicator	설정할 설비 사용 여부 표시자.
	 */
	public void setUseIndicator(String useIndicator);
	
//	public String getDAQDevice();
//	public void setDAQDevice(String dAQDevice);
//	
//	public String getFixedAssetID();
//	public void setFixedAssetID(String fixedAssetID);
//	
//	public String getModelNumber();
//	public void setModelNumber(String modelNumber);
//	
//	public String getSerialNumber();
//	public void setSerialNumber(String serialNumber);
//	
//	public String getCountryOfOrigin();
//	public void setCountryOfOrigin(String countryOfOrigin);
//	
//	public String getManufacturer();
//	public void setManufacturer(String manufacturer);
//	
//	public Instant getAcquitionDate();
//	public void setAcquitionDate(Instant acquitionDate);
//	
//	public String getLocation();
//	public void setLocation(String location);
//	
//	public String getDepartmentInChargeID();
//	public void setDepartmentInChargeID(String departmentInChargeID);
//	
//	public String getPersonInChargeID();
//	public void setPersonInChargeID(String personInChargeID);
//	
//	public String getOperatorID();
//	public void setOperatorID(String operatorID);
}
