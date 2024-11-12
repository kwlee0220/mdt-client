package mdt.model.sm.data;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


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

	@SMListField(idShort="WorkingLOTs", elementClass=DefaultLOT.class)
	private List<LOT> workingLOTs = Lists.newArrayList();
	
	@SMListField(idShort="EquipmentParameters", elementClass=DefaultEquipmentParameter.class)
	private List<Parameter> parameterList = Lists.newArrayList();
	@SMListField(idShort="EquipmentParameterValues", elementClass=DefaultEquipmentParameterValue.class)
	private List<ParameterValue> parameterValueList = Lists.newArrayList();
	
	public static DefaultEquipment from(SubmodelElement sme) {
		DefaultEquipment equip = new DefaultEquipment();
		equip.updateFromAasModel(sme);
		return equip;
	}
	
	@Override
	public String getIdShort() {
		return this.equipmentId;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getEquipmentId());
	}
}
