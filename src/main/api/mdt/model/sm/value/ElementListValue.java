package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.stream.FStream;


/**
 * {@link SubmodelElementList}의 값을 표현하는 {@link ElementValue}.
 * <p>
 * 리스트의 각 원소 값을 {@link ElementValue}로, 원소 순서를 그대로 유지하며 보관한다.
 * {@link SubmodelElementList}나 그 값으로부터 생성하려면 {@link #from(SubmodelElementList)},
 * {@link #fromValueObject(Object, SubmodelElementList)},
 * {@link #parseValueJsonNode(JsonNode, SubmodelElementList)}를 사용한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ElementListValue extends AbstractElementValue implements ElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:list";

	private final List<ElementValue> m_elementValues;

	/**
	 * 주어진 원소 값 리스트로 {@code ElementListValue}를 생성한다.
	 *
	 * @param values	원소 값 리스트({@code null} 불가).
	 */
	public ElementListValue(List<ElementValue> values) {
		Preconditions.checkNotNullArgument(values, "values is null");

		m_elementValues = values;
	}

	/**
	 * 리스트의 원소 값들을 반환한다.
	 *
	 * @return 원소 {@link ElementValue} 리스트.
	 */
	public List<ElementValue> getElementValues() {
		return m_elementValues;
	}

	/**
	 * 각 원소를 값 객체로 변환한 리스트를 반환한다.
	 *
	 * @return 원소 값 객체 리스트.
	 */
	@Override
	public List<Object> toValueObject() {
		return FStream.from(m_elementValues)
						.map(ElementValue::toValueObject)
						.toList();
	}
	
	/**
	 * 이 리스트의 각 원소 값을 주어진 {@link SubmodelElementList}의 대응 원소에 순서대로 반영한다.
	 *
	 * @param sml	갱신할 SubmodelElementList.
	 * @throws IllegalArgumentException	{@code sml}이 {@code null}이거나 원소 개수가 일치하지 않는 경우.
	 */
	public void update(SubmodelElementList sml) {
		Preconditions.checkNotNullArgument(sml, "SubmodelElementList is null");
		Preconditions.checkArgument(m_elementValues.size() == sml.getValue().size(),
									"Element count mismatch: elementValues.size=%d, "
									+ "submodelElementList.value.size=%d",
									m_elementValues.size(), sml.getValue().size());
		
		List<SubmodelElement> members = sml.getValue();
		FStream.zip(members, m_elementValues)
				.forEach(zip -> {
					SubmodelElement member = zip._1;
					ElementValue smev = zip._2;
					ElementValues.update(member, smev);
				});
	}
	
	/**
	 * 주어진 {@link SubmodelElementList}의 각 원소로부터 {@code ElementListValue}를 생성한다.
	 *
	 * @param sml	원본 SubmodelElementList.
	 * @return 생성된 ElementListValue.
	 */
	public static ElementListValue from(SubmodelElementList sml) {
		List<ElementValue> values = FStream.from(sml.getValue())
											.mapOrThrow(ElementValues::getValue)
											.toList();
		return new ElementListValue(values);
	}
	
	/**
	 * 값 객체(원소들의 {@link Iterable})와 템플릿 {@link ElementListValue}로부터
	 * {@code ElementListValue}를 생성한다.
	 * <p>
	 * 입력 원소들은 ElementListValue의 원소와 순서대로 짝지어지며,
	 * 각 원소의 타입은 SML로부터 결정된다.
	 *
	 * @param obj	원소 값들의 Iterable.
	 * @param sml	각 원소의 타입 결정에 사용할 대상 ElementListValue.
	 * @return 생성된 ElementListValue.
	 * @throws IOException	{@code obj}가 Iterable이 아니거나 변환이 실패한 경우.
	 */
	public static ElementListValue fromValueObject(Object vobj, ElementListValue sml) throws IOException {
		if ( vobj instanceof Iterable<?> iter ) {
			List<ElementValue> members
					= FStream.from(sml.m_elementValues)
							.zipWith(FStream.<Object>from(iter))
							.mapOrThrow(pair -> ElementValues.fromValueObject(pair._2, pair._1))
		                    .toList();
			return new ElementListValue(members);
		}
		else {
			throw new IOException("Invalid object for ElementListValue: " + vobj);
		}
	}
	
	/**
	 * JSON 배열 노드와 대상 {@link ElementListValue}로부터 {@code ElementListValue}를 생성한다.
	 * <p>
	 * 배열의 각 원소는 SML의 원소와 순서대로 짝지어지며, 각 원소의 타입은 SML로부터 결정된다.
	 *
	 * @param vnode	원소들을 담은 JSON 배열 노드.
	 * @param sml	각 원소의 타입 결정에 사용할 대상 ElementListValue.
	 * @return 생성된 ElementListValue.
	 * @throws IOException	{@code vnode}가 배열이 아니거나 변환이 실패한 경우.
	 */
	public static ElementListValue parseValueJsonNode(JsonNode vnode, ElementListValue sml)
		throws IOException {
		if ( !vnode.isArray() ) {
			throw new IOException("ElementListValue expects an 'Array' node: JsonNode=" + vnode);
		}
		List<ElementValue> values = FStream.from(sml.m_elementValues)
											.zipWith(FStream.from(vnode.elements()))
											.mapOrThrow(pair -> ElementValues.parseValueJsonNode(pair._2, pair._1))
											.toList();
		return new ElementListValue(values);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_elementValues);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || ElementListValue.class != obj.getClass() ) {
			return false;
		}
		
		ElementListValue other = (ElementListValue) obj;
		return Objects.equals(m_elementValues, other.m_elementValues);
	}
	
	@Override
	public String toString() {
		return m_elementValues.toString();
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	/**
	 * 원소 값들을 JSON 배열로 직렬화한다.
	 *
	 * @param gen	직렬화에 사용할 JsonGenerator.
	 * @throws IOException	직렬화가 실패한 경우.
	 */
	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		for ( ElementValue smev: m_elementValues ) {
			gen.writeObject(smev);
		}
		gen.writeEndArray();
	}
	
	/**
	 * JSON 배열 노드로부터 {@code ElementListValue}를 역직렬화한다.
	 * <p>
	 * 각 원소는 polymorphic 타입 정보를 포함한 형태로 파싱된다({@code ElementValues.parseJsonNode}).
	 *
	 * @param vnode	JSON 배열 노드.
	 * @return 역직렬화된 ElementListValue.
	 */
	public static ElementListValue deserializeValue(JsonNode vnode) {
		List<ElementValue> elements = FStream.from(vnode.elements())
											.mapOrThrow(elmNode -> ElementValues.parseJsonNode(elmNode))
											.toList();
		return new ElementListValue(elements);
	}
}
