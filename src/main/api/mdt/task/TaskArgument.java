package mdt.task;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;
import utils.json.JsonNodeStdDeserializer;
import utils.json.MultiTypeJsonNodeDeserializer;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.LiteralExpr;
import mdt.model.expr.MDTElementReferenceExpr;
import mdt.model.expr.MDTExpression;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.expr.MDTParserException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerAware;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;


/**
 * {@link MDTTask}를 실행할 때 넘기는 인자.
 * <p>
 * 다음 두 종류만 있다(봉인 클래스).
 * <ul>
 * 	<li>{@link ReferenceArgument}: MDT 요소 참조({@link MDTElementReference})를 통해 값을 읽고 쓰는 인자.</li>
 * 	<li>{@link LiteralArgument}: 고정된 값({@link ElementValue})을 갖는 인자.</li>
 * </ul>
 * 값을 읽기 전에 {@link #activate(MDTInstanceManager)}를 부른다. 종류와 상관없이 부를 수 있으며
 * 리터럴 인자에서는 아무 일도 하지 않는다.
 * <p>
 * <b>동등성:</b> {@code equals}/{@code hashCode}는 감싼 참조·값의 {@code equals}/{@code hashCode}에 위임한다.
 * 인자 자체는 필드를 바꿀 수 없지만, 감싼 {@link ElementValue}는 가변일 수 있다(예:
 * {@code MultiLanguagePropertyValue.getLangTextAll()}의 원소, {@code ElementCollectionValue.getFieldMap()}).
 * 리터럴 인자는 값을 복사하지 않고 그대로 보관·반환하므로, 그 값을 바꾸면 인자의 {@code equals}와
 * {@code hashCode}도 바뀐다. 해시 기반 컬렉션에 넣은 인자의 값은 바꾸지 않는다. 참조 인자의 동등성은 참조
 * 클래스의 정의를 따른다(예: 시계열 참조는 범위·컬럼을 비교하지 않는다).
 * <p>
 * 워크플로우 모델의 {@link mdt.workflow.model.ArgumentSpec}과는 다른 타입이다. {@code ArgumentSpec}은 워크플로우
 * 기술자에 저장되는 인자 명세로, 다른 태스크의 출력을 가리키는 {@code TaskOutputArgument}도 있다.
 * <p>
 * JSON 형식은 {@code {"@type": "mdt:argument:reference", "reference": {…}}}와
 * {@code {"@type": "mdt:argument:literal", "value": {…}}}이다. 판별자는 설정 파일(mdt-instance의
 * {@code ProgramOperationDescriptor})에 저장되므로 바꾸지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = TaskArgument.Serializer.class)
@JsonDeserialize(using = TaskArgument.Deserializer.class)
public abstract sealed class TaskArgument implements MDTInstanceManagerAware
		permits TaskArgument.ReferenceArgument, TaskArgument.LiteralArgument {
	// 새 TaskArgument 구현체를 추가하면 반드시 여기에 등록해야 polymorphic JSON 역직렬화(parseJsonNode)가 가능하다.
	// 같은 타입 이름을 두 번 등록하면 클래스 초기화 시 IllegalArgumentException이 발생한다.
	private static final MultiTypeJsonNodeDeserializer<TaskArgument> DESERIALIZER
		= MultiTypeJsonNodeDeserializer.builder(TaskArgument.class)
			.register(ReferenceArgument.SERIALIZATION_TYPE, ReferenceArgument::deserializeFields)
			.register(LiteralArgument.SERIALIZATION_TYPE, LiteralArgument::deserializeFields)
			.build();
	// Serializer가 쓰는 타입 필드 이름. 역직렬화 쪽과 항상 같도록 등록부에서 가져온다.
	private static final String FIELD_TYPE = DESERIALIZER.getTypeField();

	/**
	 * 주어진 참조를 가리키는 인자를 만든다.
	 *
	 * @param ref	MDT 요소 참조.
	 * @return	참조 인자.
	 * @throws IllegalArgumentException	{@code ref}가 {@code null}인 경우.
	 */
	public static ReferenceArgument reference(MDTElementReference ref) {
		return new ReferenceArgument(ref);
	}
	/**
	 * 참조 표현식(예: {@code param:inst:Speed})을 파싱해 참조 인자를 만든다.
	 *
	 * @param refString	참조 표현식. 문법은 {@link ElementReferences#parseExpr(String)}를 따른다.
	 * @return	참조 인자.
	 * @throws IllegalArgumentException	{@code refString}이 {@code null}인 경우.
	 * @throws MDTParserException	표현식이 올바르지 않은 경우.
	 */
	public static ReferenceArgument reference(String refString) {
		return new ReferenceArgument(ElementReferences.parseExpr(refString));
	}
	/**
	 * 주어진 값을 갖는 리터럴 인자를 만든다. 아래의 {@code literal(Integer)} 등은 해당 타입의
	 * {@link PropertyValue}로 감싸는 편의 메소드다.
	 *
	 * @param value	인자 값.
	 * @return	리터럴 인자.
	 * @throws IllegalArgumentException	{@code value}가 {@code null}인 경우.
	 */
	public static LiteralArgument literal(ElementValue value) {
		return new LiteralArgument(value);
	}
	/**
	 * 정수 값을 {@link PropertyValue#INTEGER(Integer)}로 감싼 리터럴 인자를 만든다.
	 *
	 * @param value	인자 값.
	 * @return	리터럴 인자.
	 */
	public static LiteralArgument literal(Integer value) {
		return literal(PropertyValue.INTEGER(value));
	}
	/**
	 * 실수 값을 {@link PropertyValue#FLOAT(Float)}로 감싼 리터럴 인자를 만든다.
	 *
	 * @param value	인자 값.
	 * @return	리터럴 인자.
	 */
	public static LiteralArgument literal(Float value) {
		return literal(PropertyValue.FLOAT(value));
	}
	/**
	 * 실수 값을 {@link PropertyValue#DOUBLE(Double)}로 감싼 리터럴 인자를 만든다.
	 *
	 * @param value	인자 값.
	 * @return	리터럴 인자.
	 */
	public static LiteralArgument literal(Double value) {
		return literal(PropertyValue.DOUBLE(value));
	}
	/**
	 * 문자열 값을 {@link PropertyValue#STRING(String)}로 감싼 리터럴 인자를 만든다.
	 *
	 * @param value	인자 값.
	 * @return	리터럴 인자.
	 */
	public static LiteralArgument literal(String value) {
		return literal(PropertyValue.STRING(value));
	}
	
	/**
	 * 인자가 참조를 쓰는 경우 주어진 관리자로 활성화한다. {@link LiteralArgument}에서는 아무 일도 하지 않는다.
	 *
	 * @param manager	MDT 인스턴스 관리자.
	 */
	@Override
	public abstract void activate(MDTInstanceManager manager);

	/**
	 * 인자의 값을 읽는다. 참조 인자는 가리키는 요소의 현재 값을, 리터럴 인자는 자신의 값을 돌려준다.
	 *
	 * @return	인자 값.
	 * @throws IOException	값을 읽는 중 오류가 발생한 경우.
	 */
	public abstract ElementValue readValue() throws IOException;

	/**
	 * 본 인자 명세의 직렬화 타입 식별자를 반환한다.
	 *
	 * @return	직렬화 타입 식별자.
	 */
	public abstract String getSerializationType();

	/**
	 * 본 인자 명세의 필드들을 JSON으로 직렬화한다.
	 *
	 * @param gen	JSON 생성기.
	 * @throws IOException	직렬화 과정에서 오류가 발생한 경우.
	 */
	public abstract void serializeFields(JsonGenerator gen) throws IOException;

	/**
	 * 이 인자를 {@code @type} 필드를 포함한 JSON 노드로 바꾼다.
	 *
	 * @return	JSON 노드.
	 */
	public JsonNode toJsonNode() {
		return MDTModelSerDe.toJsonNode(this);
	}

	/**
	 * 이 인자를 {@code @type} 필드를 포함한 JSON 문자열로 바꾼다.
	 *
	 * @return	JSON 문자열.
	 */
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}

	/**
	 * {@code @type} 필드를 가진 JSON 노드로부터 해당 타입의 {@link TaskArgument}를 복원한다.
	 * <p>
	 * Jackson을 거치지 않고 등록부에 바로 넘기므로, 구현체가 던진 예외가 그대로 전달된다.
	 * 2026-11-01 이전에는 {@code MDTModelSerDe.readValue}(aas4j → Jackson)를 거쳐 일반 {@code IOException}이
	 * {@code IllegalArgumentException}으로 바뀌었고, {@code null}·JSON {@code null}이면 {@code null}을 돌려줬다.
	 * 같은 일을 하던 {@code parseTypedJsonNode}는 지웠다.
	 *
	 * @param jnode	{@code @type} 필드를 포함한 JSON 노드.
	 * @return 복원된 {@link TaskArgument} 객체.
	 * @throws IllegalArgumentException	{@code jnode}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 없거나(JSON {@code null} 노드 포함), 문자열이
	 * 										아니거나, 미등록 타입이거나, 필수 필드가 없는 경우.
	 * @throws IOException	구현체가 노드를 해석하지 못한 경우.
	 */
	public static TaskArgument parseJsonNode(JsonNode jnode) throws IOException {
		return DESERIALIZER.deserialize(jnode);
	}

	/**
	 * {@code @type} 필드를 가진 JSON 문자열로부터 해당 타입의 {@link TaskArgument}를 복원한다.
	 * <p>
	 * 문자열을 트리로 읽은 뒤 {@link #parseJsonNode(JsonNode)}에 넘기므로 예외와 {@code null} 처리가 같다.
	 * 문자열 전체가 JSON 값 하나여야 한다. 첫 값 뒤에 다른 입력이 있으면(예: {@code {...} garbage}) 거부한다.
	 * 2026-11-01 이전에는 {@code MDTModelSerDe.readValue}(aas4j → Jackson)를 거쳐 {@code "null"}이면 Java
	 * {@code null}을 돌려줬고, 오류 메시지에 JSON 전체가 들어갔다.
	 *
	 * @param jsonString	JSON 문자열.
	 * @return 복원된 {@link TaskArgument} 객체. {@code null}이 아니다.
	 * @throws IllegalArgumentException	{@code jsonString}이 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 없거나(JSON {@code null} 포함), 미등록 타입이거나,
	 * 										필수 필드가 없는 경우.
	 * @throws IOException	JSON 문법이 틀렸거나, 첫 JSON 값 뒤에 다른 입력이 있거나, 구현체가 노드를 해석하지 못한 경우.
	 */
	public static TaskArgument parseJsonString(String jsonString) throws IOException {
		Preconditions.checkNotNullArgument(jsonString, "JSON string is null");
		return parseJsonNode(MDTModelSerDe.readSingleJsonNode(jsonString));
	}

	/**
	 * MDT 표현식을 해석해 인자를 만든다.
	 * <ul>
	 *   <li>요소 참조 표현식(예: {@code param:inst:Speed}, {@code inst:Data:Info.x})이면 {@link ReferenceArgument}.</li>
	 *   <li>값 리터럴 표현식이면 그 값의 {@link LiteralArgument}. 따옴표 없는 단어·숫자·불리언도 값 리터럴이다
	 *       (예: {@code hello} → 문자열, {@code 42} → 정수, {@code true} → 불리언).</li>
	 * </ul>
	 * 그 밖의 표현식(서브모델 참조 {@code inst:Data}, 대입식, {@code null})이나 문법에 맞지 않는 표현식
	 * (빈 문자열, 따옴표 없는 날짜·기간 {@code 2024-01-01T00:00:00Z}·{@code PT5S}, 공백이 든 단어 등)은
	 * {@link MDTParserException}으로 거부한다. 이런 값을 문자열로 넘기려면 따옴표로 감싼다(예: {@code 'PT5S'}).
	 * <p>
	 * 2026-11-01 이전에는 이름이 {@code parseTaskArgument}였고(별칭 없이 바꿈), 파싱에 실패하거나 참조·리터럴이
	 * 아니면 <b>표현식 문자열 자체</b>를 값으로 갖는 문자열 리터럴로 조용히 대체했다(단, 후행 입력 오류는
	 * {@code IllegalArgumentException}으로 그대로 나갔다).
	 *
	 * @param argSpecExpr	인자 표현식.
	 * @return	인자.
	 * @throws IllegalArgumentException	{@code argSpecExpr}가 {@code null}인 경우.
	 * @throws MDTParserException	표현식이 문법에 맞지 않거나, 요소 참조·값 리터럴이 아닌 경우.
	 */
	public static TaskArgument parseExpr(String argSpecExpr) {
		Preconditions.checkNotNullArgument(argSpecExpr, "TaskArgument expression is null");

		MDTExpression expr;
		try {
			expr = MDTExpressionParser.parseExpr(argSpecExpr);
		}
		catch ( MDTParserException e ) {
			// 후행 입력(예: 'a.b', 'hello world')도 파서가 MDTParserException으로 보고한다.
			throw new MDTParserException("invalid TaskArgument expression: '" + argSpecExpr + "', cause="
											+ e.getMessage(), e);
		}

		if ( expr instanceof MDTElementReferenceExpr refExpr ) {
			return TaskArgument.reference(refExpr.evaluate());
		}
		else if ( expr instanceof LiteralExpr lit ) {
			return TaskArgument.literal(lit.evaluate());
		}
		else {
			throw new MDTParserException(String.format("TaskArgument expression must be an element reference "
														+ "or a value literal: '%s' (%s)",
														argSpecExpr, expr.getClass().getSimpleName()));
		}
	}
	
	/**
	 * MDT 요소 참조를 통해 값을 읽고 쓰는 인자. 읽거나 쓰기 전에 {@link #activate(MDTInstanceManager)}를
	 * 불러야 한다.
	 */
	public static final class ReferenceArgument extends TaskArgument {
		/** JSON 직렬화 시 {@code @type} 필드에 기록되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:argument:reference";
		private static final String FIELD_REFERENCE = "reference";

		private final MDTElementReference m_ref;

		/**
		 * 주어진 참조를 감싼 인자를 만든다.
		 *
		 * @param ref	MDT 요소 참조.
		 * @throws IllegalArgumentException	{@code ref}가 {@code null}인 경우.
		 */
		public ReferenceArgument(MDTElementReference ref) {
			Preconditions.checkNotNullArgument(ref, "ref must not be null");
			m_ref = ref;
		}
		
		/**
		 * 참조를 표현식 문자열로 돌려준다. 2026-11-01 이전 이름은 {@code getReferenceString}.
		 *
		 * @return	참조 표현식.
		 */
		public String getReferenceExpr() {
			return m_ref.toStringExpr();
		}
		
		/**
		 * 감싼 참조를 돌려준다. 2026-11-01 이전 이름은 {@code getElementReference}.
		 *
		 * @return	MDT 요소 참조.
		 */
		public MDTElementReference getReference() {
			return m_ref;
		}
		
		@Override
		public void activate(MDTInstanceManager manager) {
			m_ref.activate(manager);
		}

		/**
		 * 참조가 가리키는 {@link SubmodelElement} 전체를 읽는다.
		 * <p>
		 * 2026-11-01 이전에는 상위 타입 {@link TaskArgument}에 선언되어 있었고, {@link LiteralArgument}에서는
		 * {@code UnsupportedOperationException}을 던졌다.
		 *
		 * @return	읽은 요소.
		 * @throws IOException	요소를 읽는 중 오류가 발생한 경우.
		 */
		public SubmodelElement read() throws IOException {
			return m_ref.read();
		}
		
		@Override
		public ElementValue readValue() throws IOException {
			return m_ref.readValue();
		}
		
		/**
		 * 참조가 가리키는 요소의 값을 주어진 요소의 값으로 바꾼다({@link ElementReference#update(SubmodelElement)}).
		 *
		 * @param sme	새 값을 담은 요소.
		 * @throws IOException	쓰는 중 오류가 발생한 경우.
		 */
		public void update(SubmodelElement sme) throws IOException {
			m_ref.update(sme);
		}
		
		/**
		 * 참조가 가리키는 요소의 값을 바꾼다.
		 *
		 * @param value	새 값.
		 * @throws IOException	쓰는 중 오류가 발생한 경우.
		 */
		public void updateValue(ElementValue value) throws IOException {
			m_ref.updateValue(value);
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeObjectField(FIELD_REFERENCE, m_ref);
		}

		/**
		 * {@code reference} 필드로부터 참조 인자를 복원한다.
		 *
		 * @param jnode	{@code @type}과 {@code reference} 필드를 가진 JSON 노드.
		 * @return	참조 인자.
		 * @throws JacksonDeserializationException	{@code reference} 필드가 없거나, 그 참조가
		 * 										{@link MDTElementReference}가 아닌 경우.
		 * @throws IOException	참조를 해석하지 못한 경우.
		 */
		public static ReferenceArgument deserializeFields(JsonNode jnode) throws IOException {
			// 참조는 등록부로 바로 읽는다. 이전에는 MDTModelSerDe.readValue(aas4j → Jackson)를 거치면서
			// 필드 누락이 null이 되어 생성자의 IllegalArgumentException으로 나타났고, MDTElementReference가 아닌
			// 참조(예: FileStoreReference)는 강제 형변환에서 ClassCastException이 났다.
			JsonNode refNode = JacksonUtils.getField(jnode, FIELD_REFERENCE);
			ElementReference ref = ElementReferences.parseJsonNode(refNode);
			if ( ref instanceof MDTElementReference mdtRef ) {
				return new ReferenceArgument(mdtRef);
			}
			throw new JacksonDeserializationException(String.format(
									"invalid field '%s': MDTElementReference expected, but %s",
									FIELD_REFERENCE, ref.getClass().getSimpleName()));
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || ReferenceArgument.class != obj.getClass() ) {
				return false;
			}
			return m_ref.equals(((ReferenceArgument)obj).m_ref);
		}

		@Override
		public int hashCode() {
			return m_ref.hashCode();
		}

		@Override
		public String toString() {
			return String.format("reference(%s)", m_ref.toStringExpr());
		}
	}
	
	/**
	 * 고정된 값을 갖는 인자.
	 */
	public static final class LiteralArgument extends TaskArgument {
		/** JSON 직렬화 시 {@code @type} 필드에 기록되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:argument:literal";
		private static final String FIELD_VALUE = "value";

		private final ElementValue m_value;

		/**
		 * 주어진 값을 갖는 인자를 만든다. 값은 복사하지 않고 그대로 보관한다.
		 *
		 * @param value	인자 값.
		 * @throws IllegalArgumentException	{@code value}가 {@code null}인 경우.
		 */
		public LiteralArgument(ElementValue value) {
			Preconditions.checkNotNullArgument(value, "value must not be null");
			
			m_value = value;
		}
		
		/**
		 * 리터럴 인자는 참조를 쓰지 않으므로 아무 일도 하지 않는다.
		 */
		@Override
		public void activate(MDTInstanceManager manager) {
		}
		
		@Override
		public ElementValue readValue() {
			return m_value;
		}
		
		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeObjectField(FIELD_VALUE, m_value);
		}

		/**
		 * {@code value} 필드로부터 리터럴 인자를 복원한다.
		 *
		 * @param jnode	{@code @type}과 {@code value} 필드를 가진 JSON 노드.
		 * @return	리터럴 인자.
		 * @throws JacksonDeserializationException	{@code value} 필드가 없는 경우.
		 * @throws IOException	값을 해석하지 못한 경우.
		 */
		public static LiteralArgument deserializeFields(JsonNode jnode) throws IOException {
			// 필수 필드다. 이전에는 getFieldOrNull로 읽어, 누락 시 ElementValues.parseJsonNode(null)의
			// IllegalArgumentException("JsonNode is null")으로 나타났다.
			JsonNode valueNode = JacksonUtils.getField(jnode, FIELD_VALUE);
			ElementValue value = ElementValues.parseJsonNode(valueNode);
			return new LiteralArgument(value);
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || LiteralArgument.class != obj.getClass() ) {
				return false;
			}
			return m_value.equals(((LiteralArgument)obj).m_value);
		}

		@Override
		public int hashCode() {
			return m_value.hashCode();
		}

		@Override
		public String toString() {
			return String.format("literal(%s)", m_value.toValueJsonString());
		}
	}

	/**
	 * {@link TaskArgument} 객체를 {@code @type} 필드를 포함한 JSON으로 직렬화하는 Jackson serializer.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<TaskArgument> {
		private Serializer() {
			super(TaskArgument.class);
		}

		@Override
		public void serialize(TaskArgument arg, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_TYPE, arg.getSerializationType());
			arg.serializeFields(gen);
			gen.writeEndObject();
		}
	}

	/**
	 * {@code @type} 필드를 보고 해당하는 {@link TaskArgument} 구현체로 역직렬화하는 Jackson deserializer.
	 */
	@SuppressWarnings("serial")
	public static class Deserializer extends JsonNodeStdDeserializer<TaskArgument> {
		/**
		 * {@link TaskArgument} 등록부를 사용하는 deserializer를 만든다.
		 */
		public Deserializer() {
			super(TaskArgument.class, DESERIALIZER);
		}
	}
}
