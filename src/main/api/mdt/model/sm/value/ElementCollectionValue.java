package mdt.model.sm.value;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;


/**
 * {@link SubmodelElementCollection}의 값을 표현하는 {@link ElementValue}.
 * <p>
 * 컬렉션의 각 필드(idShort)를 {@link ElementValue}로, 삽입 순서를 유지하며 보관한다
 * ({@link LinkedHashMap} 기반). {@link SubmodelElementCollection}으로부터 생성하려면
 * {@link #from(SubmodelElementCollection)}을, 값 객체·값 JSON으로부터 생성하려면 필드 타입을 정하는 템플릿을 받는
 * {@link #fromValueObject(Object, ElementCollectionValue)},
 * {@link #parseValueJsonNode(JsonNode, ElementCollectionValue)}를 사용한다.
 * <p>
 * 값 객체({@link #toValueObject()})와 value-only JSON은 필드 idShort를 키로 하는 객체이다. polymorphic
 * JSON의 타입 식별자는 {@value #SERIALIZATION_TYPE}이며, 이 때 각 필드는 자신의 polymorphic JSON
 * ({@code @type}/{@code value})으로 직렬화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ElementCollectionValue extends AbstractElementValue {
	/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
	public static final String SERIALIZATION_TYPE = "mdt:value:collection";

	private final LinkedHashMap<String,? extends ElementValue> m_fields;

	/**
	 * 주어진 필드 맵으로 {@code ElementCollectionValue}를 생성한다.
	 * <p>
	 * 전달된 맵은 복사되어 삽입 순서가 보존된다.
	 *
	 * @param elements	필드 idShort에서 값으로의 맵({@code null} 불가).
	 */
	public ElementCollectionValue(LinkedHashMap<String,? extends ElementValue> elements) {
		Preconditions.checkNotNullArgument(elements, "elements must not be null");

		m_fields = Maps.newLinkedHashMap(elements);
	}
	
	/**
	 * 필드 수를 반환한다.
	 *
	 * @return	필드 수.
	 */
	public int size() {
		return m_fields.size();
	}

	/**
	 * 각 필드를 값 객체로 변환한 맵을 반환한다.
	 *
	 * @return 필드명에서 값 객체로의 맵.
	 */
	@Override
	public Map<String,Object> toValueObject() {
		return KeyValueFStream.from(m_fields)
								.mapValue(elm -> elm != null ? elm.toValueObject() : null)
								.toMap(new LinkedHashMap<>());
	}

	/**
	 * 필드 맵을 반환한다.
	 *
	 * @return 필드 idShort에서 {@link ElementValue}로의 맵(수정 불가 뷰).
	 */
	public Map<String,? extends ElementValue> getFieldMap() {
		return Collections.unmodifiableMap(m_fields);
	}

	/**
	 * 주어진 이름의 필드가 존재하는지 여부를 반환한다.
	 *
	 * @param fieldName	필드 idShort.
	 * @return 존재하면 {@code true}.
	 */
	public boolean containsField(String fieldName) {
		return m_fields.containsKey(fieldName);
	}

	/**
	 * 주어진 이름의 필드 값을 반환한다.
	 *
	 * @param fieldName	필드 idShort.
	 * @return 필드 값. 없으면 빈 {@link Optional}.
	 */
	public Optional<ElementValue> findField(String fieldName) {
		return Optional.ofNullable(m_fields.get(fieldName));
	}

	/**
	 * 주어진 이름의 필드 값을 반환한다.
	 *
	 * @param fieldName	필드 idShort.
	 * @return 필드 값.
	 * @throws IllegalArgumentException	해당 필드가 없는 경우.
	 */
	public ElementValue getField(String fieldName) {
		ElementValue field = m_fields.get(fieldName);
		if ( field != null ) {
			return field;
		}
		else {
			throw new IllegalArgumentException("No field found: " + fieldName);
		}
	}
	
	/**
	 * 이 컬렉션의 각 필드 값을 주어진 {@link SubmodelElementCollection}의 동일 idShort 원소에 반영한다.
	 * <p>
	 * idShort로 매칭되며, 양쪽에 모두 존재하는 필드만 갱신된다. 이 값에 없는 필드는 그대로 둔다(부분 갱신).
	 * <p>
	 * 갱신할 필드 값이 {@code null}인지는 반영 전에 모두 확인하므로 이때는 아무 필드도 바뀌지 않는다.
	 * 값을 지우려면 {@code null}이 아니라 값이 {@code null}인 {@link PropertyValue}를 넣는다
	 * (값 JSON에서는 필드를 JSON null로 준다). 필드 하나의 반영이 실패하면(타입 불일치 등) 앞선 필드들은
	 * 이미 바뀐 상태로 남는다(원자적이지 않음).
	 *
	 * @param smc	갱신할 SubmodelElementCollection.
	 * @throws IllegalArgumentException	갱신할 필드 값이 {@code null}이거나 필드 반영이 실패한 경우.
	 */
	public void update(SubmodelElementCollection smc) {
		Preconditions.checkNotNullArgument(smc, "SubmodelElementCollection is null");

		// smc에 없는 필드 값은 무시한다. 즉, smc에 정의되지 않은 필드는 갱신 대상이 아니다.
		var matches = FStream.from(smc.getValue())
								.tagKey(v -> v.getIdShort())
								.match(m_fields)
								.toList();
		for ( var match: matches ) {
			Preconditions.checkArgument(match.value()._2 != null, "field value is null: idShort=%s", match.key());
		}
		matches.forEach(match -> ElementValues.update(match.value()._1, match.value()._2));
	}
	
	/**
	 * 주어진 {@link SubmodelElementCollection}의 각 원소로부터 {@code ElementCollectionValue}를 생성한다.
	 *
	 * @param smc	원본 SubmodelElementCollection.
	 * @return 생성된 ElementCollectionValue.
	 */
	public static ElementCollectionValue from(SubmodelElementCollection smc) {
		var members = FStream.from(smc.getValue())
							.mapToKeyValue(member -> KeyValue.of(member.getIdShort(), ElementValues.getValue(member)))
							.toMap(new LinkedHashMap<>());
		return new ElementCollectionValue(members);
	}
	
	/**
	 * 값 객체(필드명에서 값으로의 {@link Map})와 {@link ElementCollectionValue} 템플릿으로부터
	 * {@code ElementCollectionValue}를 생성한다.
	 * <p>
	 * 템플릿의 각 필드에 대해 idShort로 입력 맵에서 값을 찾아 변환한다.
	 * 입력 맵에 없는 필드는 결과에 포함되지 않으며(부분 갱신), 템플릿에 없는 입력 맵의 항목도 무시된다.
	 * 값이 {@code null}인 필드는 그 필드 타입의 {@code null} 값으로 변환한다: Property는 값이 {@code null}인
	 * {@link PropertyValue}(값 지우기)가 되고, 그 밖의 타입은 {@code IOException}으로 거부된다.
	 * 각 필드의 타입은 템플릿 ElementCollectionValue로부터 결정된다.
	 *
	 * @param vobj	필드명에서 값으로의 Map.
	 * @param smev	각 필드의 타입 결정에 사용할 대상 ElementCollectionValue.
	 * @return 생성된 ElementCollectionValue.
	 * @throws IOException	{@code vobj}가 Map이 아니거나 변환이 실패한 경우.
	 */
	public static ElementCollectionValue fromValueObject(Object vobj, ElementCollectionValue smev)
			throws IOException {
			if ( vobj instanceof Map vmap ) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String,ElementValue> fields
						= KeyValueFStream.from(smev.m_fields)
										.match((Map<String,Object>)vmap)
										.mapToKeyValueOrThrow(kv -> {
											ElementValue field = kv.value()._1;
											Object value = kv.value()._2;
											return KeyValue.of(kv.key(), ElementValues.fromValueObject(value, field));
										})
										.toMap(new LinkedHashMap<>());
				return new ElementCollectionValue(fields);
			}
			else {
				throw new IOException("ElementCollectionValue value is not Map: obj=" + vobj);
			}
		}
	
	/**
	 * JSON 객체 노드와 템플릿 {@link ElementCollectionValue}으로부터 {@code ElementCollectionValue}를
	 * 생성한다.
	 * <p>
	 * 템플릿의 각 필드에 대해 idShort로 노드에서 필드를 찾아 변환한다. 노드에 없는 필드는 결과에 포함되지
	 * 않으며(부분 갱신), JSON null인 필드는 그 필드 타입의 null 값으로 변환한다: Property는 값이 {@code null}인
	 * {@link PropertyValue}(값 지우기)가 되고, 그 밖의 타입은 {@code IOException}으로 거부된다.
	 * 각 필드의 타입은 템플릿으로부터 결정된다.
	 *
	 * @param vnode	필드들을 담은 JSON 객체 노드.
	 * @param smcv	각 필드의 타입 결정에 사용할 대상 ElementCollectionValue.
	 * @return 생성된 ElementCollectionValue.
	 * @throws IOException	{@code vnode}가 객체가 아니거나 변환이 실패한 경우.
	 */
	public static ElementCollectionValue parseValueJsonNode(JsonNode vnode, ElementCollectionValue smcv)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("ElementCollectionValue expects an 'Object' node: JsonNode=" + vnode);
		}
		
		// JSON null 필드는 NullNode 그대로 넘긴다(getFieldOrNull은 없는 필드와 JSON null을 구분하지 않는다).
		var valueFields
			= KeyValueFStream.from(smcv.m_fields)
							.filterKey(vnode::has)
							.mapToKeyValueOrThrow(kv -> {
								ElementValue fieldVal = ElementValues.parseValueJsonNode(vnode.get(kv.key()), kv.value());
								return KeyValue.of(kv.key(), fieldVal);
							})
							.toMap(new LinkedHashMap<>());
		return new ElementCollectionValue(valueFields);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_fields);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || ElementCollectionValue.class != obj.getClass() ) {
			return false;
		}
		
		ElementCollectionValue other = (ElementCollectionValue) obj;
		return Objects.equals(m_fields, other.m_fields);
	}
	
	@Override
	public String toString() {
		return m_fields.toString();
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	/**
	 * 필드들을 JSON 객체로 직렬화한다.
	 *
	 * @param gen	직렬화에 사용할 JsonGenerator.
	 * @throws IOException	직렬화가 실패한 경우.
	 */
	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		KeyValueFStream.from(m_fields)
						.forEachOrThrow(kv -> gen.writeObjectField(kv.key(), kv.value()));
		gen.writeEndObject();
	}
	
	/**
	 * JSON 객체 노드로부터 {@code ElementCollectionValue}를 역직렬화한다.
	 * <p>
	 * 각 필드는 polymorphic 타입 정보를 포함한 형태로 파싱된다({@code ElementValues.parseJsonNode}).
	 *
	 * @param jnode	JSON 객체 노드.
	 * @return 역직렬화된 ElementCollectionValue.
	 * @throws IOException	{@code jnode}가 객체가 아니거나({@code null}·JSON null 포함) 필드 파싱이 실패한 경우.
	 */
	public static ElementCollectionValue deserializeValue(JsonNode jnode) throws IOException {
		if ( jnode == null || !jnode.isObject() ) {
			throw new JacksonDeserializationException(String.format("invalid %s value: object expected, but %s",
																	SERIALIZATION_TYPE,
																	ElementListValue.describeNode(jnode)));
		}
		var fields = FStream.from(jnode.properties())
							.mapToKeyValueOrThrow(ent -> {
								ElementValue smev = ElementValues.parseJsonNode(ent.getValue());
								return KeyValue.of(ent.getKey(), smev);
							})
							.toMap(new LinkedHashMap<>());
		return new ElementCollectionValue(fields);
	}
}
