package mdt.model.sm.value;

import java.io.IOException;
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
import utils.json.JacksonUtils;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;


/**
 * {@link SubmodelElementCollection}의 값을 표현하는 {@link ElementValue}.
 * <p>
 * 컬렉션의 각 필드(idShort)를 {@link ElementValue}로, 삽입 순서를 유지하며 보관한다
 * ({@link LinkedHashMap} 기반). {@link SubmodelElementCollection}이나 그 값으로부터 생성하려면
 * {@link #from(SubmodelElementCollection)}, {@link #fromValueObject(Object, SubmodelElementCollection)},
 * {@link #parseValueJsonNode(JsonNode, SubmodelElementCollection)}를 사용한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementCollectionValue extends AbstractElementValue implements ElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:collection";

	private final LinkedHashMap<String,? extends ElementValue> m_fields;

	/**
	 * 주어진 필드 맵으로 {@code ElementCollectionValue}를 생성한다.
	 * <p>
	 * 전달된 맵은 복사되어 삽입 순서가 보존된다.
	 *
	 * @param elements	필드 idShort에서 값으로의 맵({@code null} 불가).
	 */
	public ElementCollectionValue(Map<String,? extends ElementValue> elements) {
		Preconditions.checkNotNullArgument(elements, "elements must not be null");

		m_fields = Maps.newLinkedHashMap(elements);
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
								.toMap();
	}

	/**
	 * 필드 맵을 반환한다.
	 *
	 * @return 필드 idShort에서 {@link ElementValue}로의 맵.
	 */
	public Map<String,? extends ElementValue> getFieldMap() {
		return m_fields;
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
	 * idShort로 매칭되며, 양쪽에 모두 존재하는 필드만 갱신된다.
	 *
	 * @param smc	갱신할 SubmodelElementCollection.
	 */
	public void update(SubmodelElementCollection smc) {
		FStream.from(smc.getValue())
				.tagKey(v -> v.getIdShort())
				.match(m_fields)
				.forEach(match -> ElementValues.update(match.value()._1, match.value()._2));
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
							.toMap();
		return new ElementCollectionValue(members);
	}
	
	/**
	 * 값 객체(필드명에서 값으로의 {@link Map})와 대상 {@link SubmodelElementCollection}으로부터
	 * {@code ElementCollectionValue}를 생성한다.
	 * <p>
	 * SMC의 각 원소에 대해 idShort로 입력 맵에서 값을 찾아 변환하며, 입력에 없는 필드는 {@code null}로 채운다.
	 * 각 필드의 타입은 SMC로부터 결정된다.
	 *
	 * @param obj	필드명에서 값으로의 Map.
	 * @param smc	각 필드의 타입 결정에 사용할 대상 SubmodelElementCollection.
	 * @return 생성된 ElementCollectionValue.
	 * @throws IOException	{@code obj}가 Map이 아니거나 변환이 실패한 경우.
	 */
	public static ElementCollectionValue fromValueObject(Object obj, SubmodelElementCollection smc)
		throws IOException {
		if ( obj instanceof Map vmap ) {
			Map<String,ElementValue> smevMap
						= FStream.from(smc.getValue())
								.mapToKeyValueOrThrow(member -> {
									Object fieldValue = vmap.get(member.getIdShort());
									ElementValue elmVal = (fieldValue != null)
			                                                ? ElementValues.fromValueObject(fieldValue, member)
			                                                : null;
									return KeyValue.of(member.getIdShort(), elmVal);
								})
								.toMap();
			return new ElementCollectionValue(smevMap);
		}
		else {
			throw new IOException("ElementCollectionValue value is not Map: obj=" + obj);
		}
	}
	
	/**
	 * JSON 객체 노드와 대상 {@link SubmodelElementCollection}으로부터 {@code ElementCollectionValue}를 생성한다.
	 * <p>
	 * SMC의 각 원소에 대해 idShort로 노드에서 필드를 찾아 변환하며, 노드에 없는 필드는 {@code null}로 채운다.
	 * 각 필드의 타입은 SMC로부터 결정된다.
	 *
	 * @param vnode	필드들을 담은 JSON 객체 노드.
	 * @param smc	각 필드의 타입 결정에 사용할 대상 SubmodelElementCollection.
	 * @return 생성된 ElementCollectionValue.
	 * @throws IOException	{@code vnode}가 객체가 아니거나 변환이 실패한 경우.
	 */
	public static ElementCollectionValue parseValueJsonNode(JsonNode vnode, SubmodelElementCollection smc)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("ElementCollectionValue expects an 'Object' node: JsonNode=" + vnode);
		}
		
		var valueFields
			= FStream.from(smc.getValue())
					.mapToKeyValueOrThrow(member -> {
						JsonNode field = JacksonUtils.getFieldOrNull(vnode, member.getIdShort());
						ElementValue fieldVal = (field != null) ? ElementValues.parseValueJsonNode(field, member) : null;
						return KeyValue.of(member.getIdShort(), fieldVal);
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
	 */
	public static ElementCollectionValue deserializeValue(JsonNode jnode) {
		var fields = FStream.from(jnode.properties())
							.mapToKeyValue(ent -> KeyValue.of(ent.getKey(), ent.getValue()))
							.mapValue(ElementValues::parseJsonNode)
							.toMap();
		return new ElementCollectionValue(fields);
	}
}
