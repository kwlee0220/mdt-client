package mdt.model.instance;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.Input;
import mdt.model.Output;
import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ai.AIInfo;
import mdt.model.sm.ai.DefaultAI;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.simulation.DefaultSimulation;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.simulation.SimulationInfo;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"@class", "id", "aasId", "aasIdShort", "assetType", "status", "baseEndpoint",
					"parameters", "operations"})
@JsonInclude(Include.NON_NULL)
public class DefaultMDTInstanceInfo implements MDTInstanceInfo {
	@JsonProperty("@class") private final String m_jsonClass;
	@JsonProperty("id") private final String m_id;
	@JsonProperty("aasId") private final String m_aasId;
	@JsonProperty("aasIdShort") private final String m_aasIdShort;
	@JsonProperty("assetType") private final String m_assetType;
	@JsonProperty("status") private final MDTInstanceStatus m_status;
	@JsonProperty("baseEndpoint") private final String m_baseEndpoint;
	@JsonProperty("parameters") private final KeyedValueList<String, ParameterInfo> m_parameters;
	@JsonProperty("operations") private final KeyedValueList<String, OperationInfo> m_operations;
	
	@JsonCreator
	public DefaultMDTInstanceInfo(@JsonProperty("id") String id,
									@JsonProperty("aasId") String aasId,
									@JsonProperty("aasIdShort") String aasIdShort,
									@JsonProperty("assetType") String assetType,
									@JsonProperty("status") MDTInstanceStatus status,
									@JsonProperty("baseEndpoint") String baseEndpoint,
									@JsonProperty("parameters") List<ParameterInfo> parameters,
									@JsonProperty("operations") List<DefaultOperationInfo> operations) {
		m_jsonClass = MDTInstanceInfo.class.getName();
		m_id = id;
		m_aasId = aasId;
		m_aasIdShort = aasIdShort;
		m_assetType = assetType;
		m_status = status;
		m_baseEndpoint = baseEndpoint;
		m_parameters = KeyedValueList.from(parameters, ParameterInfo::getId);
		m_operations = KeyedValueList.from(operations, OperationInfo::getId);
	}
	
	private DefaultMDTInstanceInfo(Builder builder) {
		m_jsonClass = MDTInstanceInfo.class.getName();
		m_id = builder.m_id;
		m_aasId = builder.m_aasId;
		m_aasIdShort = builder.m_aasIdShort;
		m_assetType = builder.m_assetType;
		m_status = builder.m_status;
		m_baseEndpoint = builder.m_baseEndpoint;
		m_parameters = builder.m_parameters;
		m_operations = builder.m_operations;
	}
	
	@JsonProperty("@class")
	public String getJsonClass() {
		return m_jsonClass;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public String getAasId() {
		return m_aasId;
	}

	@Override
	public String getAasIdShort() {
		return m_aasIdShort;
	}

	@Override
	public String getAssetType() {
		return m_assetType;
	}

	@Override
	public MDTInstanceStatus getStatus() {
		return m_status;
	}

	@Override
	public String getBaseEndpoint() {
		return m_baseEndpoint;
	}

	@Override
	public List<ParameterInfo> getParameters() {
		return m_parameters;
	}

	@Override
	public List<OperationInfo> getOperations() {
		return m_operations;
	}
	
	public static Builder builder(MDTInstance instance) {
		Builder builder = new Builder(instance.getInstanceDescriptor());
		if ( instance.getStatus() == MDTInstanceStatus.RUNNING ) {
			String assetType = instance.getInformationModel().getMDTInfo().getAssetType();
			builder.m_assetType = switch ( assetType ) {
				case "Machine" -> "Equipment";
				case "Process" -> "Operation";
				default -> assetType;
			};
			
			builder.fillSubmodelInfos(FStream.from(instance.getSubmodelServiceAll())
					.map(SubmodelService::getSubmodel).toList());
		}
		return builder;
	}
	public static class Builder {
		private String m_id;
		private String m_aasId;
		private String m_aasIdShort;
		private String m_assetType;
		private MDTInstanceStatus m_status;
		private String m_baseEndpoint;
		private KeyedValueList<String, ParameterInfo> m_parameters = KeyedValueList.newInstance(ParameterInfo::getId);
		private KeyedValueList<String, OperationInfo> m_operations = KeyedValueList.newInstance(OperationInfo::getId);
		
		public DefaultMDTInstanceInfo build() {
			return new DefaultMDTInstanceInfo(this);
		}
		
		public Builder(InstanceDescriptor instDesc) {
            m_id = instDesc.getId();
            m_aasId = instDesc.getAasId();
            m_aasIdShort = instDesc.getAasIdShort();
            m_assetType = instDesc.getAssetType();
            m_status = instDesc.getStatus();
            m_baseEndpoint = instDesc.getBaseEndpoint();
		}
		
		private Builder fillSubmodelInfos(List<Submodel> submodels) {
			for ( Submodel sm : submodels ) {
				switch ( ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId()) ) {
					case Data.SEMANTIC_ID:
						buildParameterInfoList(sm);
	                    break;
					case AI.SEMANTIC_ID:
						addAIOperationInfo(sm);
						break;
					case Simulation.SEMANTIC_ID:
						addSimulationOperationInfo(sm);
	                    break;
				};
			}
			
			return this;
		}
		
		private void buildParameterInfoList(Submodel sm) {
			DefaultData data = new DefaultData();
			data.updateFromAasModel(sm);
			
			try {
				addParameterInfoList(data.getDataInfo().getEquipment().getParameterValueList());
			}
			catch ( ResourceNotFoundException expected ) {
				addParameterInfoList(data.getDataInfo().getOperation().getParameterValueList());
			}
		}
		
		private void addParameterInfoList(List<ParameterValue> paramValueList) {
	        FStream.from(paramValueList)
                    .map(this::toParameterInfo)
                    .forEach(m_parameters::add);
	    }
		
		private ParameterInfo toParameterInfo(ParameterValue pvalue) {
			String propId = pvalue.getParameterId();
			SubmodelElement valueSme = pvalue.getParameterValue();
			if ( valueSme instanceof Property prop ) {
				return PropertyParameterInfo.from(propId, prop);
			}
			else if ( valueSme instanceof File file ) {
				return FileParameterInfo.from(propId, file);
			}
			else {
				throw new AssertionError("unsupported ParameterValue type: " + valueSme);
			}
		}
		
		private void addAIOperationInfo(Submodel sm) {
			DefaultAI ai = new DefaultAI();
			ai.updateFromAasModel(sm);
			
			AIInfo info = ai.getAIInfo();
			List<ParameterInfo> inArgs = FStream.from(info.getInputs()).map(this::getInput).toList();
			List<ParameterInfo> outArgs = FStream.from(info.getOutputs()).map(this::getOutput).toList();
			DefaultOperationInfo opInfo = new DefaultOperationInfo(ai.getIdShort(), "AI", inArgs, outArgs);
			m_operations.add(opInfo);
		}
		
		private void addSimulationOperationInfo(Submodel sm) {
			DefaultSimulation sim = new DefaultSimulation();
			sim.updateFromAasModel(sm);

			SimulationInfo info = sim.getSimulationInfo();
			List<ParameterInfo> inArgs = FStream.from(info.getInputs()).map(this::getInput).toList();
			List<ParameterInfo> outArgs = FStream.from(info.getOutputs()).map(this::getOutput).toList();
			DefaultOperationInfo opInfo = new DefaultOperationInfo(sim.getIdShort(), "Simulation", inArgs, outArgs);
			m_operations.add(opInfo);
		}
		
		private ParameterInfo getInput(Input input) {
			String inputId = input.getInputID();
			SubmodelElement valueSme = input.getInputValue();
			if ( valueSme instanceof Property prop ) {
				return PropertyParameterInfo.from(inputId, prop);
			}
			else if ( valueSme instanceof File file ) {
				return FileParameterInfo.from(inputId, file);
			}
			else {
				throw new AssertionError("unsupported Input type: " + valueSme);
			}
		}
		
		private ParameterInfo getOutput(Output output) {
			String outputId = output.getOutputID();
			SubmodelElement valueSme = output.getOutputValue();
			if ( valueSme instanceof Property prop ) {
				return PropertyParameterInfo.from(outputId, prop);
			}
			else if ( valueSme instanceof File file ) {
				return FileParameterInfo.from(outputId, file);
			}
			else {
				throw new AssertionError("unsupported Output type: " + valueSme);
			}
		}
	}
}
