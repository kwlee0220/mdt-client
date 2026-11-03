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
 * {@link Equipment} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 * <p>
 * 이 밖에 {@code WorkingLOTs}, {@code EquipmentParameters}, {@code EquipmentParameterValues}
 * SubmodelElementList를 각각 {@link LOT}, {@link Parameter}, {@link ParameterValue} 목록으로 관리한다.
 * SubmodelElementCollection의 idShort는 설비 식별자 ({@code EquipmentID})이다.
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
	
	/**
	 * 주어진 SubmodelElement로부터 {@code DefaultEquipment} 객체를 생성한다.
	 *
	 * @param sme	Equipment SubmodelElementCollection.
	 * @return	생성된 {@code DefaultEquipment} 객체.
	 */
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
