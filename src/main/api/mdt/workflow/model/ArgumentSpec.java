package mdt.workflow.model;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;


/**
 * 워크플로우 태스크의 인자(argument)를 기술하는 명세.
 * <p>
 * 다음 세 가지 종류의 인자 명세를 지원한다:
 * <ul>
 * 	<li>{@link ReferenceArgument}: MDT 요소 참조({@link MDTElementReference})로 값의 위치를 지정하는 인자.</li>
 * 	<li>{@link TaskOutputArgument}: 다른 태스크의 출력 값을 참조하는 인자.
 * 		값은 해당 태스크의 수행 결과로부터 결정된다.</li>
 * 	<li>{@link LiteralArgument}: 고정된 리터럴 값({@link ElementValue})을 갖는 인자.</li>
 * </ul>
 * 본 클래스는 인자를 <em>기술</em>하기만 하며, 인자 값을 실제로 읽고 쓰는 것은 명세를 해석하는 태스크의 몫이다.
 * <p>
 * 모든 인자 명세는 종류와 무관하게 필수 식별자({@link #getId()})와, 선택적인 이름({@link #getName()}) 및
 * 설명({@link #getDescription()})을 갖는다. 이름과 설명은 지정된 경우에만 JSON으로 직렬화된다.
 * <p>
 * 인자 명세 객체는 각 하위 클래스의 {@code builder()}를 통해 생성한다. 식별자/이름/설명은
 * {@link AbstractBuilder}가 공통으로 제공한다:
 * <pre>{@code
 * ArgumentSpec arg = ArgumentSpec.LiteralArgument.builder()
 *                                  .id("sleepTime")
 *                                  .name("SleepTime")
 *                                  .description("대기 시간(초)")
 *                                  .value(5)
 *                                  .build();
 * }</pre>
 * 이름과 설명이 필요 없는 경우는 {@link #literal(String, ElementValue)}, {@link #reference(String, String)},
 * {@link #taskOutput(String, String, String)} 등의 단축 factory 메소드를 사용할 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ArgumentSpec.Serializer.class)
@JsonDeserialize(using = ArgumentSpec.Deserializer.class)
public abstract sealed class ArgumentSpec
		permits ArgumentSpec.ReferenceArgument, ArgumentSpec.TaskOutputArgument,
				ArgumentSpec.LiteralArgument {
	private static final String FIELD_TYPE = "@type";
	private static final String FIELD_ID = "id";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "description";

	@NotNull private final String m_id;
	@Nullable private final String m_name;
	@Nullable private final String m_description;

	/**
	 * Builder에 설정된 공통 속성으로 인자 명세를 초기화한다.
	 *
	 * @param builder	공통 속성이 설정된 builder 객체.
	 * @throws IllegalArgumentException	builder에 인자 식별자가 설정되지 않은 경우.
	 */
	protected ArgumentSpec(AbstractBuilder<?,?> builder) {
		Preconditions.checkNotNullArgument(builder.m_id, "argument id must not be null");

		m_id = builder.m_id;
		m_name = builder.m_name;
		m_description = builder.m_description;
	}

	/**
	 * 인자의 식별자를 반환한다.
	 *
	 * @return	인자의 식별자.
	 */
	@NotNull public String getId() {
		return m_id;
	}

	/**
	 * 인자의 이름을 반환한다.
	 *
	 * @return	인자의 이름. 이름이 지정되지 않은 경우는 {@code null}.
	 */
	@Nullable public String getName() {
		return m_name;
	}

	/**
	 * 인자에 대한 설명을 반환한다.
	 *
	 * @return	인자에 대한 설명. 설명이 지정되지 않은 경우는 {@code null}.
	 */
	@Nullable public String getDescription() {
		return m_description;
	}
	
	public ElementValue readValue(MDTInstanceManager manager) throws IOException {
		return switch ( this ) {
			case ReferenceArgument refArg -> refArg.getReference(manager).readValue();
			case LiteralArgument litArg -> litArg.getValue();
			case TaskOutputArgument taskOutArg
				-> throw new IllegalStateException("TaskOutputArgument value is not readable: " + taskOutArg);
		};
	}

	/**
	 * 모든 {@link ArgumentSpec} builder의 공통 속성(식별자, 이름, 설명)을 제공하는 기반 builder.
	 *
	 * @param <B>	구체 builder 타입. (fluent 메소드 chaining을 위한 self type)
	 * @param <T>	builder가 생성하는 {@link ArgumentSpec} 타입.
	 */
	public abstract static class AbstractBuilder<B extends AbstractBuilder<B,T>, T extends ArgumentSpec> {
		@NotNull protected String m_id;
		@Nullable protected String m_name;
		@Nullable protected String m_description;

		/**
		 * 인자의 식별자를 설정한다.
		 *
		 * @param id	인자의 식별자. ({@code null} 불가)
		 * @return	본 builder 객체.
		 */
		public B id(@NotNull String id) {
			m_id = id;
			return self();
		}

		/**
		 * 설정된 속성으로 인자 명세 객체를 생성한다.
		 *
		 * @return	생성된 인자 명세 객체.
		 */
		public abstract T build();

		@SuppressWarnings("unchecked")
		protected B self() {
			return (B)this;
		}

		/**
		 * 인자의 이름을 설정한다.
		 *
		 * @param name	인자의 이름. ({@code null} 가능)
		 * @return	본 builder 객체.
		 */
		public B name(@Nullable String name) {
			m_name = name;
			return self();
		}

		/**
		 * 인자에 대한 설명을 설정한다.
		 *
		 * @param description	인자에 대한 설명. ({@code null} 가능)
		 * @return	본 builder 객체.
		 */
		public B description(@Nullable String description) {
			m_description = description;
			return self();
		}
	}

	/**
	 * Json 노드에 포함된 공통 필드(식별자, 이름, 설명)를 builder에 채운다.
	 *
	 * @param jnode		공통 필드를 포함한 Json 노드.
	 * @param builder	채울 대상 builder 객체.
	 * @throws IOException	필수 필드인 식별자가 존재하지 않는 경우.
	 */
	protected static void deserializeCommonFields(JsonNode jnode, AbstractBuilder<?,?> builder)
		throws IOException {
		builder.id(JacksonUtils.getStringField(jnode, FIELD_ID));
		builder.name(JacksonUtils.getStringFieldOrNull(jnode, FIELD_NAME));
		builder.description(JacksonUtils.getStringFieldOrNull(jnode, FIELD_DESCRIPTION));
	}

	// 이름과 설명이 없는 인자 명세를 간결하게 생성하기 위한 단축 factory 메소드들.
	// 식별자는 필수이기 때문에 항상 첫 인자로 받는다.
	// 이름이나 설명을 함께 지정하려면 각 하위 클래스의 builder()를 사용한다.
	public static ReferenceArgument reference(String id, String refString) {
		return ReferenceArgument.builder().id(id).reference(refString).build();
	}
	public static TaskOutputArgument taskOutput(String id, String taskId, String argName) {
		return TaskOutputArgument.builder().id(id).taskId(taskId).argumentName(argName).build();
	}
	public static LiteralArgument literal(String id, ElementValue value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	public static LiteralArgument literal(String id, Integer value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	public static LiteralArgument literal(String id, Float value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	public static LiteralArgument literal(String id, Double value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	public static LiteralArgument literal(String id, String value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}

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

	public static ArgumentSpec parseJsonNode(JsonNode jnode) throws IOException {
		return MDTModelSerDe.readValue(jnode, ArgumentSpec.class);
	}

	public static ArgumentSpec parseJsonString(String jsonString) throws IOException {
		return MDTModelSerDe.readValue(jsonString, ArgumentSpec.class);
	}
	
	public static final class ReferenceArgument extends ArgumentSpec {
		public static final String SERIALIZATION_TYPE = "mdt:task_arg:reference";
		private static final String FIELD_REFERENCE = "reference";

		private final String m_refString;

		private ReferenceArgument(Builder builder) {
			super(builder);
			Preconditions.checkNotNullArgument(builder.m_refString, "ref must not be null");
			m_refString = builder.m_refString;
		}

		public String getReferenceString() {
			return m_refString;
		}
		
		public MDTElementReference getReference(MDTInstanceManager manager) {
			MDTElementReference ref = ElementReferences.parseExpr(m_refString);
			ref.activate(manager);
			return ref;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		/**
		 * {@link ReferenceArgument} 생성을 위한 builder를 반환한다.
		 *
		 * @return	builder 객체.
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * {@link ReferenceArgument} 생성을 위한 builder.
		 */
		public static final class Builder extends AbstractBuilder<Builder,ReferenceArgument> {
			private String m_refString;

			@Override
			public ReferenceArgument build() {
				return new ReferenceArgument(this);
			}

			/**
			 * 인자의 값을 읽고 쓸 MDT 요소 참조를 설정한다.
			 *
			 * @param refString	MDT 요소 참조. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder reference(String refString) {
				m_refString = refString;
				return this;
			}
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeObjectField(FIELD_REFERENCE, m_refString);
		}

		public static ReferenceArgument deserializeFields(JsonNode jnode) throws IOException {
			Builder builder = builder();
			deserializeCommonFields(jnode, builder);

			String refString = JacksonUtils.getStringField(jnode, FIELD_REFERENCE);
			return builder.reference(refString)
							.build();
		}

		@Override
		public String toString() {
			return String.format("reference(%s)", m_refString);
		}
	}
	
	public static final class TaskOutputArgument extends ArgumentSpec {
		public static final String SERIALIZATION_TYPE = "mdt:task_arg:task-output";
		private static final String FIELD_TASK_ID = "taskId";
		private static final String FIELD_ARGUMENT_NAME = "argumentName";

		private final String m_taskId;
		private final String m_argName;

		private TaskOutputArgument(Builder builder) {
			super(builder);
			Preconditions.checkNotNullArgument(builder.m_taskId, "taskId must not be null");
			Preconditions.checkNotNullArgument(builder.m_argName, "argName must not be null");

			m_taskId = builder.m_taskId;
			m_argName = builder.m_argName;
		}

		/**
		 * {@link TaskOutputArgument} 생성을 위한 builder를 반환한다.
		 *
		 * @return	builder 객체.
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * {@link TaskOutputArgument} 생성을 위한 builder.
		 */
		public static final class Builder extends AbstractBuilder<Builder,TaskOutputArgument> {
			private String m_taskId;
			private String m_argName;

			@Override
			public TaskOutputArgument build() {
				return new TaskOutputArgument(this);
			}

			/**
			 * 출력 값을 가져올 태스크의 식별자를 설정한다.
			 *
			 * @param taskId	태스크 식별자. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder taskId(String taskId) {
				m_taskId = taskId;
				return this;
			}

			/**
			 * 가져올 출력 인자의 이름을 설정한다.
			 *
			 * @param argName	출력 인자의 이름. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder argumentName(String argName) {
				m_argName = argName;
				return this;
			}
		}

		public String getTaskId() {
			return m_taskId;
		}

		public String getArgumentName() {
			return m_argName;
		}
		
		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeStringField(FIELD_TASK_ID, m_taskId);
			gen.writeStringField(FIELD_ARGUMENT_NAME, m_argName);
		}

		public static TaskOutputArgument deserializeFields(JsonNode jnode) throws IOException {
			Builder builder = builder();
			deserializeCommonFields(jnode, builder);

			return builder.taskId(JacksonUtils.getStringField(jnode, FIELD_TASK_ID))
							.argumentName(JacksonUtils.getStringField(jnode, FIELD_ARGUMENT_NAME))
							.build();
		}

		@Override
		public String toString() {
			return String.format("task_output(%s:%s)", m_taskId, m_argName);
		}
	}
	
	public static final class LiteralArgument extends ArgumentSpec {
		public static final String SERIALIZATION_TYPE = "mdt:task_arg:literal";
		private static final String FIELD_VALUE = "value";

		private final ElementValue m_value;

		private LiteralArgument(Builder builder) {
			super(builder);
			Preconditions.checkNotNullArgument(builder.m_value, "value must not be null");

			m_value = builder.m_value;
		}

		/**
		 * {@link LiteralArgument} 생성을 위한 builder를 반환한다.
		 *
		 * @return	builder 객체.
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * {@link LiteralArgument} 생성을 위한 builder.
		 */
		public static final class Builder extends AbstractBuilder<Builder,LiteralArgument> {
			private ElementValue m_value;

			@Override
			public LiteralArgument build() {
				return new LiteralArgument(this);
			}

			/**
			 * 인자의 리터럴 값을 설정한다.
			 *
			 * @param value	리터럴 값. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder value(ElementValue value) {
				m_value = value;
				return this;
			}

			/**
			 * 인자의 리터럴 값을 {@code int} 값으로 설정한다.
			 *
			 * @param value	리터럴 값.
			 * @return	본 builder 객체.
			 */
			public Builder value(Integer value) {
				return value(PropertyValue.INTEGER(value));
			}

			/**
			 * 인자의 리터럴 값을 {@code float} 값으로 설정한다.
			 *
			 * @param value	리터럴 값.
			 * @return	본 builder 객체.
			 */
			public Builder value(Float value) {
				return value(PropertyValue.FLOAT(value));
			}

			/**
			 * 인자의 리터럴 값을 {@code double} 값으로 설정한다.
			 *
			 * @param value	리터럴 값.
			 * @return	본 builder 객체.
			 */
			public Builder value(Double value) {
				return value(PropertyValue.DOUBLE(value));
			}

			/**
			 * 인자의 리터럴 값을 문자열 값으로 설정한다.
			 *
			 * @param value	리터럴 값.
			 * @return	본 builder 객체.
			 */
			public Builder value(String value) {
				return value(PropertyValue.STRING(value));
			}
		}

		/**
		 * 인자의 리터럴 값을 반환한다.
		 *
		 * @return	리터럴 값.
		 */
		public ElementValue getValue() {
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
			Builder builder = builder();
			deserializeCommonFields(jnode, builder);

			JsonNode valueNode = JacksonUtils.getFieldOrNull(jnode, FIELD_VALUE);
			return builder.value(ElementValues.parseJsonNode(valueNode))
							.build();
		}

		@Override
		public String toString() {
			return String.format("literal(%s)", m_value.toValueJsonString());
		}
	}

	/**
	 * {@link ArgumentSpec} 객체를 {@code @type} 필드를 포함한 JSON으로 직렬화하는 Jackson serializer.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<ArgumentSpec> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<ArgumentSpec> cls) {
			super(cls);
		}

		@Override
		public void serialize(ArgumentSpec arg, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_TYPE, arg.getSerializationType());
			gen.writeStringField(FIELD_ID, arg.getId());
			// 이름과 설명은 선택 사항이기 때문에, 설정된 경우에만 직렬화한다.
			if ( arg.getName() != null ) {
				gen.writeStringField(FIELD_NAME, arg.getName());
			}
			if ( arg.getDescription() != null ) {
				gen.writeStringField(FIELD_DESCRIPTION, arg.getDescription());
			}
			arg.serializeFields(gen);
			gen.writeEndObject();
		}
	}

	/**
	 * {@code @type} 필드를 보고 해당하는 {@link ArgumentSpec} 구현체로 역직렬화하는 Jackson deserializer.
	 */
	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<ArgumentSpec> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public ArgumentSpec deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			return parseTypedJsonNode(jnode);
		}
	}

	/**
	 * {@code @type} 필드를 가진 Json 노드로부터 해당 타입의 {@link ArgumentSpec}를 복원한다.
	 *
	 * @param jnode	{@code @type} 필드를 포함한 Json 노드.
	 * @return 복원된 {@link ArgumentSpec} 객체.
	 * @throws IOException	{@code @type}이 없거나 미등록 타입이거나, 해석 과정에서 예외가 발생한 경우.
	 */
	public static ArgumentSpec parseTypedJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new JacksonDeserializationException(String.format("'%s' field is missing: json=%s",
																	FIELD_TYPE, jnode));
		}
		switch ( type ) {
			case ReferenceArgument.SERIALIZATION_TYPE:
				return ReferenceArgument.deserializeFields(jnode);
			case TaskOutputArgument.SERIALIZATION_TYPE:
				return TaskOutputArgument.deserializeFields(jnode);
			case LiteralArgument.SERIALIZATION_TYPE:
				return LiteralArgument.deserializeFields(jnode);
			default:
				throw new JacksonDeserializationException("Unregistered ArgumentSpec type: " + type);
		}
	}
}
