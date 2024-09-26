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
public class DefaultSimulationOutput extends SubmodelElementCollectionEntity
										implements SimulationOutput {
	@PropertyField(idShort="OutputID")
	private String outputID;
	
	@PropertyField(idShort="OutputName")
	private String outputName;
	
	@PropertyField(idShort="OutputValue")
	private String outputValue;
	
	@PropertyField(idShort="OutputType")
	private String outputType;
	
	public DefaultSimulationOutput() {
		super(null, "outputID");
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		DefaultSimulationOutput other = (DefaultSimulationOutput)obj;
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
