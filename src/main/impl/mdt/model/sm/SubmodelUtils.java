package mdt.model.sm;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.experimental.UtilityClass;

import utils.CSV;
import utils.Indexed;
import utils.InternalException;
import utils.func.Funcs;
import utils.func.Try;
import utils.stream.FStream;

import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;
import mdt.model.MDTSemanticIds;
import mdt.model.ModelValidationException;
import mdt.model.ResourceNotFoundException;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.shape.Shape;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValues;
import mdt.model.timeseries.TimeSeries;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class SubmodelUtils {
	public static SubmodelElement duplicate(SubmodelElement sme) {
		try {
			String jsonStr = MDTModelSerDe.toJsonString(sme);
			return MDTModelSerDe.readValue(jsonStr, sme.getClass());
		}
		catch ( IOException e ) {
			throw new InternalException("Failed to duplicate: " + sme, e);
		}
	}
	
	public static FStream<String> parseIdShortPath(String idShortPath) {
		return CSV.parseCsv(idShortPath, '.')
					.flatMapIterable(seg -> parsePathSegment(seg));
	}
	private static final Pattern PATTERN = Pattern.compile("(\\w*)(\\[(d+)\\])?");
	private static List<String> parsePathSegment(String idShort) {
		Matcher matcher = PATTERN.matcher(idShort);
		List<String> matches = Lists.newArrayList();
		while ( matcher.find() ) {
			matches.add(matcher.group());
		}
		
		return switch ( matches.size() ) {
			case 0 -> matches;
			case 2 -> List.of(matches.get(0));
			case 4 -> List.of(matches.get(0));
			case 5 -> List.of(matches.get(0), matches.get(2));
			default -> throw new AssertionError();
		};
	}
	
	public static String buildIdShortPath(Iterable<String> segList) {
		StringBuilder builder = new StringBuilder();
		for ( String seg: segList ) {
			try {
				int idx = Integer.parseInt(seg);
				builder = builder.append(String.format("[%d]", idx));
			}
			catch ( NumberFormatException e ) {
				if ( !builder.isEmpty() ) {
					builder = builder.append('.');
				}
				builder = builder.append(seg);
			}
		}
		
		return builder.toString();
	}
	
	public static <T> T getPropertyValueByPath(Submodel submodel, String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = cast(traverse(submodel, idShortPath), Property.class);
		return getPropertyValue(prop, valueClass);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPropertyValue(Property prop, Class<T> cls) {
		Object value = DataTypes.fromAas4jDatatype(prop.getValueType()).parseValueString(prop.getValue());
		if ( cls.isAssignableFrom(value.getClass()) ) {
			return (T)value;
		}
		else {
			throw new IllegalArgumentException("Cannot cast to " + cls);
		}
	}

	public static <T> T getPropertyValueByPath(SubmodelElement start, String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = cast(traverse(start, idShortPath), Property.class);
		return getPropertyValue(prop, valueClass);
	}
	
	public static void updateSubBuffer(String bufferPath, SubmodelElement buffer,
										String updatePath, SubmodelElement update) throws IOException {
		String relPath = toRelativeIdShortPath(bufferPath, updatePath);
		SubmodelElement subBuffer = traverse(buffer, relPath);
		ElementValues.update(subBuffer, ElementValues.getValue(update));
	}
	
	/**
	 * Returns the relative idShort path from the ancestor to the descendant.
	 * 
	 * @param ancestor	Ancestor idShort path.
	 * @param descendant	Descendant idShort path.
	 * @return	relative idShort path from the ancestor to the descendant.
	 * 	            If the descendant is the same as the ancestor, it returns an empty string.
	 * 				{@code null} if the descendant is not a descendant of the ancestor.
	 */
	public static String toRelativeIdShortPath(String ancestor, String descendant) {
		if ( descendant.equals(ancestor) ) {
			return "";
		}
		if ( descendant.startsWith(ancestor) ) {
			int prefixLen = ancestor.length();
			char delim = descendant.charAt(prefixLen);
			if ( delim == '.' ) {
				++prefixLen;
			}
			return (descendant.length() > prefixLen) ? descendant.substring(prefixLen) : "";
		}
		else {
			return null;
		}
	}
	
	/**
	 * Traverse to the target SubmodelElement with the given idShort path.
	 * 
	 * @param root			Root SubmodelElement to start the traversal from.
	 * @param idShortPath	idShort path to traverse.
	 * @throws ResourceNotFoundException 경로에 해당하는 SubmodeElement가 존재하지 않는 경우.
	 */
	public static SubmodelElement traverse(SubmodelElement root, String idShortPath) throws ResourceNotFoundException {
		List<String> pathSegs = CSV.parseCsv(idShortPath, '.')
									.flatMapIterable(seg -> parsePathSegment(seg))
									.toList();
		SubmodelElement current = root;
		for ( String seg: pathSegs ) {
			current = hop(current, seg);
			if ( current == null ) {
				throw new ResourceNotFoundException("SubmodelElement", "idShortPath=" + idShortPath);
			}
		}
		
		return current;
	}
	
	public static <T extends SubmodelElement> T traverse(SubmodelElement start, String idShortPath, Class<T> targetClass)
		throws ResourceNotFoundException {
		return cast(traverse(start, idShortPath), targetClass);
	}
	
	/**
	 * Traverse the Submodel starting with the given idShort path.
	 * 
	 * @param submodel		탐색 대상 Submodel 객체.
	 * @param idShortPath	탐색 경로명.
	 * @throws ResourceNotFoundException 경로에 해당하는 SubmodeElement가 존재하지 않는 경우.
	 */
	public static SubmodelElement traverse(Submodel submodel, String idShortPath) throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodel, "Submodel was null");
		Preconditions.checkNotNull(idShortPath, "idShortPath was null");
		
		SubmodelElementCollection start = new DefaultSubmodelElementCollection.Builder()
													.value(submodel.getSubmodelElements())
													.build();
		return traverse(start, idShortPath);
	}
	
	public static <T extends SubmodelElement> T traverse(Submodel submodel, String idShortPath, Class<T> targetClass)
		throws ResourceNotFoundException {
		return cast(traverse(submodel, idShortPath), targetClass);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends SubmodelElement> T cast(SubmodelElement sme, Class<T> toClass) {
		if ( toClass.isAssignableFrom(sme.getClass()) ) {
			return (T)sme;
		}
		else {
			throw new IllegalArgumentException("Cannot cast to " + toClass);
		}
	}
	
	public static String getShortSubmodelSemanticId(String semanticIdStr) {
		if ( semanticIdStr == null ) {
			return "?";
		}
		
		return switch ( semanticIdStr ) {
			case InformationModel.SEMANTIC_ID -> "Info";
			case Data.SEMANTIC_ID -> "Data";
			case Simulation.SEMANTIC_ID -> "Sim";
			case AI.SEMANTIC_ID -> "AI";
			case TimeSeries.SEMANTIC_ID -> "TS";
			case Shape.SEMANTIC_ID -> "Shape";
			default -> throw new InternalException("unknown Submodel " + semanticIdStr);
		};
	}
	
	public static boolean containsFieldById(SubmodelElementCollection smc, String fieldName) {
		return Funcs.findFirst(smc.getValue(), field -> field.getIdShort().equals(fieldName)).isPresent();
	}
	public static Optional<SubmodelElement> findFieldById(SubmodelElement smc, String fieldName) {
		if ( smc instanceof SubmodelElementCollection coll ) {
			return Funcs.findFirst(coll.getValue(), field -> field.getIdShort().equals(fieldName));
		}
		else {
			throw new IllegalArgumentException("Not a SubmodelElementCollection: " + smc.getClass());
		}
	}
	public static <T extends SubmodelElement> Optional<Indexed<T>>
	findFieldById(SubmodelElement smc, String fieldName, Class<T> outputClass) {
		if ( smc instanceof SubmodelElementCollection coll ) {
			return Funcs.findFirstIndexed(coll.getValue(), field -> field.getIdShort().equals(fieldName))
						.map(idxed -> Indexed.with(cast(idxed.value(), outputClass), idxed.index()));
		}
		else {
			throw new IllegalArgumentException("Not a SubmodelElementCollection: " + smc.getClass());
		}
	}
	public static Optional<Property> findPropertyById(SubmodelElement smc, String fieldName) {
		return findFieldById(smc, fieldName)
				.filter(field -> field instanceof Property)
				.map(field -> (Property)field);
	}
	public static SubmodelElement getFieldById(SubmodelElement smc, String fieldName)
		throws IllegalArgumentException {
		return findFieldById(smc, fieldName)
					.orElseThrow(() -> {
						String fieldNames = FStream.from(((SubmodelElementCollection)smc).getValue())
													.map(SubmodelElement::getIdShort)
													.join(", ");
						String msg = String.format("Failed to find the field '%s' from %s{%s}",
													fieldName, smc.getIdShort(), fieldNames);
						return new IllegalArgumentException(msg);
					});
	}
	public static <T extends SubmodelElement> T getFieldById(SubmodelElement smc, String fieldName,
                                    						Class<T> outputClass) throws IllegalArgumentException {
		SubmodelElement field = getFieldById(smc, fieldName);
		return cast(field, outputClass);
	}
	
	public static Property getPropertyFieldById(SubmodelElement smc, String fieldName) {
		Preconditions.checkArgument(smc instanceof SubmodelElementCollection,
									"Not a SubmodelElementCollection: " + smc.getClass());
		
		SubmodelElement field = getFieldById(smc, fieldName);
		if ( field instanceof Property prop) {
			return prop;
		}
		else {
			throw new IllegalArgumentException("Not a Property: " + fieldName);
		}
	}
	public static String getStringFieldById(SubmodelElement smc, String fieldName) {
		Preconditions.checkArgument(smc instanceof SubmodelElementCollection,
									"Not a SubmodelElementCollection: " + smc.getClass());
		
		SubmodelElement field = getFieldById(smc, fieldName);
		if ( field instanceof Property prop && prop.getValueType() == DataTypeDefXsd.STRING) {
			return prop.getValue();
		}
		else {
			throw new IllegalArgumentException("Not a Property(xs:string): " + fieldName);
		}
	}

	public static void removeFieldById(SubmodelElementList sml, String idShort) {
		sml.getValue().removeIf(field -> field.getIdShort().equals(idShort));
	}
	public static void removeFieldById(SubmodelElementCollection smc, String idShort) {
		smc.getValue().removeIf(field -> field.getIdShort().equals(idShort));
	}
	public static void replaceFieldbyId(SubmodelElementList sml, String idShort, SubmodelElement newField) {
		List<SubmodelElement> fieldList = sml.getValue();
		for ( int i = 0; i < fieldList.size(); ++i ) {
			SubmodelElement field = fieldList.get(i);
			if ( field.getIdShort().equals(idShort) ) {
				fieldList.set(i, newField);
				return;
			}
		}
	}
	public static void replaceFieldbyId(SubmodelElementCollection smc, String idShort, SubmodelElement newField) {
		List<SubmodelElement> fieldList = smc.getValue();
		for ( int i = 0; i < fieldList.size(); ++i ) {
			SubmodelElement field = fieldList.get(i);
			if ( field.getIdShort().equals(idShort) ) {
				fieldList.set(i, newField);
				return;
			}
		}
	}
	
	/**
	 * 주어진 SubmodelElement의 리스트에 포함된 SubmodelElementCollection (SMC)들 중에서
	 * SubmodelElementCollection의 하위 요소 중에서 idShort가 'fieldName'인 Property를 찾아서
	 * 그 값이 'value'인 SubmodelElementCollection을 찾아서 반환한다.
	 *
	 * @param smeList	검색 대상 SubmodelElement 리스트.
	 * @param fieldName	필드 이름.
	 * @param value		찾을 값.
	 * @return	검색된 SubmodelElementCollection
	 */
	public static Optional<Indexed<SubmodelElementCollection>>
	findFieldSMCByIdValue(List<SubmodelElement> smeList, String fieldName, String value) {
		return FStream.from(smeList)
						.castSafely(SubmodelElementCollection.class)
						.zipWithIndex()
						.findFirst(idxed -> {
							SubmodelElementCollection smc = idxed.value();
							return findFieldById(smc, fieldName)
									.filter(isme -> {
										if ( isme instanceof Property prop ) {
                                            return prop.getValue().equals(value);
                                        }
                                        else {
                                            return false;
                                        }
									})
									.isPresent();
						})
						.toOptional();
	}
	public static Indexed<SubmodelElementCollection>
	getFieldSMCByIdValue(List<SubmodelElement> smeList, String fieldName, String value) throws IllegalArgumentException {
		return findFieldSMCByIdValue(smeList, fieldName, value)
				.orElseThrow(() -> {
					String msg = String.format("Failed to find SMC of %s=%s", fieldName, value);
					return new IllegalArgumentException(msg);
				});
	}
	
	public static DefaultSubmodelElementCollection newSubmodelElementCollection(String idShort,
																				List<SubmodelElement> values) {
		return new DefaultSubmodelElementCollection.Builder()
													.idShort(idShort)
													.value(values)
													.build();
	}
	public static DefaultSubmodelElementList newSubmodelElementList(String idShort, List<SubmodelElement> values) {
		return new DefaultSubmodelElementList.Builder()
												.idShort(idShort)
												.orderRelevant(true)
												.value(values)
												.build();
	}

	public static String getSemanticIdStringOrNull(Reference semanticId) {
		return (semanticId != null) ? semanticId.getKeys().get(0).getValue() : null;
	}
	
	public static boolean isInformationModel(Submodel sm) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return InformationModel.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isDataSubmodel(Submodel sm) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return Data.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isTimeSeriesSubmodel(Submodel sm) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return TimeSeries.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isParameterValue(SubmodelElement sme) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(sme.getSemanticId());
		return ParameterValue.SEMANTIC_ID.equals(semanticId);
	}
	
	public static String getParameterValuePrefix(Submodel dataSubmodel) {
		SubmodelElementCollection dataInfo = traverse(dataSubmodel, "DataInfo", SubmodelElementCollection.class);
		if ( containsFieldById(dataInfo, "Equipment") ) {
			return "DataInfo.Equipment.EquipmentParameterValues";
		}
		else if ( containsFieldById(dataInfo, "Operation") ) {
			return "DataInfo.Operation.OperationParameterValues";
		}
		else {
			throw new IllegalArgumentException("Invalid DataInfo: " + dataInfo.getIdShort());
		}
	}
	
	public static String resolveParameterValueElementPath(String paramPathPrefix, String paramExpr,
															Function<String,Integer> resolveParameterIndex) {
		// parameter expression에서 parameter ID를 추출하여 해당 parameter의 인덱스를 구한다.
		String subPath = "";
		int paramIdx;
		try {
			// 일단 parmeter-id가 숫자인 것으로 가정하고 파싱을 실시하여
			// parameter의 idShortPath를 생성하고, 숫자가 아니어서 예외가 발생한 경우에는
			// 일반적인 id 기반의 idShortPath를 생성한다.
			paramIdx = Integer.parseInt(paramExpr);
		}
		catch ( NumberFormatException e ) {
			String paramId;
			int idx = paramExpr.indexOf('.');
			if ( idx >= 0 ) {
				paramId = paramExpr.substring(0, idx);
				subPath = paramExpr.substring(idx);
			}
			else {
				idx = paramExpr.indexOf('[');
				if ( idx >= 0 ) {
					paramId = paramExpr.substring(0, idx);
					subPath = paramExpr.substring(idx);
				}
				else {
					paramId = paramExpr;
				}
			}
			paramIdx = resolveParameterIndex.apply(paramId);
		}
		return String.format("%s[%d].ParameterValue%s", paramPathPrefix, paramIdx, subPath);
	}
	
	public static boolean isAISubmodel(Submodel sm) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return AI.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isSimulationSubmodel(Submodel sm) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return Simulation.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isEquipment(SubmodelElement element) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(element.getSemanticId());
		return Equipment.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isAASOperation(SubmodelElement element) {
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(element.getSemanticId());
		return Operation.SEMANTIC_ID.equals(semanticId);
	}
	
	public static class OperationSubmodelDescriptor {
		private final String m_id;
		private final String m_semanticIdString;
		private final Map<String,SubmodelArgumentDescriptor> m_inputs;
		private final Map<String,SubmodelArgumentDescriptor> m_outputs;
		
		public OperationSubmodelDescriptor(String id, String semanticIdStr,
											Map<String,SubmodelArgumentDescriptor> inputs,
											Map<String,SubmodelArgumentDescriptor> outputs) {
			m_id = id;
			m_semanticIdString = semanticIdStr;
			m_inputs = inputs;
			m_outputs = outputs;
		}
		
		public String getId() {
			return m_id;
		}
		
		public String getSemanticIdString() {
			return m_semanticIdString;
		}
		
		public Map<String,SubmodelArgumentDescriptor> getInputs() {
			return m_inputs;
		}
		
		public Map<String,SubmodelArgumentDescriptor> getOutputs() {
			return m_outputs;
		}
	}
	
	public static class SubmodelArgumentDescriptor {
		private final String m_id;
		private final int m_index;
		private final String m_idShortPath;
		private final SubmodelElement m_sme;
		
		public SubmodelArgumentDescriptor(String id, int index, String idShortPath, SubmodelElement sme) {
			m_id = id;
			m_index = index;
			m_idShortPath = idShortPath;
			m_sme = sme;
		}
		
		public String getId() {
			return m_id;
		}
		
		public int getIndex() {
			return m_index;
		}
		
		public String idShortPath() {
			return m_idShortPath;
		}
		
		public SubmodelElement getSubmodelElement() {
			return m_sme;
		}
	}
	
	public static OperationSubmodelDescriptor loadOperationSubmodelDescriptor(Submodel submodel) {
		// 입출력 인자 SubmodelElement를 가져온다.
		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
		if ( semanticId == null ) {
			throw new ModelValidationException("Submodel semanticId is missing: submodel idShort="
                                                + submodel.getIdShort());
		}
		String pathPrefix = switch ( semanticId ) {
            case MDTSemanticIds.SUBMODEL_AI -> "AIInfo";
            case MDTSemanticIds.SUBMODEL_SIMULATION -> "SimulationInfo";
            default ->
                throw new ModelValidationException("Unsupported Operation Submodel semanticId: " + semanticId);
        };
        SubmodelElementList inputsSml = traverse(submodel, pathPrefix + ".Inputs", SubmodelElementList.class);
        SubmodelElementList outputsSml = traverse(submodel, pathPrefix + ".Outputs", SubmodelElementList.class);
        
        return new OperationSubmodelDescriptor(submodel.getIdShort(), semanticId,
								        		loadArgumentList(inputsSml, pathPrefix + ".Inputs", "Input"),
								        		loadArgumentList(outputsSml, pathPrefix + ".Outputs", "Output"));
	}
	private Map<String,SubmodelArgumentDescriptor> loadArgumentList(SubmodelElementList argSmcList,
																	String idShortPathPrefix, String argKind) {
		return FStream.from(argSmcList.getValue())
				.zipWithIndex()
				.map(idxed -> {
					Property idProp = SubmodelUtils.traverse(idxed.value(), argKind+"ID", Property.class);
					SubmodelElement argValue = SubmodelUtils.traverse(idxed.value(), argKind+"Value");
					String idShortPath = String.format("%s[%d].%sValue", idShortPathPrefix, idxed.index(), argKind);
					return new SubmodelArgumentDescriptor(idProp.getValue(), idxed.index(), idShortPath, argValue);
				})
				.tagKey(SubmodelArgumentDescriptor::getId)
				.toMap();
	}
	
	private static final class SMETypeDesc {
		private final String m_name;
		private final AasSubmodelElements m_type;
		private final Class<? extends SubmodelElement> m_elementClass;
		
		SMETypeDesc(String name, AasSubmodelElements type,
								Class<? extends SubmodelElement> elementCls) {
			m_name = name;
			m_type = type;
			m_elementClass = elementCls;
		}
		
		public String getName() {
			return m_name;
		}
		
		public AasSubmodelElements getType() {
			return m_type;
		}
		
		public Class<? extends SubmodelElement> getElementClass() {
			return m_elementClass;
		}
	};
	
	private static final List<SMETypeDesc> TYPE_DESCS = Lists.newArrayList();
	static {
		TYPE_DESCS.add(new SMETypeDesc("Property", AasSubmodelElements.PROPERTY, Property.class));
		TYPE_DESCS.add(new SMETypeDesc("SubmodelElementCollection", AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION,
										SubmodelElementCollection.class));
		TYPE_DESCS.add(new SMETypeDesc("SubmodelElementList", AasSubmodelElements.SUBMODEL_ELEMENT_LIST,
										SubmodelElementList.class));
		TYPE_DESCS.add(new SMETypeDesc("File", AasSubmodelElements.FILE, File.class));
		TYPE_DESCS.add(new SMETypeDesc("MultiLanguageProperty", AasSubmodelElements.MULTI_LANGUAGE_PROPERTY,
										MultiLanguageProperty.class));
		TYPE_DESCS.add(new SMETypeDesc("Range", AasSubmodelElements.RANGE, Range.class));
		TYPE_DESCS.add(new SMETypeDesc("Operation", AasSubmodelElements.OPERATION,
										org.eclipse.digitaltwin.aas4j.v3.model.Operation.class));
		TYPE_DESCS.add(new SMETypeDesc("AnnotatedRelationshipElement", AasSubmodelElements.ANNOTATED_RELATIONSHIP_ELEMENT,
										AnnotatedRelationshipElement.class));
		TYPE_DESCS.add(new SMETypeDesc("Blob", AasSubmodelElements.BLOB, Blob.class));
		TYPE_DESCS.add(new SMETypeDesc("Capability", AasSubmodelElements.CAPABILITY, Capability.class));
		TYPE_DESCS.add(new SMETypeDesc("Entity", AasSubmodelElements.ENTITY, Entity.class));
		TYPE_DESCS.add(new SMETypeDesc("RelationshipElement",
												AasSubmodelElements.RELATIONSHIP_ELEMENT, RelationshipElement.class));
	};
	
	private static SMETypeDesc getTypeDescriptor(SubmodelElement element) {
		return FStream.from(TYPE_DESCS)
						.findFirst(desc -> desc.m_elementClass.isInstance(element))
						.getOrThrow(() -> new IllegalArgumentException("Unknown SubmodelElement type: " + element.getClass()));
	}
	
	public static AasSubmodelElements getSubmodelElementType(SubmodelElement element) {
		return getTypeDescriptor(element).getType();
	}
	
	public static String getValueTypeString(SubmodelElement element) {
		SMETypeDesc desc = getTypeDescriptor(element);
		return switch ( desc.m_name ) {
			case "Property" -> DataTypes.fromAas4jDatatype(((Property)element).getValueType()).getId();
			default -> desc.m_name;
		};
	}

	private static @Nullable SubmodelElement hop(SubmodelElement sme, String seg) {
		if ( sme instanceof SubmodelElementCollection smc ) {
			return FStream.from(smc.getValue())
							.findFirst(e ->  e.getIdShort() != null && e.getIdShort().equals(seg))
							.getOrNull();
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			// Navigate 대상이 SubmodelElementList인 경우에는 'seg'가 숫자일 수도 있기 때문에
			// integer로의 파싱을 시도한다. 만일 숫자가 아닌 경우에는 'idx'가 -1로 설정된다.
			int idx = Try.get(() -> Integer.parseInt(seg)).getOrElse(-1);
			if ( idx >= 0 ) {
				if ( sml.getValue().size() > idx ) {
					return sml.getValue().get(idx);
				}
				else {
					return null;
				}
			}
			else {
				return FStream.from(sml.getValue())
								.findFirst(e ->  e.getIdShort() != null && e.getIdShort().equals(seg))
								.getOrNull();
			}
		}
		
		return null;
	}

	private static final Pattern REF_TYPE_PATTERN = Pattern.compile("\\w+(\\-(\\S+))?");
	public static Reference parseReferenceSerizalization(String serialized) {
		serialized = serialized.trim();
		
		Reference semanticId = null;
		ReferenceTypes refType = null;
		if ( serialized.charAt(0) == '[' ) {
			int idx = serialized.indexOf(']');
			String refTypeStr = serialized.substring(1, idx).trim();
			serialized = serialized.substring(idx+1);
			
			idx = refTypeStr.indexOf('-');
			if ( idx >= 0 ) {
				String semanticIdStr = null;
				String realRefTypeStr = refTypeStr.substring(0, idx).trim();
				switch ( realRefTypeStr ) {
					case "ModelRef":
						refType = ReferenceTypes.MODEL_REFERENCE;
						semanticIdStr = refTypeStr.substring(idx+1, refTypeStr.lastIndexOf('-')).trim();
						break;
					case "ExternalRef":
						refType = ReferenceTypes.EXTERNAL_REFERENCE;
						semanticIdStr = refTypeStr.substring(idx+1, refTypeStr.lastIndexOf('-')).trim();
						break;
					default:
						throw new IllegalArgumentException("invalid ReferenceTypes: " + refTypeStr);
				}
				if ( semanticIdStr != null ) {
					semanticId = parseReferenceSerizalization(semanticIdStr);
				}
			}
			
			Matcher matcher = REF_TYPE_PATTERN.matcher(refTypeStr);
			if ( !matcher.find() ) {
				throw new IllegalArgumentException("Invalid ReferenceTypes: " + refTypeStr);
			}
			
			switch ( matcher.group(0) ) {
				case "ModelRef":
					refType = ReferenceTypes.MODEL_REFERENCE;
					break;
				default:
					refType = ReferenceTypes.EXTERNAL_REFERENCE;
					break;
			}
		}
		
		List<Key> keyList = parseKeyListSerialization(serialized);
		if ( refType == null ) {
			if ( keyList.get(0).getType() == KeyTypes.GLOBAL_REFERENCE ) {
				refType = ReferenceTypes.EXTERNAL_REFERENCE;
			}
			else {
				refType = ReferenceTypes.MODEL_REFERENCE;
			}
		}
		
		return new DefaultReference.Builder()
									.keys(keyList)
									.type(refType)
									.referredSemanticId(semanticId)
									.build();
	}
	
	private static List<Key> parseKeyListSerialization(String str) {
		return CSV.parseCsv(str).map(keyStr -> parseKeySerialization(keyStr)).toList();
	}

	private static final Pattern KEY_SER_PATTERN = Pattern.compile("\\((\\w+)\\)(\\S+)");
	private static Key parseKeySerialization(String str) {
		Matcher matcher = KEY_SER_PATTERN.matcher(str);
		matcher.find();
		
		KeyTypes type = KEY_TYPES_MAP.get(matcher.group(1));
		return new DefaultKey.Builder().type(type).value(matcher.group(2)).build();
	}
	
	private static final Map<String,KeyTypes> KEY_TYPES_MAP = Maps.newHashMap();
	static {
		KEY_TYPES_MAP.put("AnnotatedRelationshipElement", KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT);
		KEY_TYPES_MAP.put("AssetAdministrationShell", KeyTypes.ASSET_ADMINISTRATION_SHELL);
		KEY_TYPES_MAP.put("BaseEventElement", KeyTypes.BASIC_EVENT_ELEMENT);
		KEY_TYPES_MAP.put("Blob", KeyTypes.BLOB);
		KEY_TYPES_MAP.put("Capability", KeyTypes.CAPABILITY);
		KEY_TYPES_MAP.put("ConceptDescription", KeyTypes.CONCEPT_DESCRIPTION);
		KEY_TYPES_MAP.put("DataElement", KeyTypes.DATA_ELEMENT);
		KEY_TYPES_MAP.put("Entity", KeyTypes.ENTITY);
		KEY_TYPES_MAP.put("EventElement", KeyTypes.EVENT_ELEMENT);
		KEY_TYPES_MAP.put("File", KeyTypes.FILE);
		KEY_TYPES_MAP.put("FragmentReference", KeyTypes.FRAGMENT_REFERENCE);
		KEY_TYPES_MAP.put("GlobalReference", KeyTypes.GLOBAL_REFERENCE);
		KEY_TYPES_MAP.put("Identifiable", KeyTypes.IDENTIFIABLE);
		KEY_TYPES_MAP.put("MultiLanguageProperty", KeyTypes.MULTI_LANGUAGE_PROPERTY);
		KEY_TYPES_MAP.put("Operation", KeyTypes.OPERATION);
		KEY_TYPES_MAP.put("Property", KeyTypes.PROPERTY);
		KEY_TYPES_MAP.put("Range", KeyTypes.RANGE);
		KEY_TYPES_MAP.put("Referable", KeyTypes.REFERABLE);
		KEY_TYPES_MAP.put("ReferenceElement", KeyTypes.REFERENCE_ELEMENT);
		KEY_TYPES_MAP.put("RelationshipElement", KeyTypes.RELATIONSHIP_ELEMENT);
		KEY_TYPES_MAP.put("Submodel", KeyTypes.SUBMODEL);
		KEY_TYPES_MAP.put("SubmodelElement", KeyTypes.SUBMODEL_ELEMENT);
		KEY_TYPES_MAP.put("SubmodelElementCollection", KeyTypes.SUBMODEL_ELEMENT_COLLECTION);
		KEY_TYPES_MAP.put("SubmodelElementList", KeyTypes.SUBMODEL_ELEMENT_LIST);
	}


	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<MDTSubmodelReference> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public MDTSubmodelReference deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode node = parser.getCodec().readTree(parser);
			
			Preconditions.checkState(node instanceof ObjectNode);
			ObjectNode root = (ObjectNode)node;
			
			return DefaultSubmodelReference.parseJson(root);
		}
	}

	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<MDTSubmodelReference> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<MDTSubmodelReference> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(MDTSubmodelReference ref, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			ref.serialize(gen);
		}
	}
}
