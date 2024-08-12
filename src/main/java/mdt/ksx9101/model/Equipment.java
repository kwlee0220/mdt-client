package mdt.ksx9101.model;

import java.util.List;

import com.google.common.base.Preconditions;

import utils.func.FOption;

import mdt.ksx9101.TopLevelEntity;
import mdt.model.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Equipment extends ParametricEntity, TopLevelEntity {
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

	public default void update(String idShortPath, Object value) {
		List<String> pathSegs = SubmodelUtils.parseIdShortPath(idShortPath).toList();
		
		String seg0 = pathSegs.get(0);
		Preconditions.checkArgument("EquipmentParameters".equals(seg0),
									"'EquipmentParameters' is expected, but={}", seg0);
		
		String seg1 = pathSegs.get(1);
		ParameterValue pvalue;
		try {
			int ordinal = Integer.parseInt(seg1);
			pvalue = getParameterValues().get(ordinal);
		}
		catch ( NumberFormatException e ) {
			pvalue = getParameterValue(seg1).orElse(null);
		}
		FOption.accept(pvalue, pv -> pv.setParameterValue((String)value));
	}
}
