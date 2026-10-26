package mdt.task;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

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
import mdt.workflow.model.ArgumentSpec.TaskOutputArgument;


/**
 * 워크플로우 태스크의 인자(argument)를 기술하는 명세.
 * <p>
 * 다음 세 가지 종류의 인자 명세를 지원한다:
 * <ul>
 * 	<li>{@link ReferenceArgument}: MDT 요소 참조({@link MDTElementReference})를 통해 값을 읽고 쓰는 인자.</li>
 * 	<li>{@link TaskOutputArgument}: 다른 태스크의 출력 값을 참조하는 인자.
 * 		직접 값을 읽을 수 없고, 해당 태스크 수행 결과로부터 값이 결정된다.</li>
 * 	<li>{@link LiteralArgument}: 고정된 리터럴 값({@link ElementValue})을 갖는 인자.</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = TaskArgument.Serializer.class)
@JsonDeserialize(using = TaskArgument.Deserializer.class)
public abstract sealed class TaskArgument
		permits TaskArgument.ReferenceArgument, TaskArgument.LiteralArgument {
	private static final String FIELD_TYPE = "@type";
	public static ReferenceArgument reference(MDTElementReference ref) {
		return new ReferenceArgument(ref);
	}
	public static ReferenceArgument reference(String refString) {
		return new ReferenceArgument(ElementReferences.parseExpr(refString));
	}
	public static LiteralArgument literal(ElementValue value) {
		return new LiteralArgument(value);
	}
	public static LiteralArgument literal(Integer value) {
		return literal(PropertyValue.INTEGER(value));
	}
	public static LiteralArgument literal(Float value) {
		return literal(PropertyValue.FLOAT(value));
	}
	public static LiteralArgument literal(Double value) {
		return literal(PropertyValue.DOUBLE(value));
	}
	public static LiteralArgument literal(String value) {
		return literal(PropertyValue.STRING(value));
	}
	
	public abstract SubmodelElement read() throws IOException;
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

	public JsonNode toJsonNode() {
		return MDTModelSerDe.toJsonNode(this);
	}

	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}

	public static TaskArgument parseJsonNode(JsonNode jnode) throws IOException {
		return MDTModelSerDe.readValue(jnode, TaskArgument.class);
	}

	public static TaskArgument parseJsonString(String jsonString) throws IOException {
		return MDTModelSerDe.readValue(jsonString, TaskArgument.class);
	}

	public static TaskArgument parseTaskArgument(String argSpecExpr) {
		try {
			MDTExpression expr = MDTExpressionParser.parseExpr(argSpecExpr);
			if ( expr instanceof MDTElementReferenceExpr refExpr ) {
				return TaskArgument.reference(refExpr.evaluate());	
			}
			else if ( expr instanceof LiteralExpr lit ) {
				return TaskArgument.literal(lit.evaluate());
			}
			else {
				return TaskArgument.literal(PropertyValue.STRING(argSpecExpr));
//			throw new MDTParserException("Unexpected variable expression: expr=" + argSpecExpr);
			}
		}
		catch ( MDTParserException e ) {
			return TaskArgument.literal(PropertyValue.STRING(argSpecExpr));
		}
	}
	
	public static final class ReferenceArgument extends TaskArgument implements MDTInstanceManagerAware {
		public static final String SERIALIZATION_TYPE = "mdt:argument:reference";
		private static final String FIELD_REFERENCE = "reference";

		private final MDTElementReference m_ref;

		public ReferenceArgument(MDTElementReference ref) {
			Preconditions.checkNotNullArgument(ref, "ref must not be null");
			m_ref = ref;
		}
		
		public String getReferenceString() {
			return m_ref.toStringExpr();
		}
		
		public MDTElementReference getElementReference() {
			return m_ref;
		}
		
		@Override
		public void activate(MDTInstanceManager manager) {
			m_ref.activate(manager);
		}

		@Override
		public SubmodelElement read() throws IOException {
			return m_ref.read();
		}
		
		@Override
		public ElementValue readValue() throws IOException {
			return m_ref.readValue();
		}
		
		public void update(SubmodelElement sme) throws IOException {
			m_ref.update(sme);
		}
		
		public void updateValue(ElementValue value) throws IOException {
			m_ref.updateValue(value);
		}
		
		public void updateWithJsonString(String jsonString) throws IOException {
			m_ref.updateValue(jsonString);
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeObjectField(FIELD_REFERENCE, m_ref);
		}

		public static ReferenceArgument deserializeFields(JsonNode jnode) throws IOException {
			MDTElementReference ref = (MDTElementReference)MDTModelSerDe.readValue(jnode.get(FIELD_REFERENCE),
																					ElementReference.class);
			return new ReferenceArgument(ref);
		}

		@Override
		public String toString() {
			return String.format("reference(%s)", m_ref.toStringExpr());
		}
	}
	
	public static final class LiteralArgument extends TaskArgument {
		public static final String SERIALIZATION_TYPE = "mdt:argument:literal";
		private static final String FIELD_VALUE = "value";

		private final ElementValue m_value;

		public LiteralArgument(ElementValue value) {
			Preconditions.checkNotNullArgument(value, "value must not be null");
			
			m_value = value;
		}
		
		@Override
		public SubmodelElement read() {
			throw new UnsupportedOperationException(
					"Cannot get SubmodelElement of LiteralArgumentSpec: value=" + m_value);
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

		public static LiteralArgument deserializeFields(JsonNode jnode) throws IOException {
			JsonNode valueNode = JacksonUtils.getFieldOrNull(jnode, FIELD_VALUE);
			ElementValue value = ElementValues.parseJsonNode(valueNode);
			return new LiteralArgument(value);
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
			this(null);
		}
		private Serializer(Class<TaskArgument> cls) {
			super(cls);
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
	public static class Deserializer extends StdDeserializer<TaskArgument> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public TaskArgument deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			return parseTypedJsonNode(jnode);
		}
	}

	/**
	 * {@code @type} 필드를 가진 Json 노드로부터 해당 타입의 {@link TaskArgument}를 복원한다.
	 *
	 * @param jnode	{@code @type} 필드를 포함한 Json 노드.
	 * @return 복원된 {@link TaskArgument} 객체.
	 * @throws IOException	{@code @type}이 없거나 미등록 타입이거나, 해석 과정에서 예외가 발생한 경우.
	 */
	public static TaskArgument parseTypedJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new JacksonDeserializationException(String.format("'%s' field is missing: json=%s",
																	FIELD_TYPE, jnode));
		}

		switch ( type ) {
			case ReferenceArgument.SERIALIZATION_TYPE:
				return ReferenceArgument.deserializeFields(jnode);
			case LiteralArgument.SERIALIZATION_TYPE:
				return LiteralArgument.deserializeFields(jnode);
			default:
				throw new JacksonDeserializationException("Unregistered TaskArgument type: " + type);
		}
	}

	public static ElementValue readElementValue(TaskArgument arg, MDTInstanceManager manager) throws IOException {
		if ( arg instanceof TaskArgument.ReferenceArgument refArg ) {
			var ref = ElementReferences.parseExpr(refArg.getReferenceString());
			ref.activate(manager);
			return ref.readValue();
		}
		else if ( arg instanceof TaskArgument.LiteralArgument literal ) {
			return literal.readValue();
		}
		else {
			String msg = String.format("Unsupported readElementValue() for TaskArgument: %s", arg);
			throw new IllegalArgumentException(msg);
		}
	}

	public static ElementValue readElement(TaskArgument arg, MDTInstanceManager manager) throws IOException {
		if ( arg instanceof TaskArgument.ReferenceArgument refArg ) {
			var ref = ElementReferences.parseExpr(refArg.getReferenceString());
			ref.activate(manager);
			return ref.readValue();
		}
		else {
			String msg = String.format("Unsupported readElement() for TaskArgument: %s", arg);
			throw new IllegalArgumentException(msg);
		}
	}
}
