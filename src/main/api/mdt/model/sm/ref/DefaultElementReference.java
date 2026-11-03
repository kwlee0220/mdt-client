package mdt.model.sm.ref;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

import mdt.model.expr.MDTExpressionParser;
import mdt.model.ResourceNotFoundException;
import mdt.model.sm.value.References;


/**
 * {@link MDTSubmodelReference}와 SubmodelElement의 idShort 경로로 대상을 지정하는
 * 기본 {@link ElementReference} 구현체이다.
 * <p>
 * Json 직렬화 시 {@link #SERIALIZATION_TYPE}({@code "mdt:ref:element"}) 타입으로 식별되며,
 * 소속 Submodel 참조와 요소 경로를 각각 {@code submodelReference}, {@code elementPath} 필드로
 * 기록한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DefaultElementReference extends SubmodelBasedElementReference {
	/** Json 직렬화 타입 식별자({@code "mdt:ref:element"}). */
	public static final String SERIALIZATION_TYPE = "mdt:ref:element";
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_ELEMENT_PATH = "elementPath";
	
	private final String m_elementPath;
	
	private DefaultElementReference(MDTSubmodelReference smRef, String path) {
		super(smRef);
		Preconditions.checkNotNullArgument(path, "path must not be null");

		m_elementPath = path;
	}
	
	/**
	 * 대상 SubmodelElement의 idShort path를 반환한다.
	 * <p>
	 * 이 구현은 생성 시점에 고정된 요소 경로를 그대로 반환한다.
	 *
	 * @return 대상 SubmodelElement의 idShort path.
	 */
	@Override
	protected String buildIdShortPath() {
		return m_elementPath;
	}

	/**
	 * 이 참조가 가리키는 SubmodelElement의 idShort path를 반환한다.
	 * <p>
	 * 상위 클래스의 {@link SubmodelBasedElementReference#getIdShortPathString()} 메소드와
	 * 달리 활성화와 관계없이 호출가능함.
	 *
	 * @return 대상 SubmodelElement의 idShort path.
	 */
	@Override
	public String getIdShortPathString() {
		return m_elementPath;
	}
	
	/**
	 * 현재 참조 경로 아래의 자식 SubmodelElement를 가리키는 새 참조를 생성한다.
	 * <p>
	 * 같은 Submodel 참조를 공유하며 요소 경로 뒤에 {@code "." + name}을 덧붙인다.
	 * 이 참조가 활성 상태이면 새 참조도 같은 {@link mdt.model.instance.MDTInstanceManager}로 활성화한다.
	 *
	 * @param name	자식 SubmodelElement의 idShort.
	 * @return 자식 요소를 가리키는 {@link DefaultElementReference} 객체.
	 */
	public DefaultElementReference child(String name) {
		DefaultElementReference ref = new DefaultElementReference(getSubmodelReference(),
																	m_elementPath + "." + name);
		if ( isActivated() ) {
			ref.activate(getInstance().getInstanceManager());
		}
		return ref;
	}

	@Override
	public String toStringExpr() {
		return getSubmodelReference().toStringExpr() + ":" + MDTExpressionParser.toIdShortPathExpr(m_elementPath);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSubmodelReference(), m_elementPath);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		DefaultElementReference other = (DefaultElementReference)obj;
		return Objects.equals(getSubmodelReference(), other.getSubmodelReference())
				&& Objects.equals(m_elementPath, other.m_elementPath);
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
		gen.writeObjectField(FIELD_SUBMODEL_REF, getSubmodelReference());
		gen.writeStringField(FIELD_ELEMENT_PATH, m_elementPath);
	}

	/**
	 * Json 객체로부터 {@link DefaultElementReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code submodelReference}, {@code elementPath} 필드를 담은 Json 노드.
	 * @return 복원된 {@link DefaultElementReference} 객체.
	 * @throws JacksonDeserializationException	필수 필드가 없거나 JSON {@code null}이거나, {@code elementPath}가
	 * 											JSON 문자열이 아닌 경우.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 * @throws IllegalArgumentException	{@code submodelReference}를 해석할 수 없는 경우.
	 */
	public static DefaultElementReference deserializeFields(JsonNode jnode) throws IOException {
		JsonNode smNode = JacksonUtils.getField(jnode, FIELD_SUBMODEL_REF);
		MDTSubmodelReference smRef = DefaultSubmodelReference.parseJson(smNode);

		String idShortPath = JacksonUtils.getTextField(jnode, FIELD_ELEMENT_PATH);
		return DefaultElementReference.newInstance(smRef, idShortPath);
	}
	
	/**
	 * instance id와 Submodel idShort, 요소 경로로 {@link DefaultElementReference}를 생성한다.
	 *
	 * @param instanceId	MDT instance 식별자.
	 * @param smIdShort		대상 Submodel의 idShort.
	 * @param elementPath	대상 SubmodelElement의 idShort 경로.
	 * @return 생성된 {@link DefaultElementReference} 객체.
	 * @throws IllegalArgumentException	인자 중 하나가 {@code null}이거나, {@code instanceId}/{@code smIdShort}가
	 * 									빈 문자열인 경우.
	 */
	public static DefaultElementReference newInstance(String instanceId, String smIdShort, String elementPath) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(instanceId, smIdShort);
		return new DefaultElementReference(smRef, elementPath);
	}
	
	/**
	 * Submodel 참조와 요소 경로로 {@link DefaultElementReference}를 생성한다.
	 *
	 * @param smRef			대상 Submodel에 대한 참조.
	 * @param elementPath	대상 SubmodelElement의 idShort 경로.
	 * @return 생성된 {@link DefaultElementReference} 객체.
	 * @throws IllegalArgumentException	{@code smRef}나 {@code elementPath}가 {@code null}인 경우.
	 */
	public static DefaultElementReference newInstance(MDTSubmodelReference smRef, String elementPath) {
		return new DefaultElementReference(smRef, elementPath);
	}

	/**
	 * AAS {@link Reference}로부터 {@link DefaultElementReference}를 생성한다.
	 * <p>
	 * {@code ref}는 첫 키가 Submodel 키이고 나머지 키가 SubmodelElement 키인 ModelReference여야 한다.
	 * 첫 키의 값은 Submodel 식별자로 쓰이고({@link DefaultSubmodelReference.ByIdSubmodelReference}), 나머지
	 * 키들은 idShort 경로로 변환된다(정수 값의 키는 인덱스). 실제 자원을 조회하지 않는다.
	 *
	 * @param ref	SubmodelElement를 가리키는 AAS {@link Reference}.
	 * @return 생성된 {@link DefaultElementReference} 객체.
	 * @throws ResourceNotFoundException	참조가 가리키는 자원을 찾을 수 없는 경우.
	 * @throws IllegalArgumentException	{@code ref}가 {@code null}이거나 위 형식을 따르지 않는 경우.
	 */
	public static DefaultElementReference newInstance(Reference ref) throws ResourceNotFoundException {
		return References.toSubmodelElementReference(ref);
	}
	
	/**
	 * Json 객체 노드로부터 {@link DefaultElementReference}를 파싱한다.
	 *
	 * @param topNode	{@code submodelReference}, {@code elementPath} 필드를 담은 Json 객체 노드.
	 * @return 파싱된 {@link DefaultElementReference} 객체.
	 * @throws IOException	Json 해석에 실패하거나 {@code elementPath} 필드가 없는 경우.
	 * @throws IllegalArgumentException	{@code submodelReference}를 해석할 수 없는 경우.
	 */
	public static DefaultElementReference parseJson(ObjectNode topNode) throws IOException {
		return deserializeFields(topNode);
	}
}
