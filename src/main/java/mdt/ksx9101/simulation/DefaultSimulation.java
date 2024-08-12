package mdt.ksx9101.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import utils.func.Funcs;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.SubModelInfo;
import mdt.ksx9101.model.impl.DefaultSubModelInfo;
import mdt.model.AbstractMDTSubmodel;
import mdt.model.SMCField;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulation extends AbstractMDTSubmodel implements Simulation {
	@SMCField(idShort="SubModelInfo", adaptorClass=DefaultSubModelInfo.class)
	private SubModelInfo subModelInfo;
	
	@SMCField(idShort="SimulationInfo")
	private DefaultSimulationInfo simulationInfo;
	
	private Operation simulationOperation;

	public DefaultSimulation() {
		setIdShort("Simulation");
		setSemanticId(SEMANTIC_ID);
	}
	
	@Override
	public Submodel toAasModel() {
		Submodel submodel = super.toAasModel();
		submodel.getSubmodelElements().add(this.simulationOperation);
		
		return submodel;
	}

	@Override
	public void fromAasModel(Submodel model) {
		super.fromAasModel(model);
		
		Funcs.findFirst(model.getSubmodelElements(), sme -> "SimulationOperation".equals(sme.getIdShort()))
			.ifPresent(sme -> {
				if ( sme instanceof Operation op ) {
					this.simulationOperation = op;
				}
				else {
					throw new IllegalArgumentException("Invalid 'Simulation' Submodel: "
														+ "invalid 'SimulationOperation");
				}
			});
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), subModelInfo.getTitle());
	}
}
