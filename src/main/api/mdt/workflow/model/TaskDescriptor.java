package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import utils.InternalException;
import utils.Preconditions;
import utils.func.FOption;
import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;


/**
 * 워크플로우를 구성하는 task 하나의 기술자.
 * <p>
 * task 식별자, 이름, type(task 구현 클래스 이름 등), 설명, 선행 task 식별자({@code dependencies}),
 * trigger rule, 입력·출력 인자 명세({@link ArgumentSpec}), 옵션({@link Option}), 레이블로 구성된다.
 * 입력·출력 인자 명세와 옵션은 추가된 순서를 유지한다. JSON으로 직렬화되며 {@code null}인 필드는 출력하지
 * 않는다.
 * <p>
 * <b>동등성:</b> {@code equals}/{@code hashCode}는 식별자({@link #getId()})만 비교한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "name", "type", "description", "dependencies", "triggerRule",
					"inputArguments", "outputArguments", "options", "labels"})
@JsonIncludeProperties({ "id", "name", "type", "description", "dependencies", "triggerRule",
					"inputArguments", "outputArguments", "options", "labels" })
@JsonInclude(Include.NON_NULL)
public final class TaskDescriptor {
	/** 호출할 연산 element의 참조 표현식을 지정하는 옵션 이름. */
	public static final String OPTION_OPERATION = "operation";
	/** 호출할 연산 Submodel의 참조 표현식을 지정하는 옵션 이름. */
	public static final String OPTION_OPERATION_SUBMODEL = "operationSubmodel";
	/** task 수행 제한 시간을 지정하는 옵션 이름 (선택). */
	public static final String OPTION_TIMEOUT = "timeout";	// optional
	/** 원격 연산의 상태 확인(polling) 주기를 지정하는 옵션 이름 (선택). */
	public static final String OPTION_POLL_INTERVAL = "poll";    // optional
	/** task 수행 중 사용할 로그 레벨을 지정하는 옵션 이름. */
	public static final String OPTION_LOG_LEVEL = "loglevel";
	/** task가 수행하는 연산 Submodel의 참조 표현식을 담는 레이블 이름. */
	public static final String LABEL_MDT_OPERATION = "mdt-operation";
	
	private String m_id;
	private @Nullable String m_name;
	private String m_type;
	private @Nullable String m_description;
	
	private Set<String> m_dependencies = new HashSet<>();
	private TriggerRule m_triggerRule = TriggerRule.ALL_SUCCESS;
	private LinkedHashMap<String,ArgumentSpec> m_inputArgumentSpecs = new LinkedHashMap<>();
	private LinkedHashMap<String,ArgumentSpec.ReferenceArgument> m_outputArgumentSpecs = new LinkedHashMap<>();
	private Map<String, Option> m_options = new LinkedHashMap<>();
	private List<NameValue> m_labels = new ArrayList<>();
	
	/**
	 * 모든 필드가 비어 있는 기술자를 만든다. 주로 JSON 역직렬화에서 사용된다.
	 */
	public TaskDescriptor() { }
	
	/**
	 * 식별자, 이름, type을 갖는 기술자를 만든다. 인자는 검사하지 않는다.
	 *
	 * @param id	task 식별자.
	 * @param name	task 이름.
	 * @param type	task type.
	 */
	public TaskDescriptor(String id, String name, String type) {
		m_id = id;
		m_name = name;
		m_type = type;
	}
	
	/**
	 * task 식별자를 반환한다.
	 *
	 * @return	task 식별자. 설정되지 않은 경우 {@code null}.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * task 식별자를 설정한다.
	 *
	 * @param id	task 식별자.
	 * @throws IllegalArgumentException	{@code id}가 {@code null}인 경우.
	 */
	public void setId(String id) {
		Preconditions.checkNotNullArgument(id, "id must not be null");

		m_id = id;
	}
	
	/**
	 * task 이름을 반환한다.
	 *
	 * @return	task 이름. 설정되지 않은 경우 {@code null}.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * task 이름을 설정한다.
	 *
	 * @param name	task 이름. {@code null} 가능.
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * task type을 반환한다. 보통 task 구현 클래스의 전체 이름이다
	 * (예: {@code mdt.task.AASOperationTask}, {@link TaskDescriptors#PYTHON_SCRIPT_TASK_TYPE}).
	 *
	 * @return	task type. 설정되지 않은 경우 {@code null}.
	 */
	public String getType() {
		return m_type;
	}
	
	/**
	 * task type을 설정한다.
	 *
	 * @param type	task type.
	 * @throws IllegalArgumentException	{@code type}이 {@code null}인 경우.
	 */
	public void setType(String type) {
		Preconditions.checkNotNullArgument(type, "type must not be null");

		m_type = type;
	}
	
	/**
	 * task 설명을 반환한다.
	 *
	 * @return	task 설명. 설정되지 않은 경우 {@code null}.
	 */
	public String getDescription() {
		return m_description;
	}
	
	/**
	 * task 설명을 설정한다.
	 *
	 * @param description	task 설명. {@code null} 가능.
	 */
	public void setDescription(String description) {
		m_description = description;
	}
	
	/**
	 * 이 task보다 먼저 수행되어야 하는 선행 task들의 식별자 집합을 반환한다.
	 *
	 * @return	선행 task 식별자 집합 (수정 불가 뷰).
	 */
	public Set<String> getDependencies() {
		return Collections.unmodifiableSet(m_dependencies);
	}
	
	/**
	 * 선행 task 식별자들을 추가한다.
	 *
	 * @param dependency	추가할 선행 task 식별자들.
	 * @throws IllegalArgumentException	{@code dependency} 배열이 {@code null}인 경우.
	 */
	public void addDependency(String... dependency) {
		Preconditions.checkNotNullArgument(dependency, "dependency must not be null");

		FStream.of(dependency).forEach(m_dependencies::add);
	}
	
	/**
	 * 선행 task 식별자 집합을 설정한다. 기존 집합은 대체된다.
	 *
	 * @param dependencies	선행 task 식별자들.
	 * @throws IllegalArgumentException	{@code dependencies}가 {@code null}인 경우.
	 */
	public void setDependencies(Iterable<String> dependencies) {
		Preconditions.checkNotNullArgument(dependencies, "dependencies must not be null");

		m_dependencies = Sets.newHashSet(dependencies);
	}

	/**
	 * 직전 task들의 종료 상태에 따라 이 task를 실행할지 정하는 규칙을 반환한다.
	 *
	 * @return	trigger rule. 지정하지 않았으면 {@link TriggerRule#ALL_SUCCESS}.
	 */
	@NotNull
	public TriggerRule getTriggerRule() {
		return m_triggerRule;
	}

	/**
	 * 직전 task들의 종료 상태에 따라 이 task를 실행할지 정하는 규칙을 설정한다.
	 *
	 * @param rule	trigger rule.
	 * @throws IllegalArgumentException	{@code rule}이 {@code null}인 경우.
	 */
	public void setTriggerRule(TriggerRule rule) {
		Preconditions.checkNotNullArgument(rule, "trigger rule must not be null");

		m_triggerRule = rule;
	}

	// 기본값은 JSON에 기록하지 않아 기존 워크플로 JSON과 동일한 결과를 유지한다.
	@JsonProperty("triggerRule")
	private @Nullable TriggerRule getTriggerRuleForJackson() {
		return (m_triggerRule != TriggerRule.ALL_SUCCESS) ? m_triggerRule : null;
	}

	// 생략하거나 null이면 기본값을 쓴다.
	@JsonProperty("triggerRule")
	private void setTriggerRuleForJackson(@Nullable TriggerRule rule) {
		m_triggerRule = (rule != null) ? rule : TriggerRule.ALL_SUCCESS;
	}

	/**
	 * 주어진 식별자의 입력 인자 명세를 반환한다.
	 *
	 * @param argId	입력 인자 식별자.
	 * @return	입력 인자 명세. 없으면 {@code null}.
	 */
	public ArgumentSpec getInputArgument(String argId) {
		return m_inputArgumentSpecs.get(argId);
	}
	/**
	 * 입력 인자 명세를 추가한다.
	 *
	 * @param spec	추가할 입력 인자 명세.
	 * @throws IllegalArgumentException	{@code spec}이 {@code null}이거나, 같은 식별자의 명세가 이미 있는 경우.
	 */
	public void addInputArgument(ArgumentSpec spec) {
		Preconditions.checkNotNullArgument(spec, "Input argument spec must not be null");
		if ( m_inputArgumentSpecs.containsKey(spec.getId()) ) {
			throw new IllegalArgumentException("Input argument spec already exists: " + spec.getId());
		}
		m_inputArgumentSpecs.put(spec.getId(), spec);
	}
	
	/**
	 * 입력 인자 명세들을 추가된 순서대로 반환한다.
	 *
	 * @return	입력 인자 명세 목록 (새 리스트).
	 */
	@JsonProperty("inputArguments")
	public List<ArgumentSpec> getInputArguments() {
		return Lists.newArrayList(m_inputArgumentSpecs.values());
	}

	/**
	 * 입력 인자 명세들을 설정한다. 기존 명세는 모두 지운다.
	 * <p>
	 * {@link #addInputArgument(ArgumentSpec)}와 같이 식별자가 중복되면 거부한다.
	 *
	 * @param vars	입력 인자 명세들.
	 * @throws IllegalArgumentException	식별자가 중복된 명세가 있는 경우.
	 */
	@JsonProperty("inputArguments")
	public void setInputArguments(Iterable<ArgumentSpec> vars) {
		Preconditions.checkNotNullArgument(vars, "input argument specs must not be null");
		LinkedHashMap<String,ArgumentSpec> specs = new LinkedHashMap<>();
		for ( ArgumentSpec spec: vars ) {
			Preconditions.checkNotNullArgument(spec, "Input argument spec must not be null");
			if ( specs.putIfAbsent(spec.getId(), spec) != null ) {
				throw new IllegalArgumentException("Input argument spec already exists: " + spec.getId());
			}
		}
		m_inputArgumentSpecs = specs;
	}
	
	/**
	 * 주어진 식별자의 출력 인자 명세를 반환한다.
	 *
	 * @param argId	출력 인자 식별자.
	 * @return	출력 인자 명세. 없으면 {@code null}.
	 */
	public ArgumentSpec.ReferenceArgument getOutputArgument(String argId) {
		return m_outputArgumentSpecs.get(argId);
	}
	/**
	 * 출력 인자 명세를 추가한다.
	 *
	 * @param spec	추가할 출력 인자 명세.
	 * @throws IllegalArgumentException	{@code spec}이 {@code null}이거나, 같은 식별자의 명세가 이미 있는 경우.
	 */
	public void addOutputArgument(ArgumentSpec.ReferenceArgument spec) {
		Preconditions.checkNotNullArgument(spec, "Output argument spec must not be null");
		if ( m_outputArgumentSpecs.containsKey(spec.getId()) ) {
			throw new IllegalArgumentException("Output argument spec already exists: " + spec.getId());
		}
		m_outputArgumentSpecs.put(spec.getId(), spec);
	}
	
	/**
	 * 출력 인자 명세들을 추가된 순서대로 반환한다.
	 *
	 * @return	출력 인자 명세 목록 (새 리스트).
	 */
	@JsonProperty("outputArguments")
	public List<ArgumentSpec.ReferenceArgument> getOutputArguments() {
		return Lists.newArrayList(m_outputArgumentSpecs.values());
	}

	/**
	 * 출력 인자 명세들을 설정한다. 기존 명세는 모두 지운다.
	 * <p>
	 * {@link #addOutputArgument(ArgumentSpec.ReferenceArgument)}와 같이 식별자가 중복되면 거부한다.
	 *
	 * @param args	출력 인자 명세들.
	 * @throws IllegalArgumentException	식별자가 중복된 명세가 있는 경우.
	 */
	@JsonProperty("outputArguments")
	public void setOutputArguments(Iterable<ArgumentSpec.ReferenceArgument> args) {
		Preconditions.checkNotNullArgument(args, "output argument specs must not be null");
		LinkedHashMap<String,ArgumentSpec.ReferenceArgument> specs = new LinkedHashMap<>();
		for ( ArgumentSpec.ReferenceArgument spec: args ) {
			Preconditions.checkNotNullArgument(spec, "Output argument spec must not be null");
			if ( specs.putIfAbsent(spec.getId(), spec) != null ) {
				throw new IllegalArgumentException("Output argument spec already exists: " + spec.getId());
			}
		}
		m_outputArgumentSpecs = specs;
	}
	
	/**
	 * 옵션 이름을 키로 하는 옵션 맵을 반환한다. 내부 맵을 그대로 반환하므로 수정하면 기술자에 반영된다.
	 *
	 * @return	옵션 맵.
	 */
	@NotNull
	public Map<String,Option> getOptions() {
		return m_options;
	}
	
	/**
	 * 주어진 이름의 옵션 값을 찾는다.
	 *
	 * @param optName	옵션 이름.
	 * @return	옵션 값. 옵션이 없으면 {@link FOption#empty()}. 값이 {@code null}인 옵션이면 {@code null}을 담은
	 * 			{@link FOption}.
	 */
	public FOption<String> findOptionValue(String optName) {
		return FOption.ofNullable(m_options.get(optName))
						.map(Option::getValue);
	}
	
	/**
	 * 옵션을 추가한다. 같은 이름의 옵션이 있으면 대체한다.
	 *
	 * @param name	옵션 이름.
	 * @param value	옵션 값.
	 * @throws IllegalArgumentException	{@code name}이 {@code null}인 경우.
	 */
	public void addOption(String name, String value) {
		m_options.put(name, new Option(name, value));
	}
	
	/**
	 * JSON 직렬화용으로 옵션들을 추가된 순서대로 반환한다.
	 *
	 * @return	옵션 컬렉션 (내부 맵의 value 뷰).
	 */
	@JsonProperty("options")
	public Collection<Option> getOptionsForJackson() {
		return m_options.values();
	}
	
	/**
	 * JSON 역직렬화용으로 옵션들을 설정한다. 기존 옵션은 대체되며, 같은 이름의 옵션이 여럿이면 마지막 것이
	 * 남는다.
	 *
	 * @param options	옵션 컬렉션.
	 * @throws IllegalArgumentException	{@code options}가 {@code null}인 경우.
	 */
	@JsonProperty("options")
	public void setOptionsForJackson(Collection<Option> options) {
		Preconditions.checkNotNullArgument(options, "options must not be null");
		
		m_options = FStream.from(options)
							.tagKey(Option::getName)
							.toMap(new LinkedHashMap<>());
	}
	
	/**
	 * 레이블 목록을 반환한다. 같은 이름의 레이블이 여럿 있을 수 있다.
	 *
	 * @return	레이블 목록 (수정 불가 뷰).
	 */
	public List<NameValue> getLabels() {
		return Collections.unmodifiableList(m_labels);
	}
	/**
	 * 주어진 이름을 갖는 첫 레이블의 값을 찾는다.
	 *
	 * @param name	레이블 이름.
	 * @return	레이블 값. 없으면 {@link FOption#empty()}.
	 */
	public FOption<String> findLabel(String name) {
		return FStream.from(m_labels)
						.findFirst(lb -> lb.getName().equals(name))
						.map(NameValue::getValue);
	}

	/**
	 * 레이블을 목록 끝에 추가한다. 같은 이름의 레이블이 있어도 대체하지 않는다.
	 *
	 * @param name	레이블 이름.
	 * @param value	레이블 값.
	 * @throws IllegalArgumentException	{@code name} 또는 {@code value}가 {@code null}인 경우.
	 */
	public void addLabel(String name, String value) {
		Preconditions.checkNotNullArgument(name, "label name must not be null");
		Preconditions.checkNotNullArgument(value, "label value must not be null");

		m_labels.add(NameValue.of(name, value));
	}

	/**
	 * 레이블 목록을 설정한다. 기존 목록은 대체된다.
	 *
	 * @param labels	레이블들.
	 * @throws IllegalArgumentException	{@code labels}가 {@code null}인 경우.
	 */
	public void setLabels(Iterable<NameValue> labels) {
		Preconditions.checkNotNullArgument(labels, "labels must not be null");
		
		m_labels = Lists.newArrayList(labels);
	}

	/**
	 * JSON 문자열을 파싱하여 기술자를 만든다.
	 *
	 * @param json	기술자 JSON 문자열.
	 * @return	task 기술자.
	 * @throws IOException	JSON 문법이 올바르지 않거나 기술자로 변환할 수 없는 경우.
	 */
	public static TaskDescriptor parseJsonString(String json) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(json, TaskDescriptor.class);
	}
	/**
	 * JSON 파일을 읽어 기술자를 만든다.
	 *
	 * @param jsonFile	기술자 JSON 파일.
	 * @return	task 기술자.
	 * @throws IOException	파일을 읽지 못했거나, JSON을 기술자로 변환할 수 없는 경우.
	 */
	public static TaskDescriptor parseJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile, TaskDescriptor.class);
	}
	
	/**
	 * 기술자 JSON 배열을 담은 파일을 읽어 기술자 목록을 만든다.
	 *
	 * @param jsonFile	기술자 JSON 배열 파일.
	 * @return	task 기술자 목록.
	 * @throws IOException	파일을 읽지 못했거나, JSON을 기술자 목록으로 변환할 수 없는 경우.
	 */
	public static List<TaskDescriptor> parseListJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile,
													new TypeReference<List<TaskDescriptor>>() {});
	}
	
	/**
	 * task의 서명 문자열 {@code <짧은 type>[<id>]: (<입력 인자들>) -> (<출력 인자들>)}을 만든다.
	 *
	 * @return	서명 문자열.
	 * @throws IllegalArgumentException	type이 {@link TaskDescriptors#toShortTaskTypeId(String)}에 등록되지
	 * 									않은 경우.
	 */
	public String toSignatureString() {
		return formatSignature(TaskDescriptors.toShortTaskTypeId(m_type));
	}
	
	private String formatSignature(String taskTypeId) {
		String inArgNames = FStream.from(m_inputArgumentSpecs.keySet()).join(", ");
		String outArgNames = FStream.from(m_outputArgumentSpecs.keySet()).join(", ");
		return String.format("%s[%s]: (%s) -> (%s)", taskTypeId, getId(), inArgNames, outArgNames);
	}
	
	/**
	 * 이 기술자를 JSON 문자열로 변환한다.
	 *
	 * @return	JSON 문자열.
	 * @throws InternalException	직렬화에 실패한 경우.
	 */
	public String toJsonString() {
		try {
			return MDTModelSerDe.getJsonMapper()
								.writerFor(TaskDescriptor.class)
								.writeValueAsString(this);
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to serialize TaskDescriptor: %s", this);
			throw new InternalException(msg, e);
		}
	}
	private static final int CHUNK_SIZE = 200;
	/**
	 * 이 기술자의 JSON 문자열을 UTF-8 바이트의 Base64 문자열로 인코딩한 뒤 200자 단위 조각으로 나눈다.
	 * <p>
	 * {@link #parseEncodedString(List)}로 다시 복원할 수 있다.
	 *
	 * @return	Base64 문자열 조각 목록.
	 * @throws InternalException	JSON 직렬화에 실패한 경우.
	 */
	public List<String> toEncodedString() {
		byte[] bytes = toJsonString().getBytes(StandardCharsets.UTF_8);
		String fullStr = IOUtils.stringify(bytes);
		
	    int length = fullStr.length();
	    List<String> chunks = new ArrayList<>();
	    for (int i = 0; i < length; i += CHUNK_SIZE) {
	        int endIndex = Math.min(i + CHUNK_SIZE, length);
	        chunks.add(fullStr.substring(i, endIndex));
	    }
	    
	    return chunks;
	}
	
	/**
	 * {@link #toEncodedString()}으로 만든 Base64 문자열 조각들을 이어 붙여 기술자를 복원한다.
	 *
	 * @param chunks	Base64 문자열 조각 목록.
	 * @return	task 기술자.
	 * @throws IOException	복원된 JSON을 기술자로 변환할 수 없는 경우.
	 * @throws InternalException	Base64 디코딩 등 그 밖의 오류가 발생한 경우.
	 */
	public static TaskDescriptor parseEncodedString(List<String> chunks) throws IOException {
		try {
			String encodedStr = FStream.from(chunks)
										.fold(new StringBuilder(), StringBuilder::append)
										.toString();

			byte[] bytes = IOUtils.destringify(encodedStr);
			String jsonStr = new String(bytes, StandardCharsets.UTF_8);
			return parseJsonString(jsonStr);
		}
		catch ( IOException e ) {
			// IOException는 그대로 던진다.(InternalException로 wrapping하지 않도록 함)
			throw e;
		}
		catch ( Exception e ) {
			throw new InternalException("Failed to parse encoded TaskDescriptor", e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		TaskDescriptor other = (TaskDescriptor)obj;
		return Objects.equal(m_id, other.m_id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_id);
	}
	
	/**
	 * 기술자를 디버깅용 문자열로 변환한다.
	 * <p>
	 * {@link #toSignatureString()}과 달리 예외를 던지지 않는다. type이 {@code null}이면 {@code ?}로,
	 * {@link TaskDescriptors#toShortTaskTypeId(String)}에 등록되지 않은 type이면 클래스 이름 그대로 표시한다.
	 *
	 * @return	기술자 문자열.
	 */
	@Override
	public String toString() {
		String shortType = toShortTaskTypeOrRaw(m_type);
		String taskName = (m_type == null) ? shortType : switch ( m_type ) {
			case "mdt.task.SetTask" -> "Set";
//			case "mdt.task.CopyTask" -> "Copy";
			case "mdt.task.ProgramTask" -> "Program";
			case "mdt.task.HttpTask" -> "Http";
			case "mdt.task.AASOperationTask" -> "AAS";
			default -> shortType;
		};

//		String optList = FStream.from(m_options)
//								.flatMapIterable(Option::toCommandOptionSpec)
//								.join(", ", "[", "]");
		return String.format("%s: %s, opts:%s", taskName, formatSignature(shortType), m_options);
	}
	
	private static String toShortTaskTypeOrRaw(String type) {
		if ( type == null ) {
			return "?";
		}
		try {
			return TaskDescriptors.toShortTaskTypeId(type);
		}
		catch ( IllegalArgumentException unregistered ) {
			return type;
		}
	}
}
