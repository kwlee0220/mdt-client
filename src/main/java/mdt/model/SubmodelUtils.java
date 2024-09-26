package mdt.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.CSV;
import utils.func.Try;
import utils.func.Tuple;
import utils.stream.FStream;

import lombok.experimental.UtilityClass;
import mdt.model.resource.value.MultiLanguagePropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class SubmodelUtils {
	public static Property newProperty(String id, DataTypeDefXsd valueType, @Nullable String value) {
		return new DefaultProperty.Builder()
									.idShort(id)
									.value(value)
									.valueType(valueType)
									.build();
	}
	
	public static Property newStringProperty(String id, @Nullable String value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), value);
	}
	public static Property newStringProperty(String id, @Nullable Object value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), (value != null) ? value.toString() : null);
	}

	public static Property newIntProperty(String id, @Nullable int value) {
		return newProperty(id, DataTypes.INT.getTypeDefXsd(), DataTypes.INT.toValueString(value));
	}

	public static Property newLongProperty(String id, @Nullable Long value) {
		return newProperty(id, DataTypes.LONG.getTypeDefXsd(), DataTypes.LONG.toValueString(value));
	}

	public static Property newDateTimeProperty(String id, @Nullable Instant value) {
		return newProperty(id, DataTypes.DATE_TIME.getTypeDefXsd(), DataTypes.DATE_TIME.toValueString(value));
	}

	public static Property newDateProperty(String id, @Nullable Date value) {
		return newProperty(id, DataTypes.DATE.getTypeDefXsd(), DataTypes.DATE.toValueString(value));
	}

	public static Property newDurationProperty(String id, @Nullable Duration value) {
		return newProperty(id, DataTypes.DURATION.getTypeDefXsd(), DataTypes.DURATION.toValueString(value));
	}

	public static MultiLanguageProperty newMultiLanguageProperty(String id, MultiLanguagePropertyValue value) {
		List<LangStringTextType> textList
				= FStream.from(value.get())
							.map(langText -> new DefaultLangStringTextType.Builder()
																			.language(langText.getLanguage())
																			.text(langText.getText())
																			.build())
							.cast(LangStringTextType.class)
							.toList();
		return new DefaultMultiLanguageProperty.Builder()
						.idShort(id)
						.value(textList)
						.build();
	}
	
	public static FStream<String> parseIdShortPath(String idShortPath) {
		return CSV.parseCsv(idShortPath, '.')
					.flatMapIterable(seg -> parsePathSegment(seg));
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
	
	public static String toRelativeIdShortPath(String ancestor, String descendant) {
		if ( descendant.startsWith(ancestor) ) {
			int prefixLen = ancestor.length();
			return (descendant.length() > prefixLen) ? descendant.substring(prefixLen + 1) : "";
		}
		else {
			return null;
		}
	}
	
	public static SubmodelElement traverse(SubmodelElement root, String idShortPath) {
		SubmodelElement current = root;
		for ( String seg: CSV.parseCsv(idShortPath, '.')
								.flatMapIterable(seg -> parsePathSegment(seg)) ) {
			current = hop(current, seg);
			if ( current == null ) {
				break;
			}
		}
		
		return current;
	}
	
	public static SubmodelElement traverse(Submodel submodel, String idShortPath) throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodel, "Submodel was null");
		Preconditions.checkNotNull(idShortPath, "idShortPath was null");
		
		SubmodelElementCollection start = new DefaultSubmodelElementCollection.Builder()
													.value(submodel.getSubmodelElements())
													.build();
		return traverse(start, idShortPath);
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
	
	public Tuple<String,String> parseSubmodelReference(Reference ref) {
		Preconditions.checkArgument(ref != null);
		Preconditions.checkArgument(ref.getKeys().size() > 1, "Empty Reference");
		
		Key submodelKey = ref.getKeys().get(0);
		Preconditions.checkArgument(submodelKey.getType() == KeyTypes.SUBMODEL,
									"The first key should be 'SUBMODEL'");
		String idShortPath = FStream.from(ref.getKeys())
									.drop(1)
									.map(Key::getValue)
									.join('.');
		return Tuple.of(submodelKey.getValue(), idShortPath);
	}
	
	public static Tuple<String,List<String>> parseModelReference(Reference ref) {
		if ( ref.getType() == ReferenceTypes.MODEL_REFERENCE ) {
			List<Key> keys = ref.getKeys();
			if ( keys.get(0).getType() != KeyTypes.SUBMODEL ) {
				String msg = String.format("Unexpected type for the first key: type=%s", keys.get(0).getType());
				throw new IllegalArgumentException(msg);
			}
			
			ArrayList<String> pathSegs = Lists.newArrayList();
			for ( int i =0; i < keys.size(); ++i ) {
				Key key = keys.get(i);
				switch ( key.getType() ) {
					case ASSET_ADMINISTRATION_SHELL:
					case IDENTIFIABLE:
					case MULTI_LANGUAGE_PROPERTY:
					case REFERABLE:
					case SUBMODEL:
						String msg = String.format("Unexpected type: type=%s", key.getType());
						throw new IllegalArgumentException(msg);
					default:
						try {
							int idx = Integer.parseInt(key.getValue());
							if ( i > 1 ) {
								if ( keys.get(i).getType() == KeyTypes.SUBMODEL_ELEMENT_LIST ) {
									String last = pathSegs.remove(pathSegs.size()-1);
									last = String.format("%s[%d]", last, idx);
									pathSegs.add(last);
								}
							}
							else {
								String msg2 = String.format("Invalid reference key=%s", key.getValue());
								throw new IllegalArgumentException(msg2);
							}
						}
						catch ( NumberFormatException e ) {
							pathSegs.add(key.getValue());
						}
						break;
				}
			}
			
			return Tuple.of(keys.get(0).getValue(), pathSegs);
		}
		else {
			String msg = String.format("Not ModelReference: type=%s", ref.getType());
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
				return sml.getValue().get(idx);
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
		
		List<Key> keyList = parparseKeyListSerialization(serialized);
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
	
	private static List<Key> parparseKeyListSerialization(String str) {
		return CSV.parseCsv(str).map(keyStr -> parseKeySerialization(keyStr)).toList();
	}

	private static final Pattern KEY_SER_PATTERN = Pattern.compile("\\((\\w+)\\)(\\S+)");
	private static Key parseKeySerialization(String str) {
		Matcher matcher = KEY_SER_PATTERN.matcher(str);
		matcher.find();
		
		KeyTypes type = KEY_TYPES_MAP.get(matcher.group(1));
		return new DefaultKey.Builder().type(type).value(matcher.group(2)).build();
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
	
}
