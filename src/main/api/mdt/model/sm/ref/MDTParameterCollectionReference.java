package mdt.model.sm.ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import utils.KeyValue;
import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;
import utils.stream.FStream;

import mdt.model.expr.MDTExpressionParser;
import mdt.model.MDTModelSerDe;
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
	/**
	 * Json 직렬화 타입 식별자({@code "mdt:ref:param"}). {@link MDTParameterReference}와 공유하며,
	 * {@code parameterExpr} 필드가 {@code "*"}인 것으로 구분한다.
	 */
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
	
	/**
	 * 참조가 가리키는 파라미터 집합의 값을 읽어 {@code ParameterID -> 값} 형태로 반환한다.
	 * <p>
	 * 각 멤버의 {@code ParameterID} 값을 키로, {@code ParameterValue}의 값을 값으로 하는
	 * {@link ElementCollectionValue}를 파라미터 순서대로 만든다.
	 *
	 * @return 파라미터 값들을 담은 {@link ElementCollectionValue}.
	 * @throws IOException 읽기 과정에서 예외가 발생하거나, 값이 {@link ElementListValue}가 아니거나,
	 * 				멤버 구조가 올바르지 않은 경우.
	 */
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
	
	/**
	 * 주어진 파라미터 값 목록으로 파라미터 집합 전체를 갱신한다.
	 * <p>
	 * {@code sme}는 {@link #read()}가 반환하는 것과 같은 형태, 즉 멤버의 idShort가 {@code ParameterID}인
	 * {@link SubmodelElementList}여야 한다. 현재 파라미터 목록을 읽어 각 파라미터의 {@code ParameterValue}를
	 * 같은 ID 멤버의 복사본(idShort를 {@code "ParameterValue"}로 바꾼 것)으로 교체한 뒤 기록한다. 주어진
	 * {@code sme}와 그 멤버들은 변경되지 않는다.
	 *
	 * @param sme	{@code ParameterID}를 idShort로 갖는 파라미터 값들의 {@link SubmodelElementList}.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 {@link SubmodelElementList}가 아닌 경우.
	 * @throws IOException	현재 파라미터 중 {@code sme}에 값이 없는 것이 있거나, 읽기·쓰기 과정에서 예외가
	 * 						발생한 경우.
	 */
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
					// 호출자의 요소를 바꾸지 않도록 복사본의 idShort를 바꿔서 넣는다.
					SubmodelElement copied = MDTModelSerDe.readValue(MDTModelSerDe.toJsonString(newValue),
																	SubmodelElement.class);
					copied.setIdShort("ParameterValue");
					SubmodelUtils.replaceFieldById((SubmodelElementCollection)paramValue, "ParameterValue",
													copied);
				});
		super.write(paramValues);
	}

	/**
	 * 파라미터 값들을 ID별로 갱신한다.
	 * <p>
	 * 값은 {@link #readValue()}가 돌려주는 것과 같은 {@code ParameterID -> 값} 형태의
	 * {@link ElementCollectionValue}이다. 주어진 ID의 파라미터만 갱신하고 나머지는 그대로 둔다(부분 갱신).
	 * 모르는 ID가 있거나 값의 타입이 파라미터와 맞지 않으면 아무것도 기록하지 않는다.
	 *
	 * @param smev	{@code ParameterID -> 값} 형태의 {@link ElementCollectionValue}.
	 * @throws IllegalArgumentException	{@code smev}가 {@code null}이거나 {@link ElementCollectionValue}가 아니거나,
	 * 									모르는 ID나 {@code null} 값이 있거나, 값의 타입이 맞지 않는 경우.
	 * @throws IOException	읽기·쓰기 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void updateValue(ElementValue smev) throws IOException {
		Preconditions.checkNotNullArgument(smev, "smev is null");
		Preconditions.checkArgument(smev instanceof ElementCollectionValue,
									"smev should be a ElementCollectionValue: %s", smev.getClass().getSimpleName());

		Map<String,? extends ElementValue> newValues = ((ElementCollectionValue)smev).getFieldMap();
		SubmodelElementList params = readParameters();
		Map<String,SubmodelElement> members = toMemberMap(params, newValues.keySet());
		for ( Map.Entry<String,? extends ElementValue> ent: newValues.entrySet() ) {
			Preconditions.checkArgument(ent.getValue() != null, "parameter value is null: ParameterID=%s", ent.getKey());
			ElementValues.update(members.get(ent.getKey()), ent.getValue());
		}
		write(params);
	}

	/**
	 * 값 JSON 객체({@code {"<ParameterID>": <값 JSON>, …}})로 파라미터 값들을 ID별로 갱신한다.
	 * <p>
	 * {@link #readValue()}의 값 JSON과 같은 형태이다. 주어진 ID의 파라미터만 갱신한다(부분 갱신).
	 *
	 * @param valueJsonString	값 JSON 객체 문자열.
	 * @throws JacksonDeserializationException	JSON 객체가 아니거나 값이 파라미터 타입과 맞지 않는 경우.
	 * @throws IllegalArgumentException	모르는 ID가 있는 경우.
	 * @throws IOException	JSON 문법 오류이거나 읽기·쓰기 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void updateFromValueJson(String valueJsonString) throws IOException {
		Preconditions.checkNotNullArgument(valueJsonString, "valueJsonString is null");
		updateFromValueJson(MDTModelSerDe.readSingleJsonNode(valueJsonString));
	}

	/**
	 * 값 JSON 객체 노드로 파라미터 값들을 ID별로 갱신한다. {@link #updateFromValueJson(String)}와 같다.
	 *
	 * @param valueNode	값 JSON 객체 노드.
	 * @throws IllegalArgumentException	{@code valueNode}가 {@code null}이거나 모르는 ID가 있는 경우.
	 * @throws IOException	{@link #updateFromValueJson(String)}와 같다.
	 */
	@Override
	public void updateFromValueJson(JsonNode valueNode) throws IOException {
		Preconditions.checkNotNullArgument(valueNode, "valueNode is null");
		if ( !valueNode.isObject() ) {
			throw new JacksonDeserializationException(String.format("invalid parameter collection value: object expected, but %s",
																	valueNode.getNodeType().name().toLowerCase()));
		}

		SubmodelElementList params = readParameters();
		Map<String,SubmodelElement> members = toMemberMap(params, Sets.newLinkedHashSet(valueNode::fieldNames));
		for ( Map.Entry<String,JsonNode> ent: valueNode.properties() ) {
			ElementValues.updateFromValueJson(members.get(ent.getKey()), ent.getValue());
		}
		write(params);
	}

	// read() 결과(멤버 idShort = ParameterID)를 읽는다.
	private SubmodelElementList readParameters() throws IOException {
		// read()는 SubmodelElementList가 아니면 IOException을 던진다.
		return (SubmodelElementList)read();
	}

	// 갱신할 ID가 모두 있는지 먼저 확인하고, ID -> 파라미터 값 요소 맵을 만든다.
	private Map<String,SubmodelElement> toMemberMap(SubmodelElementList params, Set<String> ids) {
		Map<String,SubmodelElement> members = Maps.newLinkedHashMap();
		for ( SubmodelElement member: params.getValue() ) {
			members.put(member.getIdShort(), member);
		}
		for ( String id: ids ) {
			Preconditions.checkArgument(members.containsKey(id), "unknown ParameterID: %s", id);
		}
		return members;
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
		String instanceId = JacksonUtils.getTextField(jnode, FIELD_INSTANCE_ID);
		String parameterExpr = JacksonUtils.getTextField(jnode, FIELD_PARAMETER_EXPR);

		Preconditions.checkArgument(parameterExpr.equals("*"),
									"invalid parameterExpr=%s for MDTParameterCollectionReference",
									parameterExpr);

		return new MDTParameterCollectionReference(instanceId);
	}

	@Override
	public String toStringExpr() {
		return String.format("param:%s:*", MDTExpressionParser.toNameExpr(getInstanceId()));
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
		if ( obj == null || obj.getClass() != getClass() ) {
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
	 * @throws IllegalArgumentException	{@code instanceId}가 {@code null}이거나 빈 문자열인 경우.
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
