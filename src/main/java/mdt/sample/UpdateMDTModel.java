package mdt.sample;

import java.io.File;

import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Try;

import mdt.model.AASUtils;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.ResourceNotFoundException;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.simulation.Simulation;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class UpdateMDTModel {
	public static final void main(String... args) throws Exception {
		File aasFile = new File("C:/mdt/models/welder/model.json");
		
		Environment env = AASUtils.readEnvironment(aasFile);
		for ( Submodel sm: env.getSubmodels() ) {
			addSubmodelSemanticId(sm);
		}
		
		AASUtils.writeEnvironment(aasFile, env);
	}
	
	private static void addSubmodelSemanticId(Submodel sm) {
		if ( isSimulation(sm) ) {
			if ( sm.getSemanticId() == null ) {
				sm.setSemanticId(Simulation.SEMANTIC_ID_REFERENCE);
			}
			addInputOutputSemanticIds(sm);
		}
		else if ( isAI(sm) ) {
			if ( sm.getSemanticId() == null ) {
				sm.setSemanticId(AI.SEMANTIC_ID_REFERENCE);
			}
			addInputOutputSemanticIds(sm);
		}
		else if ( isData(sm) ) {
			if ( sm.getSemanticId() == null ) {
				sm.setSemanticId(Data.SEMANTIC_ID_REFERENCE);
			}
			if ( isEquipment(sm) ) {
				addEquipmentParameterSemanticId(sm);
			}
			else if ( isOperation(sm) ) {
				addOperationParameterSemanticId(sm);
			}
		}
	}

	private static boolean isData(Submodel sm) {
		return Funcs.findFirst(sm.getSubmodelElements(), sme -> sme.getIdShort().equals("DataInfo"))
						.isPresent();
	}
	private static boolean isSimulation(Submodel sm) {
		return Funcs.findFirst(sm.getSubmodelElements(), sme -> sme.getIdShort().equals("SimulationInfo"))
						.isPresent();
	}
	private static boolean isAI(Submodel sm) {
		return Funcs.findFirst(sm.getSubmodelElements(), sme -> sme.getIdShort().equals("AIInfo"))
						.isPresent();
	}
	private static boolean isEquipment(Submodel sm) {
		return Try.run(() -> SubmodelUtils.traverse(sm, "DataInfo.Equipment")).isSuccessful();
	}
	private static boolean isOperation(Submodel sm) {
		return Try.run(() -> SubmodelUtils.traverse(sm, "DataInfo.Operation")).isSuccessful();
	}
	
	private static void addEquipmentParameterSemanticId(Submodel sm) {
		SubmodelElementList paramValues = (SubmodelElementList)SubmodelUtils.traverse(sm,
																	"DataInfo.Equipment.EquipmentParameterValues");
		for ( SubmodelElement pv: paramValues.getValue() ) {
			FOption.ifAbsent(pv.getSemanticId(), () -> pv.setSemanticId(ParameterValue.SEMANTIC_ID_REFERENCE));
		}
		SubmodelElementList params = (SubmodelElementList)SubmodelUtils.traverse(sm,
																	"DataInfo.Equipment.EquipmentParameters");
		for ( SubmodelElement param: params.getValue() ) {
			FOption.ifAbsent(param.getSemanticId(), () -> param.setSemanticId(Parameter.SEMANTIC_ID_REFERENCE));
		}
	}
	
	private static void addOperationParameterSemanticId(Submodel sm) {
		SubmodelElementList paramValues = (SubmodelElementList)SubmodelUtils.traverse(sm,
																	"DataInfo.Operation.OperationParameterValues");
		for ( SubmodelElement pv: paramValues.getValue() ) {
			FOption.ifAbsent(pv.getSemanticId(), () -> pv.setSemanticId(ParameterValue.SEMANTIC_ID_REFERENCE));
		}
		SubmodelElementList params = (SubmodelElementList)SubmodelUtils.traverse(sm,
																	"DataInfo.Operation.OperationParameters");
		for ( SubmodelElement param: params.getValue() ) {
			FOption.ifAbsent(param.getSemanticId(), () -> param.setSemanticId(Parameter.SEMANTIC_ID_REFERENCE));
		}
	}
	
	private static void addInputOutputSemanticIds(Submodel sm) {
		try {
			SubmodelElementList inputs = (SubmodelElementList)SubmodelUtils.traverse(sm, "SimulationInfo.Inputs");
			for ( SubmodelElement input: inputs.getValue() ) {
				FOption.ifAbsent(input.getSemanticId(), () -> input.setSemanticId(Input.SEMANTIC_ID_REFERENCE));
			}
			SubmodelElementList outs = (SubmodelElementList)SubmodelUtils.traverse(sm, "SimulationInfo.Outputs");
			for ( SubmodelElement out: outs.getValue() ) {
				FOption.ifAbsent(out.getSemanticId(), () -> out.setSemanticId(Output.SEMANTIC_ID_REFERENCE));
			}
		}
		catch ( ResourceNotFoundException e ) { }
		try {
			SubmodelElementList inputs = (SubmodelElementList)SubmodelUtils.traverse(sm, "AIInfo.Inputs");
			for ( SubmodelElement input: inputs.getValue() ) {
				FOption.ifAbsent(input.getSemanticId(), () -> input.setSemanticId(Input.SEMANTIC_ID_REFERENCE));
			}
			SubmodelElementList outs = (SubmodelElementList)SubmodelUtils.traverse(sm, "SimulationInfo.Outputs");
			for ( SubmodelElement out: outs.getValue() ) {
				FOption.ifAbsent(out.getSemanticId(), () -> out.setSemanticId(Output.SEMANTIC_ID_REFERENCE));
			}
		}
		catch ( ResourceNotFoundException e ) { }
	}
}
