package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMElementField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link Input}의 기본 구현으로, 연산 입력 인자를 SubmodelElementCollection 엔티티로 표현한다.
 * <p>
 * 각 필드는 같은 이름의 하위 SubmodelElement(InputID, InputName, InputValue, InputType)에
 * 매핑되며, 엔티티의 idShort는 입력 인자 식별자(InputID)를 사용한다. 동일성 비교({@code equals}/
 * {@code hashCode})도 입력 인자 식별자만으로 판단한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultInput extends SubmodelElementCollectionEntity implements Input {
	@PropertyField(idShort="InputID")
	private String inputID;
	
	@PropertyField(idShort="InputName")
	private String inputName;
	
	@SMElementField(idShort="InputValue")
	private SubmodelElement inputValue;
	
	@PropertyField(idShort="InputType")
	private String inputType;
	
	@Override
	public String getIdShort() {
		return this.inputID;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		DefaultInput other = (DefaultInput)obj;
		return Objects.equal(getInputID(), other.getInputID());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.inputID);
	}
	
	@Override
	public String toString() {
		return String.format("Input[%s]", this.inputID);
	}
}
