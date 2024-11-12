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
public class DefaultOutput extends SubmodelElementCollectionEntity
										implements Output {
	@PropertyField(idShort="OutputID")
	private String outputID;
	
	@PropertyField(idShort="OutputName")
	private String outputName;

	@SMElementField(idShort="OutputValue")
	private SubmodelElement outputValue;
	
	@PropertyField(idShort="OutputType")
	private String outputType;
	
	@Override
	public String getIdShort() {
		return this.outputID;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		DefaultOutput other = (DefaultOutput)obj;
		return Objects.equal(getOutputID(), other.getOutputID());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.outputID);
	}
	
	@Override
	public String toString() {
		return String.format("Output[%s]", this.outputID);
	}
}
