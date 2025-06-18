package mdt.model.sm;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
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
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Try;
import utils.stream.FStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;
import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTSubmodelReference;
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
			case 4 -> List.of(matches.get(1));
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
										String updatePath, SubmodelElement update) {
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
			case TimeSeries.SEMANTIC_ID -> "TimeSeries";
			default -> throw new InternalException("unknown Submodel " + semanticIdStr);
		};
	}
	
	public static boolean containsFieldById(SubmodelElementCollection smc, String fieldName) {
		return Funcs.findFirst(smc.getValue(), field -> field.getIdShort().equals(fieldName)).isPresent();
	}
	public static FOption<Indexed<SubmodelElement>> findFieldById(SubmodelElement smc, String fieldName) {
		if ( smc instanceof SubmodelElementCollection coll ) {
			return Funcs.findFirstIndexed(coll.getValue(), field -> field.getIdShort().equals(fieldName));
		}
		else {
			throw new IllegalArgumentException("Not a SubmodelElementCollection: " + smc.getClass());
		}
	}
	public static FOption<Indexed<Property>> findPropertyById(SubmodelElement smc, String fieldName) {
		return findFieldById(smc, fieldName)
				.filter(idxed -> idxed.value() instanceof Property)
				.map(idxed -> Indexed.with((Property) idxed.value(), idxed.index()));
	}
	public static Indexed<SubmodelElement> getFieldById(SubmodelElement smc, String fieldName)
		throws IllegalArgumentException {
		return findFieldById(smc, fieldName)
					.getOrThrow(() -> {
						String fieldNames = FStream.from(((SubmodelElementCollection)smc).getValue())
													.map(SubmodelElement::getIdShort)
													.join(", ");
						String msg = String.format("Failed to find the field '%s' from %s{%s}",
													fieldName, smc.getIdShort(), fieldNames);
						return new IllegalArgumentException(msg);
					});
	}
	public static <T extends SubmodelElement> Indexed<T> getFieldById(SubmodelElementCollection smc, String fieldName,
                                            						Class<T> outputClass) throws IllegalArgumentException {
		Indexed<SubmodelElement> idxed = getFieldById(smc, fieldName);
		return Indexed.with(cast(idxed.value(), outputClass), idxed.index());
	}
	
	public static Indexed<Property> getPropertyById(SubmodelElementCollection smc, String fieldName) {
		Indexed<SubmodelElement> idxed = getFieldById(smc, fieldName);
		if ( idxed.value() instanceof Property prop ) {
			return Indexed.with(prop, idxed.index());
		}
		else {
			throw new IllegalArgumentException("Not a Property: " + fieldName);
		}
	}
	public static Indexed<String> getPropertyValueById(SubmodelElement smc, String fieldName) {
		Indexed<SubmodelElement> idxed = getFieldById(smc, fieldName);
		if ( idxed.value() instanceof Property prop ) {
			return Indexed.with(prop.getValue(), idxed.index());
		}
		else {
			throw new IllegalArgumentException("Not a Property: " + fieldName);
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
	public static FOption<Indexed<SubmodelElementCollection>>
	findFieldSMCByIdValue(List<SubmodelElement> smeList, String fieldName, String value) {
		return FStream.from(smeList)
						.castSafely(SubmodelElementCollection.class)
						.zipWithIndex()
						.findFirst(idxed -> {
							SubmodelElementCollection smc = idxed.value();
							return findFieldById(smc, fieldName)
									.filter(isme -> {
										if ( isme.value() instanceof Property prop ) {
                                            return prop.getValue().equals(value);
                                        }
                                        else {
                                            return false;
                                        }
									})
									.isPresent();
						});
	}
	public static Indexed<SubmodelElementCollection>
	getFieldSMCByIdValue(List<SubmodelElement> smeList, String fieldName, String value) throws IllegalArgumentException {
		return findFieldSMCByIdValue(smeList, fieldName, value)
				.getOrThrow(() -> {
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
	
	public static boolean isDataSubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return Data.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isParameterValue(SubmodelElement sme) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sme.getSemanticId());
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
	
	public static boolean isAISubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return AI.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isSimulationSubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return Simulation.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isEquipment(SubmodelElement element) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(element.getSemanticId());
		return Equipment.SEMANTIC_ID.equals(semanticId);
	}
	
	public static boolean isOperation(SubmodelElement element) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(element.getSemanticId());
		return Operation.SEMANTIC_ID.equals(semanticId);
	}
	
	public static String getTypeString(SubmodelElement element) {
		if ( element instanceof Property prop ) {
			DataType<?> type = DataTypes.fromAas4jDatatype(prop.getValueType());
			return type.getId();
		}
		else if ( element instanceof File ) {
			return "File";
		}
		else if ( element instanceof SubmodelElementCollection ) {
			return "SubmodelElementCollection";
		}
		else if ( element instanceof SubmodelElementList ) {
			return "SubmodelElementList";
		}
		else if ( element instanceof MultiLanguageProperty ) {
			return "MultiLanguageProperty";
		}
		else if ( element instanceof Range ) {
			return "Range";
		}
		else {
			String msg = String.format("Unsupported SubmodelElement: type=%s", element.getClass());
			throw new IllegalArgumentException(msg);
		}
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
//			String refType = FOption.mapOrElse(node.get(DefaultSubmodelReference.FIELD_REFERENCE_TYPE), JsonNode::asText,
//												ElementReferenceType.DEFAULT.name());
//			
//			SubmodelReferenceType type = SubmodelReferenceType.fromName(refType);
//			return switch ( type ) {
//				case DEFAULT -> DefaultSubmodelReference.parseJson(root);
//				default -> throw new IllegalArgumentException("Unknown SubmodelReference type: " + type);
//			};
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
