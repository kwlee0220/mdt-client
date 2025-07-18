package mdt.model.instance;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.stream.FStream;

import mdt.model.instance.MDTModel.MDTOperationModel;
import mdt.model.instance.MDTModel.MDTOperationModel.Argument;
import mdt.model.instance.MDTModel.MDTParameterModel;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.ref.MDTParameterReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTModelService {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTModelService.class);
	
	private final MDTInstanceManager m_manager;
	private final InstanceDescriptor m_desc;
	
	private MDTModelService(MDTInstanceManager manager, InstanceDescriptor desc) {
		m_manager = manager;
		m_desc = desc;
	}
	
	public static MDTModelService of(MDTInstanceManager manager, InstanceDescriptor desc) {
		return new MDTModelService(manager, desc);
	}
	
	public MDTModel readModel() throws IOException {
		MDTModel model = new MDTModel();
		model.setInstanceId(m_desc.getId());
		model.setStatus(m_desc.getStatus());
		model.setBaseEndpoint(m_desc.getBaseEndpoint());
		model.setAasId(m_desc.getAasId());
		model.setAasIdShort(m_desc.getAasIdShort());
		model.setGlobalAssetId(m_desc.getGlobalAssetId());
		model.setAssetType(m_desc.getAssetType());
		model.setAssetKind(m_desc.getAssetKind());

		model.setSubmodels(toSubmodelDescriptorMap());
		model.setParameters(toParameterModelMap());
		model.setOperations(toOperationModelMap());

		return model;
	}
	
	public LinkedHashMap<String,InstanceSubmodelDescriptor> toSubmodelDescriptorMap() {
		return FStream.from(m_desc.getInstanceSubmodelDescriptorAll())
						.mapToKeyValue(desc -> KeyValue.of(desc.getIdShort(), desc))
						.toMap(Maps.newLinkedHashMap());
	}
	
	public LinkedHashMap<String,MDTParameterModel> toParameterModelMap() throws IOException {
		return FStream.from(m_desc.getMDTParameterDescriptorAll())
				        .mapOrThrow(paramDesc -> toParameterModel(paramDesc))
				        .mapToKeyValue(paramModel -> KeyValue.of(paramModel.getName(), paramModel))
				        .toMap(Maps.newLinkedHashMap());
	}
	private MDTParameterModel toParameterModel(MDTParameterDescriptor paramDesc) throws IOException {
		MDTParameterModel paramModel = new MDTParameterModel();
		paramModel.setName(paramDesc.getName());
		paramModel.setReference(String.format("param:%s:%s", m_desc.getId(), paramDesc.getName()));

		MDTParameterReference ref = MDTParameterReference.newInstance(m_desc.getId(), paramDesc.getName());
		ref.activate(m_manager);
		paramModel.setValue(ref.readValue());
		
		return paramModel;
	}
	
	public LinkedHashMap<String,MDTOperationModel> toOperationModelMap() throws IOException {
		return FStream.from(m_desc.getMDTOperationDescriptorAll())
						.mapOrThrow(opDesc -> getOperationModel(opDesc))
						.mapToKeyValue(opModel -> KeyValue.of(opModel.getName(), opModel))
				        .toMap(Maps.newLinkedHashMap());
	}
	private MDTOperationModel getOperationModel(MDTOperationDescriptor opDesc) throws IOException {
		MDTOperationModel opModel = new MDTOperationModel();
		
		opModel.setName(opDesc.getName());
		opModel.setOperationType(opDesc.getOperationType());

		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(m_desc.getId(), opDesc.getName());
		LinkedHashMap<String,Argument> inputArgs
						= FStream.from(opDesc.getInputArguments())
								.mapOrThrow(nv -> {
									MDTArgumentReference argRef = MDTArgumentReference.builder()
																					.submodelReference(smRef)
																					.kind(MDTArgumentKind.INPUT)
																					.argument(nv.getName())
																					.build();
									argRef.activate(m_manager);
									
									MDTOperationModel.Argument arg = new MDTOperationModel.Argument();
									arg.setName(nv.getName());
									arg.setReference(String.format("oparg:%s:%s:in:%s",
													m_desc.getId(), opDesc.getName(), nv.getName()));
									arg.setValue(argRef.readValue());
									
									return arg;
								})
								.mapToKeyValue(arg -> KeyValue.of(arg.getName(), arg))
						        .toMap(Maps.newLinkedHashMap());
		opModel.setInputArguments(inputArgs);
		
		LinkedHashMap<String,Argument> outputArgs
						= FStream.from(opDesc.getOutputArguments())
								.mapOrThrow(nv -> {
									MDTArgumentReference argRef = MDTArgumentReference.builder()
																						.submodelReference(smRef)
																						.kind(MDTArgumentKind.OUTPUT)
																						.argument(nv.getName())
																						.build();
									argRef.activate(m_manager);
									
									MDTOperationModel.Argument arg = new MDTOperationModel.Argument();
									arg.setName(nv.getName());
									arg.setReference(String.format("oparg:%s:%s:in:%s",
													m_desc.getId(), opDesc.getName(), nv.getName()));
									arg.setValue(argRef.readValue());
									
									return arg;
								})
								.mapToKeyValue(arg -> KeyValue.of(arg.getName(), arg))
						        .toMap(Maps.newLinkedHashMap());
		opModel.setOutputArguments(outputArgs);
		
		return opModel;
	}
}
