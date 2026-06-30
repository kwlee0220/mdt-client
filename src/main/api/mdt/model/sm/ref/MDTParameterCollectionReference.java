package mdt.model.sm.ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.Preconditions;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.ModelValidationException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.info.MDTAssetType;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;
import mdt.model.sm.value.PropertyValue;


/**
 * MDT 인스턴스의 파라미터 전체 집합을 가리키는 {@link ElementReference} 구현체이다.
 * <p>
 * 대상 인스턴스의 {@code Data} Submodel 안에 있는 파라미터 목록(에셋 종류에 따라
 * {@code DataInfo.Equipment.EquipmentParameterValues} 또는
 * {@code DataInfo.Operation.OperationParameterValues})을 참조하며, 읽기/쓰기 시
 * {@code ParameterID}/{@code ParameterValue} 쌍으로 구성된 내부 표현과
 * {@code idShort -> 값} 형태의 컬렉션 표현 사이를 변환한다.
 * <p>
 * Json 직렬화 시 {@link #SERIALIZATION_TYPE}({@code "mdt:ref:param"}) 타입으로 식별된다.
 * 이 타입은 {@link MDTParameterReference}와 공유되며, {@code parameterExpr} 필드 값이
 * {@code "*"}인 경우 이 클래스(전체 집합)로, 그 외에는 {@link MDTParameterReference}(개별 파라미터)로
 * 역직렬화된다.
 * <p>
 * 값을 읽거나 쓰기 전에 {@link #activate(MDTInstanceManager)}로 활성화해야 하며, 파라미터 집합
 * 참조는 첨부파일 입출력을 지원하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterCollectionReference extends SubmodelBasedElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:param";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_PARAMETER_EXPR = "parameterExpr";
	
	private MDTParameterCollectionReference(String instanceId) {
		super(DefaultSubmodelReference.ofIdShort(instanceId, "Data"));
	}
	
	/**
	 * 참조가 가리키는 파라미터 집합을 읽어 {@link SubmodelElementList}로 반환한다.
	 * <p>
	 * 각 멤버의 {@code ParameterValue} 요소를 꺼내 그 idShort를 {@code ParameterID} 값으로
	 * 설정한 뒤 목록으로 반환한다.
	 *
	 * @return 파라미터 값들을 담은 {@link SubmodelElementList}.
	 * @throws IOException 읽기 과정에서 예외가 발생하거나, 대상이 {@link SubmodelElementList}가
	 * 				아닌 경우.
	 */
	@Override
	public SubmodelElement read() throws IOException {
		try {
			SubmodelElement sme = super.read();
			if ( !(sme instanceof SubmodelElementList) ) {
				String msg = String.format("Invalid MDTParameterCollection read from %s (not List): %s",
											toStringExpr(), sme);
				throw new IOException(msg);
			}

			SubmodelElementList sml = (SubmodelElementList)sme;
			List<SubmodelElement> members = FStream.from(sml.getValue())
													.mapOrThrow(elm -> {
														var kv = toMemberElement(elm);
														kv.value().setIdShort(kv.key());
														return kv.value();
													})
													.toList();
			sml.setValue(members);
			return sml;
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to read Parameter(%s), cause=%s", toStringExpr(), e.getMessage());
			throw new IOException(msg, e);
		}
	}
	private KeyValue<String,SubmodelElement> toMemberElement(SubmodelElement elm) throws IOException {
		if ( !(elm instanceof SubmodelElementCollection) ) {
			throw new IOException("Invalid MDTParameterCollection member element type: "
									+ elm.getClass().getName());
		}
		SubmodelElementCollection member = (SubmodelElementCollection)elm;
		
		String memberId = SubmodelUtils.getStringFieldById(member, "ParameterID");
		SubmodelElement memberElm = SubmodelUtils.getFieldById(member, "ParameterValue");
		return KeyValue.of(memberId, memberElm);
	}
	
	@Override
	public ElementValue readValue() throws IOException {
		ElementValue smev = super.readValue();
		if ( smev instanceof ElementListValue smelv ) {
			LinkedHashMap<String,ElementValue> paramValues = Maps.newLinkedHashMap();
			for ( ElementValue member : smelv.getElementValues() ) {
				KeyValue<String,ElementValue> kv = toMemberValue(member);
				paramValues.put(kv.key(), kv.value());
			}
			
			return new ElementCollectionValue(paramValues);
		}
		else {
			String msg = String.format("Invalid MDTParameterCollection value read from %s (not List): %s",
										toStringExpr(), smev);
			throw new IOException(msg);
		}
	}
	private KeyValue<String,ElementValue> toMemberValue(ElementValue member) throws IOException {
		if ( !(member instanceof ElementCollectionValue) ) {
			throw new IOException("Invalid MDTParameterCollection member value type: "
									+ member.getClass().getName());
		}
		ElementCollectionValue cv = (ElementCollectionValue)member;
		
		ElementValue vfield = cv.getField("ParameterID");
		if ( vfield instanceof PropertyValue<?> pidValue ) {
			String paramId = (String)pidValue.toValueObject();
			return KeyValue.of(paramId, cv.getField("ParameterValue"));
		}
		throw new IOException(String.format("Invalid ParameterID value type: %s",
											vfield.getClass().getName()));
	}
	
	@Override
	public void write(SubmodelElement sme) throws IOException {
		Preconditions.checkNotNullArgument(sme, "sme is null");
		Preconditions.checkArgument(sme instanceof SubmodelElementList,
									"sme should be a SubmodelElementList: %s", sme);

		SubmodelElementList smel = (SubmodelElementList)sme;
		LinkedHashMap<String,SubmodelElement> valueMap
								= FStream.from(smel.getValue())
										.mapOrThrow(elm -> KeyValue.of(elm.getIdShort(), elm))
										.mapToKeyValue(kv -> kv)
										.toMap(Maps.newLinkedHashMap());
		
		SubmodelElement baseSme = super.read();
		if ( !(baseSme instanceof SubmodelElementList) ) {
			String msg = String.format("Invalid MDTParameterCollection read from %s (not List): %s",
										toStringExpr(), baseSme);
			throw new IOException(msg);
		}
		SubmodelElementList paramValues = (SubmodelElementList)baseSme;
		FStream.from(paramValues.getValue())
				.forEachOrThrow(paramValue -> {
					String memberId = SubmodelUtils.getFieldById(paramValue, "ParameterID", Property.class)
													.getValue();
					SubmodelElement newValue = valueMap.get(memberId);
					if ( newValue == null ) {
						String msg = String.format("Missing parameter value for ParameterID=%s", memberId);
						throw new IOException(msg);
					}
					newValue.setIdShort("ParameterValue");
					SubmodelUtils.replaceFieldById((SubmodelElementCollection)paramValue, "ParameterValue",
													newValue);
				});
		super.write(paramValues);
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		Preconditions.checkNotNullArgument(smev, "smev is null");
		Preconditions.checkArgument(smev instanceof ElementCollectionValue,
									"smev should be a ElementCollectionValue: %s", smev);

		ElementCollectionValue smecv = (ElementCollectionValue)smev;
		List<ElementValue> paramValues
							= KeyValueFStream.from(smecv.getFieldMap())
											.map(kv -> {
												String paramId = kv.key();
												ElementValue paramValue = kv.value();
												
												LinkedHashMap<String,ElementValue> v = Maps.newLinkedHashMap();
												v.put("ParameterID", PropertyValue.STRING(paramId));
												v.put("ParameterValue", paramValue);
												return (ElementValue)new ElementCollectionValue(v);
											})
											.toList();
		ElementListValue expandedValue = new ElementListValue(paramValues);
		super.updateValue(expandedValue);
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		Preconditions.checkNotNullArgument(valueJsonString, "valueJsonString is null");
		
		SubmodelElement elm = read();
		ElementValues.updateWithValueJsonString(elm, valueJsonString);
		write(elm);
	}

	/**
	 * 파라미터 집합 참조는 첨부파일을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void readAttachment(OutputStream out) throws IOException {
		throw new UnsupportedOperationException("Attachment is not supported for MDTParameterCollectionReference");
	}

	/**
	 * 파라미터 집합 참조는 첨부파일을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void updateAttachment(FileValue file, InputStream content) throws IOException {
		throw new UnsupportedOperationException("Attachment is not supported for MDTParameterCollectionReference");
	}

	/**
	 * 파라미터 집합 참조는 첨부파일을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void removeAttachment() throws IOException {
		throw new UnsupportedOperationException("Attachment is not supported for MDTParameterCollectionReference");
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	/**
	 * 이 참조의 필드들을 주어진 {@link JsonGenerator}로 직렬화한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Serializer}가 {@link ElementReference} 객체를
	 * 직렬화하는 과정에서 호출된다.
	 *
	 * @param gen	직렬화에 사용할 {@link JsonGenerator}.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField(FIELD_INSTANCE_ID, getInstanceId());
		gen.writeStringField(FIELD_PARAMETER_EXPR, "*");
	}

	/**
	 * Json 객체로부터 {@link MDTParameterCollectionReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code instanceId}, {@code parameterExpr} 필드를 담은 Json 노드.
	 * @return 복원된 {@link MDTParameterCollectionReference} 객체.
	 * @throws IOException	Json 역직렬화 과정에서 예외가 발생한 경우.
	 * @throws IllegalArgumentException	{@code parameterExpr}이 {@code "*"}가 아닌 경우.
	 */
	public static MDTParameterCollectionReference deserializeFields(JsonNode jnode) throws IOException {
		String instanceId = checkJsonField(jnode, FIELD_INSTANCE_ID).asText();
		String parameterExpr = checkJsonField(jnode, FIELD_PARAMETER_EXPR).asText();

		Preconditions.checkArgument(parameterExpr.equals("*"),
									"invalid parameterExpr=%s for MDTParameterCollectionReference",
									parameterExpr);

		return new MDTParameterCollectionReference(instanceId);
	}

	@Override
	public String toStringExpr() {
		return String.format("param:%s:*", getInstanceId());
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || !(obj instanceof MDTParameterCollectionReference) ) {
			return false;
		}

		MDTParameterCollectionReference other = (MDTParameterCollectionReference) obj;
		return getInstanceId().equals(other.getInstanceId());
	}

	@Override
	public int hashCode() {
		return getInstanceId().hashCode();
	}
	
	/**
	 * 주어진 인스턴스의 파라미터 집합을 가리키는 {@link MDTParameterCollectionReference}를 생성한다.
	 *
	 * @param instanceId	대상 MDT 인스턴스의 식별자.
	 * @return 생성된 {@link MDTParameterCollectionReference} 객체.
	 */
	public static MDTParameterCollectionReference newInstance(String instanceId) {
		return new MDTParameterCollectionReference(instanceId);
	}
	
	/**
	 * 파라미터 집합이 위치한 idShort path를 생성한다.
	 * <p>
	 * 참조가 활성화되는 시점에 호출되며, 인스턴스의 에셋 종류에 따라
	 * {@code DataInfo.Equipment.EquipmentParameterValues} 또는
	 * {@code DataInfo.Operation.OperationParameterValues}를 반환한다.
	 *
	 * @return 파라미터 목록의 idShort path.
	 * @throws ModelValidationException 인스턴스의 에셋 종류가 설정되어 있지 않은 경우.
	 * @throws IllegalArgumentException 지원하지 않는 에셋 종류인 경우.
	 */
	@Override
	protected String buildIdShortPath() {
		MDTInstance instance = getInstance();
		MDTAssetType assetType = instance.getAssetType();
		if ( assetType == null ) {
			throw new ModelValidationException("AssetType is empty");
		}
	
		String assetTypeName = switch ( assetType ) {
			case Machine -> "Equipment";
            case Process -> "Operation";
            default -> throw new IllegalArgumentException("MDTParameter is not supported for assetType: "
            											+ assetType);
		};
		
		return String.format("DataInfo.%s.%sParameterValues", assetTypeName, assetTypeName);
	}
}
