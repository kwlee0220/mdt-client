package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMElementField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
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
