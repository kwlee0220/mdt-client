package mdt.model.instance;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.func.Lazy;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.info.CompositionItem;
import mdt.model.sm.info.DefaultInformationModel;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.info.TwinComposition;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.ref.MDTParameterReference;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTModelServiceOld {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTModelServiceOld.class);
	
	private final MDTInstance m_instance;
	private final InstanceDescriptor m_desc;
	
	private final Lazy<InformationModel> m_informationModel = Lazy.of(() -> loadInformationModel());
	private final Lazy<Map<String,CompositionItem>> m_components = Lazy.of(() -> loadComponentMap());
	private final Lazy<Map<String,MDTParameterDescriptor>> m_parameterMap = Lazy.of(() -> loadParameterMap());
	private final Lazy<Map<String,MDTOperationDescriptor>> m_operationMap = Lazy.of(() -> loadOperationMap());
	
	private MDTModelServiceOld(MDTInstance instance) {
		m_instance = instance;
		m_desc = instance.getInstanceDescriptor();
	}
	
	public static MDTModelServiceOld of(MDTInstance instance) {
		return new MDTModelServiceOld(instance);
	}
	
	public MDTInstance getInstance() {
		return m_instance;
	}

	public InformationModel getInformationModel() {
		return m_informationModel.get();
	}
	
	public List<MDTInstance> getSubComponentAll() {
		try {
			return FStream.from(getTargetInstanceAll("contain"))
							.filterNot(inst -> inst.getId().equals(m_desc.getId()))
							.toList();
		}
		catch ( Exception e ) {
			// 현 MDTInstance가 RUNNING 상태가 아닌 경우에는 TwinComposition을
			// 구할 수 없어서 empty list를 반환한다.
			return Collections.emptyList();
		}
	}
	
	public List<MDTInstance> getTargetInstanceAll(String depType) {
		TwinComposition tcomp = getInformationModel().getTwinComposition();
		String myId = tcomp.getCompositionID();

		return FStream.from(tcomp.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals(depType) && dep.getSourceId().equals(myId))
						.mapOrIgnore(dep -> toInstance(dep.getTargetId()))
						.toList();
	}
	
	public List<MDTInstance> getSourceInstanceAll(String depType) {
		TwinComposition tcomp = getInformationModel().getTwinComposition();
		String myId = tcomp.getCompositionID();

		return FStream.from(tcomp.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals(depType) && dep.getTargetId().equals(myId))
						.mapOrThrow(dep -> toInstance(dep.getSourceId()))
						.toList();
	}
	
	public Data getData() throws ResourceNotFoundException {
		SubmodelService dataSvc =  FStream.from(m_instance.getSubmodelServiceAllBySemanticId(Data.SEMANTIC_ID))
									        .findFirst()
											.getOrThrow(() -> new ResourceNotFoundException("DataService"));
		
		DefaultData data = new DefaultData();
		data.updateFromAasModel(dataSvc.getSubmodel());
		return data;
	}
	
	public List<MDTParameterDescriptor> getParameterDescriptorAll() {
		return m_desc.getMDTParameterDescriptorAll();
	}
	
	public MDTParameterDescriptor getParameterDescriptor(String paramName) {
		MDTParameterDescriptor paramDesc = m_parameterMap.get().get(paramName);
		if ( paramDesc == null ) {
			throw new ResourceNotFoundException("MDTParameterDescriptor", paramName);
		}
		return paramDesc;
	}
	
	public SubmodelElement readParameterValueElementValue(String paramName) throws IOException {
		return readParameterSubmodelElement(getParameterDescriptor(paramName));
	}
	
	public MDTOperationDescriptor getOperationDescriptor(String opName) {
		MDTOperationDescriptor opDesc = m_operationMap.get().get(opName);
		if ( opDesc == null ) {
			throw new ResourceNotFoundException("MDTOperationDescriptor", opName);
		}
		return opDesc;
	}
	
	public List<ElementValue> readOperationArgumentValueAll(String opName, MDTArgumentKind kind) throws IOException {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(m_instance.getId(), opName);
		MDTArgumentReference argsRef = MDTArgumentReference.builder()
															.submodelReference(smRef)
															.kind(kind)
															.argument("*")
															.build();
		argsRef.activate(m_instance.getInstanceManager());
		
		return ((ElementListValue) argsRef.readValue()).getElementAll();
	}
	
	public String toJsonString(boolean prettyPrint) throws IOException {
		JsonMapper mapper = MDTModelSerDe.MAPPER;
		if ( prettyPrint ) {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toJsonNode());
		}
		else {
			return mapper.writeValueAsString(toJsonNode());
		}
	}
	
	public Map<String,Object> getParameterMapInfo() throws IOException {
		Map<String,Object> parameterInfoMap = Maps.newHashMap();
		FStream.from(m_desc.getMDTParameterDescriptorAll())
				.forEachOrThrow(paramDesc -> {
					ElementValue paramAsValue = readParameterValue(paramDesc.getName());
					parameterInfoMap.put(paramDesc.getName(), paramAsValue);
				});
		return parameterInfoMap;
	}
	
	public List<Object> getOperationInfoList() throws IOException {
		return FStream.from(m_desc.getMDTOperationDescriptorAll())
						.mapOrThrow(opDesc -> (Object)getOperationInfo(opDesc))
						.toList();
	}
	
	private Map<String,Object> getOperationInfo(MDTOperationDescriptor opDesc) throws IOException {
		Map<String,Object> operationInfo = Maps.newHashMap();
		operationInfo.put("name", opDesc.getName());
		operationInfo.put("operationType", opDesc.getOperationType());

		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(m_instance.getId(), opDesc.getName());
		Map<String,ElementValue> inputArgs = Maps.newHashMap();
		FStream.from(opDesc.getInputArguments())
				.forEachOrThrow(nv -> {
					MDTArgumentReference argRef = MDTArgumentReference.builder()
																		.submodelReference(smRef)
																		.kind(MDTArgumentKind.INPUT)
																		.argument(nv.getName())
																		.build();
					argRef.activate(m_instance.getInstanceManager());
					inputArgs.put(nv.getName(), argRef.readValue());
				});
		operationInfo.put("inputArguments", inputArgs);
		
		Map<String,ElementValue> outputArgs = Maps.newHashMap();
		FStream.from(opDesc.getInputArguments())
				.forEachOrThrow(nv -> {
					MDTArgumentReference argRef = MDTArgumentReference.builder()
																		.submodelReference(smRef)
																		.kind(MDTArgumentKind.OUTPUT)
																		.argument(nv.getName())
																		.build();
					argRef.activate(m_instance.getInstanceManager());
					outputArgs.put(nv.getName(), argRef.readValue());
				});
		operationInfo.put("outputArguments", outputArgs);
		
		return operationInfo;
	}
	
	public JsonNode toJsonNode() throws IOException {
		ObjectNode src = (ObjectNode)MDTModelSerDe.toJsonNode(m_desc);
		ObjectNode tar = MDTModelSerDe.MAPPER.createObjectNode();
		
		// copy all fields except 'operations', 'parameters', 'submodels' to the target node
		FStream.from(src.fieldNames())
				.filter(name -> !name.equals("operations") && !name.equals("parameters") && !name.equals("submodels"))
				.forEach(name -> tar.set(name, src.get(name)));
		
		MDTInstanceStatus status = m_instance.getStatus();
		tar.put("status", status.name());
		if ( status == MDTInstanceStatus.RUNNING ) {
			tar.put("baseEndpoint", m_instance.getServiceEndpoint());
		}
		else {
			tar.putNull("baseEndpoint");
		}
		tar.set("submodels", src.get("submodels"));
		
		if ( status == MDTInstanceStatus.RUNNING ) {
			Map<String,Object> parameterInfoMap = Maps.newHashMap();
			FStream.from(src.get("parameters").elements())
					.forEachOrThrow(paramNode -> {
						String paramName = paramNode.get("name").asText();
						ElementValue paramAsValue = readParameterValue(paramName);
						parameterInfoMap.put(paramName, paramAsValue);
					});
			JsonNode parameters = MDTModelSerDe.getJsonMapper().valueToTree(parameterInfoMap);
			tar.set("parameters", parameters);
			
			ArrayNode operations = (ArrayNode)src.get("operations");
			FStream.from(operations.elements())
			        .cast(ObjectNode.class)
					.forEachOrThrow(opNode -> {
						String name = opNode.get("name").asText();
                        
                        Map<String,ElementValue> inArgs
                        	= FStream.from(opNode.get("inputArguments").elements())
			                        	.map(arg -> arg.get("name").asText())
			                        	.zipWith(FStream.from(readOperationArgumentValueAll(name, MDTArgumentKind.INPUT)))
			                    		.mapToKeyValue(pair -> KeyValue.of(pair._1, pair._2))
			                    		.toMap();
                        opNode.set("inputArguments", MDTModelSerDe.getJsonMapper().valueToTree(inArgs));
                        
                        Map<String,ElementValue> outArgs
                    	= FStream.from(opNode.get("outputArguments").elements())
		                        	.map(arg -> arg.get("name").asText())
		                        	.zipWith(FStream.from(readOperationArgumentValueAll(name, MDTArgumentKind.OUTPUT)))
		                    		.mapToKeyValue(pair -> KeyValue.of(pair._1, pair._2))
		                    		.toMap();
                        opNode.set("outputArguments", MDTModelSerDe.getJsonMapper().valueToTree(outArgs));
					});
			tar.set("operations", src.get("operations"));
		}
		else {
			tar.set("parameters", src.get("parameters"));
			tar.set("operations", src.get("operations"));
		}
		
		return tar;
	}
	
	private MDTInstance toInstance(String compId) throws ResourceNotFoundException {
		CompositionItem comp = m_components.get().get(compId);
		if ( comp == null ) {
			throw new ResourceNotFoundException("CompositionItem", "id=" + compId);
		}

		MDTInstanceManager manager = m_instance.getInstanceManager();
		String aasId = comp.getReference();
		return manager.getInstanceByAasId(aasId);
	}
	private InformationModel loadInformationModel() {
		SubmodelService infoSvc = FStream.from(m_instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID))
								        .findFirst()
										.getOrThrow(() -> new ResourceNotFoundException("InformationModelService"));
        
        DefaultInformationModel infoModel = new DefaultInformationModel();
        infoModel.updateFromAasModel(infoSvc.getSubmodel());
        return infoModel;
	}
	
	private Map<String, CompositionItem> loadComponentMap() {
		TwinComposition tcomp = getInformationModel().getTwinComposition();
		return FStream.from(tcomp.getCompositionItems())
						.tagKey(item -> item.getID())
						.toMap();
	}

	private Map<String, MDTParameterDescriptor> loadParameterMap() {
		return FStream.from(m_desc.getMDTParameterDescriptorAll())
						.tagKey(MDTParameterDescriptor::getName)
						.toMap();
	}
	
	private Map<String, MDTOperationDescriptor> loadOperationMap() {
		return FStream.from(m_desc.getMDTOperationDescriptorAll())
						.tagKey(MDTOperationDescriptor::getName)
						.toMap();
	}
	
	private ElementValue readParameterValue(String paramName) throws IOException {
		MDTParameterReference ref = MDTParameterReference.newInstance(m_instance.getId(), paramName);
		ref.activate(m_instance.getInstanceManager());
		return ref.readValue();
	}
	
	public SubmodelElement readParameterSubmodelElement(MDTParameterDescriptor paramDesc) throws IOException {
		MDTParameterReference ref = MDTParameterReference.newInstance(m_instance.getId(), paramDesc.getName());
		ref.activate(m_instance.getInstanceManager());
		return ref.read();
	}
	
	private void addValues(ArrayNode args, List<ElementValue> values) {
		FStream.from(args.elements())
				.zipWith(FStream.from(values))
				.forEach(pair -> {
					ObjectNode argNode = (ObjectNode) pair._1;
					ElementValue argValue = pair._2;

					String valStr = (argValue != null) ? argValue.toString() : "";
					argNode.put("value", valStr);
				});
	}
}
