package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import mdt.model.service.ParameterCollection;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Equipment extends ParameterCollection {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/Data/Equipment/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
	public String getEquipmentId();
	public void setEquipmentId(String id);
	
	public String getEquipmentName();
	public void setEquipmentName(String name);
	
//	public String getParentEquipmentID();
//	public void setParentEquipmentID(String parentEquipmentID);
	
	public String getEquipmentType();
	public void setEquipmentType(String equipmentType);
	
//	public String getEquipmentLargeClass();
//	public void setEquipmentLargeClass(String equipmentLargeClass);
//	
//	public String getEquipmentMediumClass();
//	public void setEquipmentMediumClass(String equipmentMediumClass);
//	
//	public String getEquipmentSmallClass();
//	public void setEquipmentSmallClass(String equipmentSmallClass);
	
	public String getUseIndicator();
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
