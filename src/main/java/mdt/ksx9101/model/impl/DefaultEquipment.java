package mdt.ksx9101.model.impl;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Equipment;
import mdt.ksx9101.model.LOT;
import mdt.ksx9101.model.Parameter;
import mdt.ksx9101.model.ParameterValue;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;
import mdt.model.SMLField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultEquipment extends SubmodelElementCollectionEntity implements Equipment {
	@PropertyField(idShort="EquipmentID") private String equipmentId;
	@PropertyField(idShort="EquipmentName") private String equipmentName;
	@PropertyField(idShort="EquipmentType") private String equipmentType;
	@PropertyField(idShort="UseIndicator") private String useIndicator;

	@SMLField(idShort="WorkingLOTs", elementClass=DefaultLOT.class)
	private List<LOT> workingLOTs = Lists.newArrayList();
	
	@SMLField(idShort="EquipmentParameters", elementClass=DefaultEquipmentParameter.class)
	private List<Parameter> parameters = Lists.newArrayList();
	@SMLField(idShort="EquipmentParameterValues", elementClass=DefaultEquipmentParameterValue.class)
	private List<ParameterValue> parameterValues = Lists.newArrayList();
	
	public DefaultEquipment() {
		super("Equipment", null);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getEquipmentId());
	}
}
