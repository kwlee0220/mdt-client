package mdt.workflow.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import mdt.model.expr.MDTParserException;
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
 * 	<li>{@link ReferenceArgument}: MDT 요소 참조 <em>표현식</em>(예: {@code param:inst:Speed})으로 값의 위치를
 * 		지정하는 인자. 참조 객체({@link MDTElementReference})는 실행 시점에 표현식을 해석해 얻는다.</li>
 * 	<li>{@link TaskOutputArgument}: 다른 태스크의 출력 값을 참조하는 인자.
 * 		값은 해당 태스크의 수행 결과로부터 결정된다.</li>
 * 	<li>{@link LiteralArgument}: 고정된 리터럴 값({@link ElementValue})을 갖는 인자.</li>
 * </ul>
 * 본 클래스는 인자를 <em>기술</em>하기만 하며, 인자 값을 실제로 읽고 쓰는 것은 명세를 해석하는 태스크의 몫이다
 * (예: {@code mdt.task.TaskUtils.readArgumentValue}).
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
 * <p>
 * {@link mdt.task.TaskArgument}와는 다른 타입이다. {@code ArgumentSpec}은 워크플로 기술자({@link TaskDescriptor})에
 * 저장되는 명세로 식별자·이름·설명을 갖고 다른 태스크의 출력도 가리킬 수 있다. {@code TaskArgument}는 태스크를
 * 실행할 때 넘기는, 실제 참조·값을 담은 인자다.
 * <p>
 * JSON 형식은 {@code {"@type": "mdt:task_arg:reference", "id": …, "name": …, "description": …, "reference": "<표현식>"}},
 * {@code {"@type": "mdt:task_arg:task-output", …, "taskId": …, "argumentName": …}},
 * {@code {"@type": "mdt:task_arg:literal", …, "value": {…}}}이다({@code name}·{@code description}은 선택).
 * 판별자는 워크플로 기술자에 저장되므로 바꾸지 않는다.
 * <p>
 * <b>동등성:</b> 세 하위 클래스는 같은 클래스끼리 공통 필드(식별자·이름·설명)와 자기 필드가 모두 같으면
 * {@code equals}가 참이다. 리터럴 명세는 {@link ElementValue}를 복사하지 않고 보관하므로, 그 값이 가변이고
 * 바뀌면 {@code equals}·{@code hashCode}도 바뀐다. 해시 기반 컬렉션에 넣은 명세의 값은 바꾸지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ArgumentSpec.Serializer.class)
@JsonDeserialize(using = ArgumentSpec.Deserializer.class)
public abstract sealed class ArgumentSpec
		permits ArgumentSpec.ReferenceArgument, ArgumentSpec.TaskOutputArgument,
				ArgumentSpec.LiteralArgument {
	// 새 ArgumentSpec 구현체를 추가하면 반드시 여기에 등록해야 polymorphic JSON 역직렬화(parseJsonNode)가 가능하다.
	// 같은 타입 이름을 두 번 등록하면 클래스 초기화 시 IllegalArgumentException이 발생한다.
	private static final MultiTypeJsonNodeDeserializer<ArgumentSpec> DESERIALIZER
		= MultiTypeJsonNodeDeserializer.builder(ArgumentSpec.class)
			.register(ReferenceArgument.SERIALIZATION_TYPE, ReferenceArgument::deserializeFields)
			.register(TaskOutputArgument.SERIALIZATION_TYPE, TaskOutputArgument::deserializeFields)
			.register(LiteralArgument.SERIALIZATION_TYPE, LiteralArgument::deserializeFields)
			.build();
	// Serializer가 쓰는 타입 필드 이름. 역직렬화 쪽과 항상 같도록 등록부에서 가져온다.
	private static final String FIELD_TYPE = DESERIALIZER.getTypeField();
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
	 * @throws IllegalArgumentException	builder에 인자 식별자가 설정되지 않았거나 공백뿐인 경우.
	 */
	protected ArgumentSpec(AbstractBuilder<?,?> builder) {
		Preconditions.checkNotNullArgument(builder.m_id, "id is not set");
		Preconditions.checkArgument(!builder.m_id.isBlank(), "id is blank");

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

	/**
	 * 공통 필드(식별자, 이름, 설명)가 같은지 비교한다. 하위 클래스의 {@code equals}가 쓴다.
	 *
	 * @param other	비교할 인자 명세.
	 * @return	공통 필드가 모두 같으면 {@code true}.
	 */
	protected final boolean commonFieldsEqual(ArgumentSpec other) {
		return m_id.equals(other.m_id)
				&& Objects.equals(m_name, other.m_name)
				&& Objects.equals(m_description, other.m_description);
	}

	/**
	 * 공통 필드와 하위 클래스 필드로 해시값을 만든다. 하위 클래스의 {@code hashCode}가 쓴다.
	 *
	 * @param fields	해시값에 포함할 하위 클래스 필드들.
	 * @return	해시값.
	 */
	protected final int commonFieldsHash(Object... fields) {
		return Objects.hash(m_id, m_name, m_description, Arrays.hashCode(fields));
	}
	
	/**
	 * 모든 {@link ArgumentSpec} builder의 공통 속성(식별자, 이름, 설명)을 제공하는 기반 builder.
	 *
	 * @param <B>	구체 builder 타입. (fluent 메소드 chaining을 위한 self type)
	 * @param <T>	builder가 생성하는 {@link ArgumentSpec} 타입.
	 */
	public abstract static class AbstractBuilder<B extends AbstractBuilder<B,T>, T extends ArgumentSpec> {
		// build()에서 검증하므로 설정 전에는 null이다.
		/** 설정된 인자 식별자. 설정 전에는 {@code null}. */
		@Nullable protected String m_id;
		/** 설정된 인자 이름. 설정 전에는 {@code null}. */
		@Nullable protected String m_name;
		/** 설정된 인자 설명. 설정 전에는 {@code null}. */
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

		/**
		 * method chaining을 위해 자신을 하위 builder 타입으로 반환한다.
		 *
		 * @return	본 builder 객체.
		 */
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
	 * @throws JacksonDeserializationException	필수 필드인 식별자가 없거나, JSON 문자열이 아니거나, 공백뿐인 경우.
	 */
	protected static void deserializeCommonFields(JsonNode jnode, AbstractBuilder<?,?> builder)
		throws IOException {
		builder.id(readRequiredText(jnode, FIELD_ID));
		builder.name(JacksonUtils.getStringFieldOrNull(jnode, FIELD_NAME));
		builder.description(JacksonUtils.getStringFieldOrNull(jnode, FIELD_DESCRIPTION));
	}

	/**
	 * 비어 있지 않은 JSON 문자열이어야 하는 필수 필드를 읽는다. 식별자({@code id}, {@code taskId},
	 * {@code argumentName})와 참조 표현식({@code reference})에 쓴다.
	 * <p>
	 * 숫자·불리언을 문자열로 바꿔 받지 않는다({@code "id": 1} 거부). 공백뿐인 값은 생성자의
	 * {@code IllegalArgumentException} 대신 필드 이름을 담은 JSON 경로의 예외로 알린다.
	 */
	static String readRequiredText(JsonNode jnode, String field) throws JacksonDeserializationException {
		String value = JacksonUtils.getTextField(jnode, field);
		if ( value.isBlank() ) {
			throw new JacksonDeserializationException("invalid field '" + field + "': blank");
		}
		return value;
	}

	// 이름과 설명이 없는 인자 명세를 간결하게 생성하기 위한 단축 factory 메소드들.
	// 식별자는 필수이기 때문에 항상 첫 인자로 받는다.
	// 이름이나 설명을 함께 지정하려면 각 하위 클래스의 builder()를 사용한다.

	/**
	 * 참조 인자 명세를 만든다.
	 *
	 * @param id			인자 식별자.
	 * @param refExpr		MDT 요소 참조 표현식(예: {@code param:inst:Speed}).
	 * @return	참조 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code id}가 공백뿐인 경우.
	 * @throws MDTParserException	참조 표현식의 문법이 틀린 경우.
	 */
	public static ReferenceArgument reference(String id, String refExpr) {
		return ReferenceArgument.builder().id(id).reference(refExpr).build();
	}
	/**
	 * 다른 태스크의 출력을 가리키는 인자 명세를 만든다.
	 *
	 * @param id		인자 식별자.
	 * @param taskId	출력을 낸 태스크의 식별자.
	 * @param argumentName	그 태스크의 출력 인자 식별자(원래 태스크의 출력 인자 {@code id}).
	 * @return	task-output 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 공백뿐인 경우.
	 */
	public static TaskOutputArgument taskOutput(String id, String taskId, String argumentName) {
		return TaskOutputArgument.builder().id(id).taskId(taskId).argumentName(argumentName).build();
	}
	/**
	 * 리터럴 인자 명세를 만든다. 아래의 {@code literal(String, Integer)} 등은 해당 타입의
	 * {@link PropertyValue}로 감싸는 편의 메소드다.
	 *
	 * @param id		인자 식별자.
	 * @param value		인자 값.
	 * @return	리터럴 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code id}가 공백뿐인 경우.
	 */
	public static LiteralArgument literal(String id, ElementValue value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	/**
	 * 값을 {@link PropertyValue#INTEGER(Integer)}로 감싼 리터럴 인자 명세를 만든다.
	 *
	 * @param id		인자 식별자.
	 * @param value		인자 값.
	 * @return	리터럴 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code id}가 공백뿐인 경우.
	 */
	public static LiteralArgument literal(String id, Integer value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	/**
	 * 값을 {@link PropertyValue#FLOAT(Float)}로 감싼 리터럴 인자 명세를 만든다.
	 *
	 * @param id		인자 식별자.
	 * @param value		인자 값.
	 * @return	리터럴 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code id}가 공백뿐인 경우.
	 */
	public static LiteralArgument literal(String id, Float value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	/**
	 * 값을 {@link PropertyValue#DOUBLE(Double)}로 감싼 리터럴 인자 명세를 만든다.
	 *
	 * @param id		인자 식별자.
	 * @param value		인자 값.
	 * @return	리터럴 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code id}가 공백뿐인 경우.
	 */
	public static LiteralArgument literal(String id, Double value) {
		return LiteralArgument.builder().id(id).value(value).build();
	}
	/**
	 * 값을 {@link PropertyValue#STRING(String)}로 감싼 리터럴 인자 명세를 만든다.
	 *
	 * @param id		인자 식별자.
	 * @param value		인자 값.
	 * @return	리터럴 인자 명세.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code id}가 공백뿐인 경우.
	 */
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

	/**
	 * 이 명세를 {@code @type} 필드를 포함한 JSON 노드로 바꾼다.
	 *
	 * @return	JSON 노드.
	 */
	public JsonNode toJsonNode() {
		return MDTModelSerDe.toJsonNode(this);
	}

	/**
	 * 이 명세를 {@code @type} 필드를 포함한 JSON 문자열로 바꾼다.
	 *
	 * @return	JSON 문자열.
	 */
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}

	/**
	 * {@code @type} 필드를 가진 JSON 노드로부터 해당 타입의 {@link ArgumentSpec}를 복원한다.
	 * <p>
	 * Jackson을 거치지 않고 등록부에 바로 넘기므로, 구현체가 던진 예외가 그대로 전달된다.
	 *
	 * @param jnode	{@code @type} 필드를 포함한 JSON 노드.
	 * @return 복원된 {@link ArgumentSpec} 객체.
	 * @throws IllegalArgumentException	{@code jnode}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 없거나(JSON {@code null} 포함), 문자열이 아니거나,
	 * 										미등록 타입이거나, 필수 필드가 없는 경우.
	 * @throws IOException	구현체가 노드를 해석하지 못한 경우.
	 */
	public static ArgumentSpec parseJsonNode(JsonNode jnode) throws IOException {
		return DESERIALIZER.deserialize(jnode);
	}

	/**
	 * {@code @type} 필드를 가진 JSON 문자열로부터 해당 타입의 {@link ArgumentSpec}를 복원한다.
	 * <p>
	 * 문자열을 트리로 읽은 뒤 {@link #parseJsonNode(JsonNode)}에 넘기므로 예외와 {@code null} 처리가 같다.
	 * 문자열 전체가 JSON 값 하나여야 한다. 첫 값 뒤에 다른 입력이 있으면(예: {@code {...} garbage}) 거부한다.
	 *
	 * @param jsonString	JSON 문자열.
	 * @return 복원된 {@link ArgumentSpec} 객체. {@code null}이 아니다.
	 * @throws IllegalArgumentException	{@code jsonString}이 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 없거나(JSON {@code null} 포함), 미등록 타입이거나,
	 * 										필수 필드가 없는 경우.
	 * @throws IOException	JSON 문법이 틀렸거나, 첫 JSON 값 뒤에 다른 입력이 있거나, 구현체가 노드를 해석하지 못한 경우.
	 */
	public static ArgumentSpec parseJsonString(String jsonString) throws IOException {
		Preconditions.checkNotNullArgument(jsonString, "JSON string is null");
		return parseJsonNode(MDTModelSerDe.readSingleJsonNode(jsonString));
	}
	
	/**
	 * MDT 요소 참조 표현식으로 값의 위치를 지정하는 인자 명세.
	 */
	public static final class ReferenceArgument extends ArgumentSpec {
		/** JSON 직렬화 시 {@code @type} 필드에 기록되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:task_arg:reference";
		private static final String FIELD_REFERENCE = "reference";

		private final String m_refExpr;

		private ReferenceArgument(Builder builder) {
			super(builder);
			Preconditions.checkNotNullArgument(builder.m_refExpr, "reference is not set");
			validateReferenceExpr(builder.m_refExpr);
			m_refExpr = builder.m_refExpr;
		}

		/**
		 * 참조 표현식의 문법을 확인한다. 참조 객체는 실행 시점에 다시 해석하므로 결과는 버린다.
		 */
		private static void validateReferenceExpr(String refExpr) {
			try {
				ElementReferences.parseExpr(refExpr);
			}
			catch ( MDTParserException | IllegalArgumentException e ) {
				// 문법 오류(후행 입력 포함)는 MDTParserException이다. parseExpr는 참조 객체를 만드는 평가 단계까지
				// 포함하므로, 그 단계의 의미 오류(IllegalArgumentException)도 같은 예외로 맞춘다.
				throw new MDTParserException("invalid reference expression: '" + refExpr + "', cause="
												+ e.getMessage(), e);
			}
		}

		/**
		 * 참조 표현식을 돌려준다.
		 *
		 * @return	참조 표현식.
		 */
		public String getReferenceExpr() {
			return m_refExpr;
		}
		
		/**
		 * 참조 표현식을 해석하고 주어진 관리자로 활성화한 참조 객체를 돌려준다. 부를 때마다 새로 해석한다.
		 *
		 * @param manager	MDT 인스턴스 관리자.
		 * @return	활성화된 참조.
		 * @throws MDTParserException	참조 표현식이 올바르지 않은 경우.
		 */
		public MDTElementReference resolveReference(MDTInstanceManager manager) {
			MDTElementReference ref = ElementReferences.parseExpr(m_refExpr);
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
			private String m_refExpr;

			@Override
			public ReferenceArgument build() {
				return new ReferenceArgument(this);
			}

			/**
			 * 인자의 값을 읽고 쓸 MDT 요소 참조를 설정한다.
			 *
			 * @param refExpr	MDT 요소 참조 표현식. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder reference(String refExpr) {
				m_refExpr = refExpr;
				return this;
			}
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeStringField(FIELD_REFERENCE, m_refExpr);
		}

		/**
		 * {@code reference} 필드로부터 참조 인자 명세를 복원한다.
		 *
		 * @param jnode	공통 필드와 {@code reference} 필드를 가진 JSON 노드.
		 * @return	참조 인자 명세.
		 * @throws JacksonDeserializationException	필수 필드가 없거나, JSON 문자열이 아니거나, 참조 표현식의 문법이 틀린 경우.
		 */
		public static ReferenceArgument deserializeFields(JsonNode jnode) throws IOException {
			Builder builder = builder();
			deserializeCommonFields(jnode, builder);

			String refExpr = readRequiredText(jnode, FIELD_REFERENCE);
			try {
				return builder.reference(refExpr)
								.build();
			}
			catch ( MDTParserException e ) {
				throw new JacksonDeserializationException(String.format("invalid field '%s': %s",
																		FIELD_REFERENCE, e.getMessage()), e);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || ReferenceArgument.class != obj.getClass() ) {
				return false;
			}
			ReferenceArgument other = (ReferenceArgument)obj;
			return commonFieldsEqual(other) && m_refExpr.equals(other.m_refExpr);
		}

		@Override
		public int hashCode() {
			return commonFieldsHash(m_refExpr);
		}

		@Override
		public String toString() {
			return String.format("%s=reference(%s)", getId(), m_refExpr);
		}
	}
	
	/**
	 * 다른 태스크의 출력 인자를 가리키는 인자 명세. 값은 그 태스크의 수행 결과로부터 실행 엔진이 정한다.
	 */
	public static final class TaskOutputArgument extends ArgumentSpec {
		/** JSON 직렬화 시 {@code @type} 필드에 기록되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:task_arg:task-output";
		private static final String FIELD_TASK_ID = "taskId";
		private static final String FIELD_ARGUMENT_NAME = "argumentName";

		private final String m_taskId;
		private final String m_argumentName;

		private TaskOutputArgument(Builder builder) {
			super(builder);
			Preconditions.checkNotNullArgument(builder.m_taskId, "taskId is not set");
			Preconditions.checkNotNullArgument(builder.m_argumentName, "argumentName is not set");
			Preconditions.checkArgument(!builder.m_taskId.isBlank(), "taskId is blank");
			Preconditions.checkArgument(!builder.m_argumentName.isBlank(), "argumentName is blank");

			m_taskId = builder.m_taskId;
			m_argumentName = builder.m_argumentName;
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
			private String m_argumentName;

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
			 * 가져올 출력 인자의 식별자(원래 태스크의 출력 인자 {@code id})를 설정한다. 메소드 이름은 JSON 필드
			 * {@code argumentName}을 따른다.
			 *
			 * @param argumentName	원래 태스크의 출력 인자 식별자({@code id}). ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder argumentName(String argumentName) {
				m_argumentName = argumentName;
				return this;
			}
		}

		/**
		 * 출력을 낸 태스크의 식별자를 돌려준다.
		 *
		 * @return	태스크 식별자.
		 */
		public String getTaskId() {
			return m_taskId;
		}

		/**
		 * 가리키는 출력 인자의 <b>식별자</b>를 돌려준다.
		 * <p>
		 * 원래 태스크가 결과로 내놓는 출력 인자의 {@code id}와 같아야 한다. 실행 엔진은 이 값을 키로 그 태스크의
		 * 출력을 찾는다(예: Airflow에서는 mdtpy {@code DagContext.get_task_output(task_id, arg_id)}). 표시용
		 * 이름({@link #getName()})이 아니다. JSON 필드 이름은 저장 형식 호환을 위해 {@code argumentName}을 유지한다.
		 *
		 * @return	출력 인자 식별자.
		 */
		public String getOutputArgumentId() {
			return m_argumentName;
		}
		
		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeStringField(FIELD_TASK_ID, m_taskId);
			gen.writeStringField(FIELD_ARGUMENT_NAME, m_argumentName);
		}

		/**
		 * {@code taskId}·{@code argumentName} 필드로부터 task-output 인자 명세를 복원한다.
		 *
		 * @param jnode	공통 필드와 {@code taskId}·{@code argumentName} 필드를 가진 JSON 노드.
		 * @return	task-output 인자 명세.
		 * @throws JacksonDeserializationException	필수 필드가 없거나, JSON 문자열이 아니거나, 공백뿐인 경우.
		 */
		public static TaskOutputArgument deserializeFields(JsonNode jnode) throws IOException {
			Builder builder = builder();
			deserializeCommonFields(jnode, builder);

			return builder.taskId(readRequiredText(jnode, FIELD_TASK_ID))
							.argumentName(readRequiredText(jnode, FIELD_ARGUMENT_NAME))
							.build();
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || TaskOutputArgument.class != obj.getClass() ) {
				return false;
			}
			TaskOutputArgument other = (TaskOutputArgument)obj;
			return commonFieldsEqual(other) && m_taskId.equals(other.m_taskId) && m_argumentName.equals(other.m_argumentName);
		}

		@Override
		public int hashCode() {
			return commonFieldsHash(m_taskId, m_argumentName);
		}

		@Override
		public String toString() {
			return String.format("%s=task_output(%s:%s)", getId(), m_taskId, m_argumentName);
		}
	}
	
	/**
	 * 고정된 값을 갖는 인자 명세.
	 */
	public static final class LiteralArgument extends ArgumentSpec {
		/** JSON 직렬화 시 {@code @type} 필드에 기록되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:task_arg:literal";
		private static final String FIELD_VALUE = "value";

		private final ElementValue m_value;

		private LiteralArgument(Builder builder) {
			super(builder);
			Preconditions.checkNotNullArgument(builder.m_value, "value is not set");

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
			 * <p>
			 * {@code null}인 {@link ElementValue}는 거부하지만, 안에 {@code null}을 담은 값(예:
			 * {@code PropertyValue.STRING(null)}, 값이 비어 있는 AAS 속성)은 그대로 받는다. 아래의
			 * {@code value(Integer)} 등 편의 메소드는 {@code null}을 거부한다.
			 *
			 * @param value	리터럴 값. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder value(ElementValue value) {
				// 편의 메소드들과 같은 메시지를 쓴다. 생성자의 "value is not set"은 value(…)를 부르지 않은 경우만 남는다.
				Preconditions.checkNotNullArgument(value, "value is null");
				m_value = value;
				return this;
			}

			/**
			 * 인자의 리터럴 값을 {@code int} 값으로 설정한다.
			 *
			 * @param value	리터럴 값. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder value(Integer value) {
				// null이면 값이 빈 PropertyValue가 만들어지므로 거부한다.
				Preconditions.checkNotNullArgument(value, "value is null");
				return value(PropertyValue.INTEGER(value));
			}

			/**
			 * 인자의 리터럴 값을 {@code float} 값으로 설정한다.
			 *
			 * @param value	리터럴 값. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder value(Float value) {
				// null이면 값이 빈 PropertyValue가 만들어지므로 거부한다.
				Preconditions.checkNotNullArgument(value, "value is null");
				return value(PropertyValue.FLOAT(value));
			}

			/**
			 * 인자의 리터럴 값을 {@code double} 값으로 설정한다.
			 *
			 * @param value	리터럴 값. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder value(Double value) {
				// null이면 값이 빈 PropertyValue가 만들어지므로 거부한다.
				Preconditions.checkNotNullArgument(value, "value is null");
				return value(PropertyValue.DOUBLE(value));
			}

			/**
			 * 인자의 리터럴 값을 문자열 값으로 설정한다.
			 *
			 * @param value	리터럴 값. ({@code null} 불가)
			 * @return	본 builder 객체.
			 */
			public Builder value(String value) {
				// null이면 값이 빈 PropertyValue가 만들어지므로 거부한다.
				Preconditions.checkNotNullArgument(value, "value is null");
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

		/**
		 * {@code value} 필드로부터 리터럴 인자 명세를 복원한다.
		 *
		 * @param jnode	공통 필드와 {@code value} 필드({@code @type}/{@code value} 형태의 값)를 가진 JSON 노드.
		 * @return	리터럴 인자 명세.
		 * @throws JacksonDeserializationException	공통 필드나 {@code value} 필드가 잘못되었거나, 값의 {@code @type}이
		 * 										없거나 미등록 타입인 경우.
		 * @throws IOException	값을 해석하지 못한 경우.
		 */
		public static LiteralArgument deserializeFields(JsonNode jnode) throws IOException {
			Builder builder = builder();
			deserializeCommonFields(jnode, builder);

			// 필수 필드다.
			JsonNode valueNode = JacksonUtils.getField(jnode, FIELD_VALUE);
			return builder.value(ElementValues.parseJsonNode(valueNode))
							.build();
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || LiteralArgument.class != obj.getClass() ) {
				return false;
			}
			LiteralArgument other = (LiteralArgument)obj;
			return commonFieldsEqual(other) && m_value.equals(other.m_value);
		}

		@Override
		public int hashCode() {
			return commonFieldsHash(m_value);
		}

		@Override
		public String toString() {
			return String.format("%s=literal(%s)", getId(), m_value.toValueJsonString());
		}
	}

	/**
	 * {@link ArgumentSpec} 객체를 {@code @type} 필드를 포함한 JSON으로 직렬화하는 Jackson serializer.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<ArgumentSpec> {
		private Serializer() {
			super(ArgumentSpec.class);
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
	public static class Deserializer extends JsonNodeStdDeserializer<ArgumentSpec> {
		/**
		 * {@link ArgumentSpec} 등록부를 사용하는 deserializer를 만든다.
		 */
		public Deserializer() {
			super(ArgumentSpec.class, DESERIALIZER);
		}
	}
}
