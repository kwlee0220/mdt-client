package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTSubmodelDescriptor;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdSubmodelReference;
import mdt.model.sm.simulation.Simulation;


/**
 * MDT 연산(Simulation/AI)의 입력 또는 출력 인자를 가리키는 {@link ElementReference} 구현체이다.
 * <p>
 * 대상 연산이 포함된 Submodel에 대한 참조와 인자의 종류({@link MDTArgumentKind}), 그리고 인자 명세
 * (argSpec)로 대상 인자 값을 지정한다. 인자 명세는 다음 형태를 가질 수 있다.
 * <ul>
 *     <li>인자 이름 (예: {@code "UpperImage"})
 *     <li>인덱스 번호 (예: {@code "0"})
 *     <li>{@code "*"}: 모든 인자
 * </ul>
 * <p>
 * Json 직렬화 시 {@link #SERIALIZATION_TYPE}({@code "mdt:ref:oparg"}) 타입으로 식별되며, 인자의 값을
 * 읽거나 쓰기 위해서는 {@link #activate(MDTInstanceManager)}를 호출하여 참조를 활성화해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTArgumentReference extends SubmodelBasedElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:oparg";
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_ARG_SPEC = "argumentSpec";
	private static final String ALL_ARGS = "*";
	
	private final MDTArgumentKind m_kind;
	private final String m_argSpec;
	
	private MDTArgumentReference(MDTSubmodelReference submodelRef, MDTArgumentKind kind, String argSpec) {
		super(submodelRef);
		Preconditions.checkNotNullArgument(kind, "Null argument kind");
		Preconditions.checkNotNullArgument(argSpec, "Null argSpec");
		
		m_kind = kind;
		m_argSpec = argSpec;
	}
	
	/**
	 * 인자의 종류(입력/출력)를 반환한다.
	 *
	 * @return 인자의 종류를 나타내는 {@link MDTArgumentKind}.
	 */
	public MDTArgumentKind getKind() {
		return m_kind;
	}

	/**
	 * 인자 명세를 반환한다.
	 * <p>
	 * 인자의 이름 또는 인덱스 번호이거나, 모든 인자를 의미하는 {@code "*"}이다.
	 *
	 * @return 인자 명세 문자열.
	 */
	public String getArgumentSpec() {
		return m_argSpec;
	}

	/**
	 * 참조가 가리키는 인자 값을 읽어 반환한다.
	 * <p>
	 * 인자 명세가 {@code "*"}인 경우, 모든 인자를 모아 각 인자의 값 요소(idShort를 인자 이름으로 설정한
	 * 것)를 담은 {@link SubmodelElementList}를 반환한다. 그 외에는 지정된 단일 인자의 값을 반환한다.
	 *
	 * @return 인자 값에 해당하는 {@link SubmodelElement}.
	 * @throws IOException 읽기 과정에서 예외가 발생한 경우.
	 */
	@Override
	public SubmodelElement read() throws IOException {
		SubmodelElementList argSmel = super.readList();
		if ( !m_argSpec.equals(ALL_ARGS) ) {
			return argSmel;
		}

		String argKindStr = switch ( m_kind ) {
			case INPUT -> "Input";
			case OUTPUT -> "Output";
		};
		String idField = String.format("%sID", argKindStr);
		String valueField = String.format("%sValue", argKindStr);
		List<SubmodelElement> argList = FStream.from(argSmel.getValue())
												.castSafely(SubmodelElementCollection.class)
												.map(smc -> {
													String argName = SubmodelUtils.getStringFieldById(smc, idField);
													SubmodelElement field = SubmodelUtils.getFieldById(smc, valueField);
													field.setIdShort(argName);
													return field;
												})
												.toList();
		String idShort = String.format("%sArguments", argKindStr);
		return new DefaultSubmodelElementList.Builder().idShort(idShort).value(argList).build();
	}

	@Override
	public String toStringExpr() {
		String kindStr = switch ( m_kind ) {
			case INPUT -> "in";
			case OUTPUT -> "out";
		};
		return "oparg:" + getSubmodelReference().toStringExpr() + ":" + kindStr + ":" + m_argSpec;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || !(obj instanceof MDTArgumentReference) ) {
			return false;
		}

		MDTArgumentReference other = (MDTArgumentReference) obj;
		return Objects.equals(getSubmodelReference(), other.getSubmodelReference())
				&& Objects.equals(m_kind, other.m_kind)
				&& Objects.equals(m_argSpec, other.m_argSpec);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSubmodelReference(), m_kind, m_argSpec);
	}
	
	/**
	 * {@link MDTArgumentReference}를 생성하기 위한 {@link Builder}를 반환한다.
	 *
	 * @return 새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * {@link MDTArgumentReference}를 단계적으로 구성하는 빌더이다.
	 */
	public static class Builder {
		private DefaultSubmodelReference m_submodelRef;
		private MDTArgumentKind m_kind;
		private String m_argSpec;

		/**
		 * 지금까지 설정된 값으로 {@link MDTArgumentReference}를 생성한다.
		 *
		 * @return 생성된 {@link MDTArgumentReference} 객체.
		 */
		public MDTArgumentReference build() {
			return MDTArgumentReference.newInstance(m_submodelRef, m_kind, m_argSpec);
		}

		/**
		 * 인자가 속한 Submodel 참조를 설정한다.
		 *
		 * @param smRef	대상 Submodel에 대한 참조.
		 * @return 이 빌더 객체.
		 */
		public Builder submodelReference(DefaultSubmodelReference smRef) {
			m_submodelRef = smRef;
			return this;
		}

		/**
		 * 인자의 종류(입력/출력)를 설정한다.
		 *
		 * @param kind	인자의 종류.
		 * @return 이 빌더 객체.
		 */
		public Builder kind(MDTArgumentKind kind) {
			m_kind = kind;
			return this;
		}

		/**
		 * 인자 명세(이름, 인덱스 또는 {@code "*"})를 설정한다.
		 *
		 * @param argSpec	인자 명세 문자열.
		 * @return 이 빌더 객체.
		 */
		public Builder argument(String argSpec) {
			m_argSpec = argSpec;
			return this;
		}
	}

	/**
	 * Submodel 참조와 인자 종류, 인자 명세로 {@link MDTArgumentReference}를 생성한다.
	 *
	 * @param smRef		인자가 속한 Submodel에 대한 참조.
	 * @param kind		인자의 종류(입력/출력).
	 * @param argSpec	인자 명세(이름, 인덱스 또는 {@code "*"}).
	 * @return 생성된 {@link MDTArgumentReference} 객체.
	 */
	public static MDTArgumentReference newInstance(MDTSubmodelReference smRef, MDTArgumentKind kind,
													String argSpec) {
		return new MDTArgumentReference(smRef, kind, argSpec);
	}
	
//	static MDTArgumentReference fromMap(Map<String, String> props) {
//		String instanceId = (String)props.get("instanceId");
//		String submodelIdShort = (String)props.get("submodelIdShort");
//		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(instanceId, submodelIdShort);
//		
//		MDTArgumentKind kind = MDTArgumentKind.fromString(props.get(FIELD_KIND));
//		String argSpec = props.get(FIELD_ARG_SPEC);
//		
//		return new MDTArgumentReference(smRef, kind, argSpec);
//	}

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
		gen.writeStringField(FIELD_KIND, m_kind.name().toLowerCase());
		gen.writeStringField(FIELD_ARG_SPEC, m_argSpec);
	}

	/**
	 * Json 객체로부터 {@link MDTArgumentReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code submodelReference}, {@code kind}, {@code argumentSpec} 필드를 담은 Json 노드.
	 * @return 복원된 {@link MDTArgumentReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static MDTArgumentReference deserializeFields(JsonNode jnode) throws IOException {
		JsonNode smRefNode = checkJsonField(jnode, FIELD_SUBMODEL_REF);
		DefaultSubmodelReference smRef = MDTModelSerDe.readValue(smRefNode, DefaultSubmodelReference.class);
		
		String kindStr = checkJsonField(jnode, FIELD_KIND).asText();
		String argSpec = checkJsonField(jnode, FIELD_ARG_SPEC).asText();
		
		return MDTArgumentReference.newInstance(smRef, MDTArgumentKind.fromString(kindStr), argSpec);
	}
	
	/**
	 * 참조 대상 인자의 idShort path를 생성한다.
	 * <p>
	 * 참조가 활성화되는 시점에 호출되며, 대상 Submodel의 semanticId로 연산 종류(Simulation/AI)를 판별해
	 * 경로 접두어({@code <opType>Info.<Input|Output>s})를 만든 뒤, 인자 명세(argSpec)를 해석하여 대상
	 * 인자 값의 경로를 만든다. argSpec이 {@code "*"}이면 인자 목록 경로를, 숫자이면 인덱스를, 그 외에는
	 * 인자 이름으로 간주하여 해당 인자의 인덱스를 조회한다.
	 *
	 * @return 대상 인자의 idShort path.
	 * @throws ResourceNotFoundException 대상 Submodel 또는 인자를 찾을 수 없는 경우.
	 * @throws IllegalArgumentException 알 수 없는 Submodel 참조 종류이거나 지원하지 않는 semanticId인 경우.
	 */
	@Override
	protected String buildIdShortPath() {
		MDTSubmodelReference submodelRef = getSubmodelReference();
		List<MDTSubmodelDescriptor> ismDescList = getInstance().getMDTSubmodelDescriptorAll();
		MDTSubmodelDescriptor found = null;
		if ( submodelRef instanceof ByIdSubmodelReference byId ) {
			found = FStream.from(ismDescList)
							.findFirst(ism -> byId.getSubmodelId().equals(ism.getId()))
							.getOrThrow(() -> new ResourceNotFoundException("MDTArgument", "id=" + byId.getSubmodelId()));
        }
		else if ( submodelRef instanceof ByIdShortSubmodelReference byIdShort ) {
			found = FStream.from(ismDescList)
							.findFirst(ism -> byIdShort.getSubmodelIdShort().equals(ism.getIdShort()))
							.getOrThrow(() -> new ResourceNotFoundException("MDTArgument",
																		"idShort=" + byIdShort.getSubmodelIdShort()));
		}
		else {
			throw new IllegalArgumentException("Unknown submodel reference: " + submodelRef);
		}
		
		String opType = switch ( found.getSemanticId() ) {
			case Simulation.SEMANTIC_ID -> "Simulation";
			case AI.SEMANTIC_ID -> "AI";
			default -> throw new IllegalArgumentException("Unknown submodel semanticId: " + found.getSemanticId());
		};
		String argKindStr = switch ( m_kind ) {
			case INPUT -> "Input";
			case OUTPUT -> "Output";
		};
		String pathPrefix = String.format("%sInfo.%ss", opType, argKindStr);
		
		if ( m_argSpec.equals(ALL_ARGS) ) {
			return pathPrefix;
		}
		
		int argIndex = -1;
		try {
			argIndex = Integer.parseInt(m_argSpec);
		}
		catch ( NumberFormatException expected ) {
			String field = String.format("%sID", argKindStr);
			SubmodelElementList argsList = (SubmodelElementList)getSubmodelReference().get().getSubmodelElementByPath(pathPrefix);
			argIndex = SubmodelUtils.findFieldSMCByIdValue(argsList.getValue(), field, m_argSpec)
									.orElseThrow(() -> new ResourceNotFoundException("MDTArgument", "arg=" + m_argSpec))
									.index();
		}
		
		return String.format("%s[%d].%sValue", pathPrefix, argIndex, argKindStr);
	}
}
