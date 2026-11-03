package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.Preconditions;
import utils.json.JacksonUtils;
import utils.stream.FStream;


/**
 * AAS {@link ReferenceElement} SubmodelElement의 값({@link Reference})을 표현하는 클래스.
 * <p>
 * 값 객체({@link #toValueObject()})와 value-only JSON은 키 목록 {@code [{"type": ..., "value": ...}, ...]}
 * 형태이며 참조 타입({@link ReferenceTypes})은 포함하지 않는다. polymorphic JSON의 타입 식별자는
 * {@value #SERIALIZATION_TYPE}이며, 이 때의 {@code value}는 참조 타입을 포함한
 * {@code {"type": ..., "keys": [...]}} 형태로 직렬화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ReferenceElementValue extends AbstractElementValue implements DataElementValue {
	/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
	public static final String SERIALIZATION_TYPE = "mdt:value:reference";
	
	private final Reference m_reference;
	
	/**
	 * 주어진 {@link Reference}로 {@code ReferenceElementValue} 객체를 생성한다.
	 *
	 * @param reference	참조 객체.
	 * @throws IllegalArgumentException	{@code reference}가 {@code null}인 경우.
	 */
	public ReferenceElementValue(Reference reference) {
		Preconditions.checkNotNullArgument(reference, "reference is null");

		m_reference = reference;
	}
	
	/**
	 * AAS {@link ReferenceElement} 객체의 값으로 {@code ReferenceElementValue} 객체를 생성한다.
	 *
	 * @param refElm	값을 추출할 ReferenceElement 객체.
	 * @return	생성된 {@code ReferenceElementValue} 객체.
	 * @throws IllegalArgumentException	{@code refElm}의 값이 {@code null}인 경우.
	 */
	public static ReferenceElementValue from(ReferenceElement refElm) {
		return new ReferenceElementValue(refElm.getValue());
	}
	
	/**
	 * 참조 객체를 반환한다.
	 *
	 * @return	참조 객체.
	 */
	public Reference getReference() {
		return m_reference;
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	/**
	 * 참조의 키 목록을 {@code type}/{@code value} 키를 가진 {@link Map}의 리스트로 반환한다.
	 * 참조 타입은 포함되지 않는다.
	 *
	 * @return	값 객체.
	 */
	@Override
	public List<Map<String,String>> toValueObject() {
		return FStream.from(m_reference.getKeys())
						.map(key -> {
							Map<String,String> kvMap = Maps.newLinkedHashMap();
							kvMap.put("type", key.getType().name());
							kvMap.put("value", key.getValue());
							return kvMap;
						})
						.toList();
	}
	
	/**
	 * 주어진 {@link ReferenceElement}의 값을 이 객체의 참조로 설정한다.
	 *
	 * @param refElm	갱신할 ReferenceElement 객체.
	 */
	public void update(ReferenceElement refElm) {
		refElm.setValue(m_reference);
	}
	
	/**
	 * 값 객체({@code type}/{@code value} 키를 가진 {@link Map}의 {@link List})로부터
	 * {@code ReferenceElementValue}를 생성한다.
	 *
	 * @param value		키 목록에 해당하는 값 객체.
	 * @param refTypes	생성할 참조의 타입.
	 * @return	생성된 {@code ReferenceElementValue} 객체.
	 * @throws IOException	{@code value}가 {@link List}가 아닌 경우.
	 * @throws IllegalArgumentException	키 타입이 {@link KeyTypes}에 정의되지 않은 이름인 경우.
	 */
	public static ReferenceElementValue fromValueObject(Object value, ReferenceTypes refTypes) throws IOException {
		if ( value instanceof List keyObjsList ) {
			DefaultReference.Builder builder = new DefaultReference.Builder().type(refTypes);
			@SuppressWarnings("unchecked")
			List<Key> keys = FStream.<Map<?, ?>>from(keyObjsList)
									.map(keyObj -> {
										Map<?, ?> keyMap = (Map<?, ?>) keyObj;
										String keyType = (String) keyMap.get("type");
										String keyValue = (String) keyMap.get("value");
										return (Key) new DefaultKey.Builder()
																	.type(KeyTypes.valueOf(keyType))
																	.value(keyValue)
																	.build();
									})
									.toList();
			DefaultReference ref = builder.keys(keys).build();
			return new ReferenceElementValue(ref);
		}
		else {
			throw new IOException("invalid ReferenceElementValue object: " + value);
		}
	}
	
	/**
	 * value-only JSON 노드로부터 {@code ReferenceElementValue}를 생성한다.
	 * <p>
	 * 키 배열({@code [{"type": ..., "value": ...}, ...]}) 또는 {@code keys} 필드에 키 배열을 가진 JSON 객체를
	 * 받는다. 객체의 {@code type} 필드는 무시되고 참조 타입은 {@code refTypes}로 정해진다.
	 *
	 * @param vnode		JSON 노드.
	 * @param refTypes	생성할 참조의 타입.
	 * @return	생성된 {@code ReferenceElementValue} 객체.
	 * @throws IOException	{@code vnode}가 배열도 아니고 {@code keys} 필드를 가진 객체도 아니거나,
	 * 						키의 {@code type}/{@code value} 필드가 없는 경우.
	 */
	public static ReferenceElementValue parseValueJsonNode(JsonNode vnode, ReferenceTypes refTypes)
		throws IOException {
		JsonNode keysNode;
		if ( vnode.isArray() ) {
			keysNode = vnode;
		}
		else if ( vnode.isObject() && vnode.has("keys") ) {
			keysNode = vnode.get("keys");
		}
		else {
			throw new IOException("ReferenceElementValue expects a key 'Array' node: JsonNode=" + vnode);
		}

		Reference ref = new DefaultReference.Builder()
								.type(refTypes)
								.keys(parseKeysNode(keysNode))
								.build();
		return new ReferenceElementValue(ref);
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
			gen.writeStringField("type", m_reference.getType().name());
			gen.writeArrayFieldStart("keys");
				for ( Key key: m_reference.getKeys() ) {
					gen.writeStartObject();
					gen.writeStringField("type", key.getType().name());
					gen.writeStringField("value", key.getValue());
					gen.writeEndObject();
				}
			gen.writeEndArray();
		gen.writeEndObject();
	}
	
	/**
	 * polymorphic JSON의 {@code value} 하위 노드({@code {"type": ..., "keys": [...]}})로부터
	 * {@code ReferenceElementValue}를 생성한다.
	 * <p>
	 * {@link ElementValues}의 역직렬화 등록부에서 사용된다.
	 *
	 * @param vnode	JSON 객체 노드.
	 * @return	생성된 {@code ReferenceElementValue} 객체.
	 * @throws IOException	{@code type} 필드가 없거나, {@code keys} 필드가 배열이 아니거나,
	 * 						키의 {@code type}/{@code value} 필드가 없는 경우.
	 */
	public static ReferenceElementValue deserializeValue(JsonNode vnode) throws IOException {
		String type = JacksonUtils.getStringField(vnode, "type");
		JsonNode keysNode = JacksonUtils.getFieldOrNull(vnode, "keys");
		if ( keysNode == null || !keysNode.isArray() ) {
			throw new IOException("ReferenceElementValue expects a 'keys' Array field: JsonNode=" + vnode);
		}

		Reference ref = new DefaultReference.Builder()
								.type(ReferenceTypes.valueOf(type))
								.keys(parseKeysNode(keysNode))
								.build();
		return new ReferenceElementValue(ref);
	}

	private static List<Key> parseKeysNode(JsonNode keysNode) {
		return FStream.from(keysNode.elements())
						.mapOrThrow(keyNode -> {
							String keyType = JacksonUtils.getStringField(keyNode, "type");
							String keyValue = JacksonUtils.getStringField(keyNode, "value");
							return (Key)new DefaultKey.Builder()
														.type(KeyTypes.valueOf(keyType))
														.value(keyValue)
														.build();
						})
						.toList();
	}

	/**
	 * 감싼 aas4j {@link Reference}의 {@code equals}로 비교한다. {@code Reference}는 가변 객체이므로
	 * ({@code setKeys} 등) 그것을 바꾸면 이 값의 {@code equals}와 {@code hashCode}도 바뀐다.
	 * 해시 기반 컬렉션에 넣은 뒤에는 참조를 바꾸지 않는다.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || ReferenceElementValue.class != obj.getClass() ) {
			return false;
		}

		ReferenceElementValue other = (ReferenceElementValue)obj;
		return m_reference.equals(other.m_reference);
	}

	@Override
	public int hashCode() {
		return m_reference.hashCode();
	}
}
