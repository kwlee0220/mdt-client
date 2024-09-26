package mdt.ksx9101.simulation;

import com.google.common.base.Objects;

import lombok.Getter;
import lombok.Setter;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class Input extends SubmodelElementCollectionEntity {
	@PropertyField(idShort="InputID")
	private String inputID;
	
	@PropertyField(idShort="InputName")
	private String inputName;
	
	@PropertyField(idShort="InputValue")
	private String inputValue;
	
	@PropertyField(idShort="InputType")
	private String inputType;
	
	public Input() {
		super(null, "inputID");
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		Input other = (Input)obj;
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
