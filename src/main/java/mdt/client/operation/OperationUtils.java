package mdt.client.operation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import lombok.experimental.UtilityClass;

import utils.InternalException;
import utils.KeyedValueList;
import utils.func.FOption;
import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.model.Input;
import mdt.model.MDTModelSerDe;
import mdt.model.Output;
import mdt.model.ReferenceUtils;
import mdt.model.service.SubmodelService;
import mdt.model.sm.AASFile;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultEquipment;
import mdt.model.sm.data.DefaultOperation;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.info.DefaultMDTInfo;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.simulation.DefaultSimulationInfo;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.value.SubmodelElementValue;
import mdt.task.OperationExecutionContext;
import mdt.task.Parameter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class OperationUtils {
	private static final Logger s_logger = LoggerFactory.getLogger(OperationUtils.class);
	
	public static String toParametersJsonString(Collection<Parameter> parameters) {
		return MDTModelSerDe.toJsonString(parameters);
	}
	
	public static List<Parameter> parseParametersJsonString(String paramJsonStr) throws IOException {
		return MDTModelSerDe.readValueList(paramJsonStr, Parameter.class);
	}
	
	public static ObjectNode toSubmodelElementsJsonNode(Map<String,SubmodelElement> elements) {
		return FStream.from(elements)
						.mapValue(MDTModelSerDe::toJsonNode)
						.fold(MDTModelSerDe.getJsonMapper().createObjectNode(),
								(on,kv) -> on.set(kv.key(), kv.value()));
	}
	public static String toSubmodelElementsJsonString(Map<String,SubmodelElement> elements) throws IOException {
		ObjectNode jnode = toSubmodelElementsJsonNode(elements);
		return MDTModelSerDe.getJsonMapper().writeValueAsString(jnode);
	}
	
	public static Map<String,SubmodelElement> parseSubmodelElementsJsonString(String elementsJson)
		throws IOException {
		Map<String,SubmodelElement> elements = Maps.newHashMap();
		for ( Map.Entry<String,JsonNode> ent: MDTModelSerDe.getJsonMapper().readTree(elementsJson).properties() ) {
			SubmodelElement element = MDTModelSerDe.readValue(ent.getValue(), SubmodelElement.class);
			elements.put(ent.getKey(), element);
		}
		return elements;
	}
	
	public static File downloadParameter(Parameter param, File dir) throws IOException {
		SubmodelElement sme = param.getReference().read();
		
		if ( sme instanceof Property ) {
			File paramFile = new File(dir, param.getName());
			IOUtils.toFile(ElementValues.toRawString(sme), StandardCharsets.UTF_8, paramFile);
			return paramFile;
		}
		else if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
			Preconditions.checkArgument(param.getReference() instanceof MDTElementReference,
										"Parameter should contains MDTSubmodelElementReference, but {}",
										param.getReference().getClass());
			
			MDTElementReference dref = (MDTElementReference)param.getReference();
			AASFile mdtFile = dref.getSubmodelService().getFileByPath(dref.getElementPath());
			File paramFile = new File(dir, FilenameUtils.getName(mdtFile.getPath()));
			
			if ( s_logger.isInfoEnabled() ) {
				s_logger.info("downloading MDTFile: {} from {} to {}", mdtFile, dref, paramFile);
			}
			IOUtils.toFile(mdtFile.getContent(), paramFile);
			
			return paramFile;
		}
		else {
			String msg = String.format("Cannot support SubmodelElement type for download: type={}", sme.getClass());
			throw new UnsupportedOperationException(msg);
		}
	}
	
	public static Parameter toParameter(OperationVariable opv) {
		SubmodelElement sme = opv.getValue();
		return Parameter.of(sme.getIdShort(), sme);
	}
	
	public static String toExternalString(SubmodelElement sme) throws IOException {
		SubmodelElementValue smev = ElementValues.getValue(sme);
		if ( smev != null ) {
			return ( smev instanceof PropertyValue propv )
					? FOption.getOrElse(propv.getValue(), "")
					: MDTModelSerDe.toJsonString(smev);
		}
		else {
			return null;
		}
	}
	
	public OperationExecutionContext loadSubmodelExecutionContext(MDTSubmodelReference smRef) {
		SubmodelService svc = smRef.get();

		Reference semanticId = svc.getSubmodel().getSemanticId();
		if ( Simulation.SEMANTIC_ID_REFERENCE.equals(semanticId) ) {
			return loadSimulationExecutionContext(svc, smRef);
		
		}
		else if ( AI.SEMANTIC_ID_REFERENCE.equals(semanticId) ) {
			return loadAIExecutionContext(svc, smRef);
		}
		else if ( Data.SEMANTIC_ID_REFERENCE.equals(semanticId) ) {
			OperationExecutionContext opContext = new OperationExecutionContext();
			opContext.setInputParameters(loadDataParameters(svc, smRef));
			
			return opContext;
		}
		
		throw new IllegalArgumentException("Unsupported Submodel's semantic-id: "
											+ ReferenceUtils.getSemanticIdStringOrNull(semanticId));
	}

	private OperationExecutionContext loadSimulationExecutionContext(SubmodelService svc,
																	MDTSubmodelReference smRef) {
		DefaultSimulationInfo info = new DefaultSimulationInfo();
		info.updateFromAasModel(svc.getSubmodelElementByPath("SimulationInfo"));
		
		OperationExecutionContext opContext = new OperationExecutionContext();
		
		List<Parameter> inParams
			= FStream.from(info.getInputs())
						.zipWithIndex()
						.map(idxed -> {
							Input input = idxed.value();
							String path = String.format("SimulationInfo.Inputs[%d].InputValue", idxed.index());
							ElementReference valRef = DefaultElementReference.newInstance(smRef, path);
							return Parameter.of(input.getInputID(), valRef);
						})
						.toList();
		opContext.setInputParameters(inParams);

		List<Parameter> outParams
			 = FStream.from(info.getOutputs())
						.zipWithIndex()
						.map(idxed -> {
							Output output = idxed.value();
							String path = String.format("SimulationInfo.Outputs[%d].OutputValue", idxed.index());
							ElementReference valRef = DefaultElementReference.newInstance(smRef, path);
							return Parameter.of(output.getOutputID(), valRef);
						})
						.toList();
		opContext.setOutputParameters(outParams);
		
		opContext.setLastExecutionTimeReference(DefaultElementReference.newInstance(smRef,
																			"SimulationInfo.Model.LastExecutionTime"));
		
		return opContext;
	}
	
	private OperationExecutionContext loadAIExecutionContext(SubmodelService svc, MDTSubmodelReference smRef) {
		DefaultSimulationInfo info = new DefaultSimulationInfo();
		info.updateFromAasModel(svc.getSubmodelElementByPath("AIInfo"));
		
		OperationExecutionContext opParams = new OperationExecutionContext();

		List<Parameter> inParams
			= FStream.from(info.getInputs())
					.zipWithIndex()
					.map(idxed -> {
						Input input = idxed.value();
						String path = String.format("AIInfo.Inputs[%d].InputValue", idxed.index());
						ElementReference valRef = DefaultElementReference.newInstance(smRef, path);
						return Parameter.of(input.getInputID(), valRef);
					})
					.toList();
		opParams.setInputParameters(inParams);

		List<Parameter> outParams
			 = FStream.from(info.getOutputs())
						.zipWithIndex()
						.map(idxed -> {
							Output output = idxed.value();
							String path = String.format("AIInfo.Outputs[%d].OutputValue", idxed.index());
							ElementReference valRef = DefaultElementReference.newInstance(smRef, path);
							
							return Parameter.of(output.getOutputID(), valRef);
						})
						.toList();
		opParams.setOutputParameters(outParams);

		opParams.setLastExecutionTimeReference(DefaultElementReference.newInstance(smRef,
																				"AIInfo.Model.LastExecutionTime"));
		
		return opParams;
	}
	
	private KeyedValueList<String,Parameter> loadDataParameters(SubmodelService svc, MDTSubmodelReference smRef) {
		SubmodelService infoSvc = DefaultSubmodelReference.newInstance(smRef.getInstance(), "InformationModel")
															.get();
		
		DefaultMDTInfo mdtInfo = new DefaultMDTInfo();
		mdtInfo.updateFromAasModel(infoSvc.getSubmodelElementByPath("MDTInfo"));
		
		if ( mdtInfo.getAssetType().equals("Machine") ) {
			return loadEquipmentParameters(svc, smRef);
		}
		else if ( mdtInfo.getAssetType().equals("Process") ) {
			return loadOperationParameters(svc, smRef);
		}
		else {
			throw new InternalException("Unknown AssetType: " + mdtInfo.getAssetType());
		}
	}
	
	private static final String EQUIP_PARAM_PATTERN = "DataInfo.Equipment.EquipmentParameterValues[%d].ParameterValue";
	private KeyedValueList<String,Parameter> loadEquipmentParameters(SubmodelService svc, MDTSubmodelReference smRef) {
		DefaultEquipment equipment = new DefaultEquipment();
		equipment.updateFromAasModel(svc.getSubmodelElementByPath("DataInfo.Equipment"));
		
		return FStream.from(equipment.getParameterValueList())
						.zipWithIndex()
						.map(idxed -> {
							ParameterValue pv = idxed.value();
							String path = String.format(EQUIP_PARAM_PATTERN, idxed.index());
							ElementReference valRef = DefaultElementReference.newInstance(smRef, path);
							return Parameter.of(pv.getParameterId(), valRef);
						})
						.collect(KeyedValueList.newInstance(Parameter::getName), KeyedValueList::add);
	}

	private static final String OP_PARAM_PATTERN = "DataInfo.Operation.OperationParameterValues[%d].ParameterValue";
	private KeyedValueList<String,Parameter> loadOperationParameters(SubmodelService svc, MDTSubmodelReference smRef) {
		DefaultOperation equipment = new DefaultOperation();
		equipment.updateFromAasModel(svc.getSubmodelElementByPath("DataInfo.Operation"));
		
		return FStream.from(equipment.getParameterValueList())
						.zipWithIndex()
						.map(idxed -> {
							ParameterValue pv = idxed.value();
							String path = String.format(OP_PARAM_PATTERN, idxed.index());
							ElementReference valRef = DefaultElementReference.newInstance(smRef, path);
							return Parameter.of(pv.getParameterId(), valRef);
						})
						.collect(KeyedValueList.newInstance(Parameter::getName), KeyedValueList::add);
	}
}
