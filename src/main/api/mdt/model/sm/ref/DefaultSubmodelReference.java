package mdt.model.sm.ref;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;


/**
 * {@link MDTSubmodelReference}의 기본 구현을 제공하는 추상 클래스이다.
 * <p>
 * 대상 Submodel의 지정 방식에 따라 두 가지 구현을 제공한다.
 * <ul>
 *   <li>{@link ByIdSubmodelReference}: Submodel 식별자로 지정.
 *       표현식: {@code submodel:id=<submodelId>}, Json: {@code {"submodelId": ...}}</li>
 *   <li>{@link ByIdShortSubmodelReference}: MDTInstance 식별자와 Submodel의 idShort로 지정.
 *       표현식: {@code <instanceId>:<submodelIdShort>},
 *       Json: {@code {"instanceId": ..., "submodelIdShort": ...}}</li>
 * </ul>
 * 두 구현 모두 {@link #activate(MDTInstanceManager)}로 활성화한 후에 대상 Submodel에
 * 접근할 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class DefaultSubmodelReference implements MDTSubmodelReference {
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_SUBMODEL_ID = "submodelId";
	private static final String FIELD_SUBMODEL_ID_SHORT = "submodelIdShort";
	
	// 활성화 여부를 결정한다. 재활성화가 실패하면 null이 된다(2026-11-01 이전에는 이전 인스턴스가 남아
	// 활성 상태로 보였다).
	protected volatile MDTInstance m_instance;

	@Override
	public boolean isActivated() {
		return m_instance != null;
	}

	@Override
	public MDTInstance getInstance() {
		Preconditions.checkState(m_instance != null, "SubmodelReference is not activated");
		
		return m_instance;
	}
	
	/**
	 * Submodel 식별자로 대상을 지정하는 {@link DefaultSubmodelReference} 구현이다.
	 * <p>
	 * 소속 MDTInstance는 활성화 시점에 Submodel 식별자를 통해 검색된다.
	 */
	public static final class ByIdSubmodelReference extends DefaultSubmodelReference {
		private final String m_submodelId;

		private ByIdSubmodelReference(String submodelId) {
			Preconditions.checkNotNullArgument(submodelId, "submodelId is null");
			Preconditions.checkArgument(!submodelId.isBlank(), "submodelId is blank");

			m_submodelId = submodelId;
		}

		/**
		 * 참조가 가리키는 Submodel의 식별자를 반환한다.
		 * <p>
		 * 생성 시점에 고정되므로 활성화 여부와 무관하게 호출 가능하다.
		 *
		 * @return	Submodel 식별자.
		 */
		public String getSubmodelId() {
			return m_submodelId;
		}
		
		@Override
		public String getInstanceId() {
			return getInstance().getId();
		}
		
		@Override
		public void activate(MDTInstanceManager manager) throws ResourceNotFoundException {
			Preconditions.checkNotNullArgument(manager, "manager is null");
			
			// 실패하면 이전 인스턴스를 남기지 않고 비활성 상태가 된다.
			m_instance = null;
			m_instance = manager.getInstanceBySubmodelId(m_submodelId);
		}
		
		@Override
		public SubmodelService get() throws ResourceNotFoundException {
			return getInstance().getSubmodelServiceById(m_submodelId)
								.getOrThrow(() -> ResourceNotFoundException.ofId("Submodel", m_submodelId));
		}

		@Override
		public void serialize(JsonGenerator gen) throws IOException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_SUBMODEL_ID, m_submodelId);
			gen.writeEndObject();
		}

		@Override
		public String toStringExpr() {
			return String.format("submodel:id=%s", m_submodelId);
		}
		
		@Override
		public String toString() {
			String actStr = isActivated() ? "activated" : "deactivated";
			return String.format("%s (%s)", toStringExpr(), actStr);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(m_submodelId);
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || obj.getClass() != getClass() ) {
				return false;
			}
			
			ByIdSubmodelReference other = (ByIdSubmodelReference)obj;
			return Objects.equals(m_submodelId, other.m_submodelId);
		}
	}
	
	/**
	 * MDTInstance 식별자와 Submodel idShort로 대상을 지정하는 {@link DefaultSubmodelReference}
	 * 구현이다.
	 */
	public static final class ByIdShortSubmodelReference extends DefaultSubmodelReference {
		private final String m_instanceId;
		private final String m_submodelIdShort;
		
		private ByIdShortSubmodelReference(String instanceId, String submodelIdShort) {
			Preconditions.checkNotNullArgument(instanceId, "instanceId is null");
			Preconditions.checkNotNullArgument(submodelIdShort, "submodelIdShort is null");
			Preconditions.checkArgument(!instanceId.isBlank(), "instanceId is blank");
			Preconditions.checkArgument(!submodelIdShort.isBlank(), "submodelIdShort is blank");
			
			m_instanceId = instanceId;
			m_submodelIdShort = submodelIdShort;
		}
		
		@Override
		public String getInstanceId() {
			return m_instanceId;
		}

		/**
		 * 참조가 가리키는 Submodel의 식별자를 반환한다.
		 * <p>
		 * 활성화된 MDTInstance의 Submodel 기술자 목록에서 idShort로 검색하므로,
		 * 활성화 전에 호출하면 {@link IllegalStateException}이 발생한다.
		 *
		 * @return	Submodel 식별자.
		 * @throws IllegalStateException		참조가 활성화되지 않은 경우.
		 * @throws ResourceNotFoundException	해당 idShort의 Submodel이 존재하지 않는 경우.
		 */
		@Override
		public String getSubmodelId() {
			return FStream.from(getInstance().getMDTSubmodelDescriptorAll())
							.findFirst(ism -> m_submodelIdShort.equals(ism.getIdShort()))
							.map(ism -> ism.getId())
							.getOrThrow(() -> ResourceNotFoundException.ofIdShort("Submodel", m_submodelIdShort));
		}

		/**
		 * 참조가 가리키는 Submodel의 idShort를 반환한다.
		 *
		 * @return	Submodel idShort.
		 */
		public String getSubmodelIdShort() {
			return m_submodelIdShort;
		}
		
		@Override
		public void activate(MDTInstanceManager manager) throws ResourceNotFoundException {
			Preconditions.checkNotNullArgument(manager, "manager is null");
			
			// 실패하면 이전 인스턴스를 남기지 않고 비활성 상태가 된다.
			m_instance = null;
			m_instance = manager.getInstance(m_instanceId);
		}
		
		@Override
		public SubmodelService get() throws ResourceNotFoundException {
			return getInstance().getSubmodelServiceByIdShort(m_submodelIdShort)
								.getOrThrow(() -> ResourceNotFoundException.ofIdShort("Submodel", m_submodelIdShort));
		}

		@Override
		public void serialize(JsonGenerator gen) throws IOException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_INSTANCE_ID, getInstanceId());
			gen.writeStringField(FIELD_SUBMODEL_ID_SHORT, m_submodelIdShort);
			gen.writeEndObject();
		}

		@Override
		public String toStringExpr() {
			return String.format("%s:%s", getInstanceId(), m_submodelIdShort);
		}
		
		@Override
		public String toString() {
			String actStr = isActivated() ? "activated" : "deactivated";
			return String.format("%s (%s)", toStringExpr(), actStr);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(getInstanceId(), m_submodelIdShort);
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null || obj.getClass() != getClass() ) {
				return false;
			}
			
			ByIdShortSubmodelReference other = (ByIdShortSubmodelReference)obj;
			return Objects.equals(getInstanceId(), other.getInstanceId())
					&& Objects.equals(m_submodelIdShort, other.m_submodelIdShort);
		}
	}
	
	/**
	 * Json 노드로부터 {@link DefaultSubmodelReference}를 복원한다.
	 * <p>
	 * {@code submodelId} 필드가 있으면 {@link ByIdSubmodelReference}로, 그렇지 않으면
	 * {@code instanceId}/{@code submodelIdShort} 필드로 {@link ByIdShortSubmodelReference}를 복원한다.
	 * 식별자 필드는 비어 있지 않은 JSON 문자열이어야 한다(생성자와 같은 규칙). 2026-11-01 이전에는 숫자·불리언도
	 * 문자열로 받았고({@code true:42:x} 같은 참조가 만들어졌다), 오류 메시지에 노드 전체를 넣은 일반
	 * {@link IOException}을 던졌다.
	 *
	 * @param jnode	Submodel 참조 정보를 담은 Json 객체 노드.
	 * @return 복원된 {@link DefaultSubmodelReference} 객체.
	 * @throws IllegalArgumentException	{@code jnode}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	객체가 아니거나, 필수 필드가 없거나, 식별자가 JSON 문자열이 아니거나
	 * 											빈 문자열인 경우. 메시지에는 필드 이름만 들어간다.
	 */
	public static DefaultSubmodelReference parseJson(JsonNode jnode) throws IOException {
		Preconditions.checkNotNullArgument(jnode, "jnode is null");
		if ( !jnode.isObject() ) {
			throw new JacksonDeserializationException(String.format("invalid submodel reference: object expected, but %s",
																	jnode.getNodeType().name().toLowerCase()));
		}

		String smId = JacksonUtils.getTextFieldOrNull(jnode, FIELD_SUBMODEL_ID);
		if ( smId != null ) {
			return ofId(requireNonBlank(smId, FIELD_SUBMODEL_ID));
		}
		String smIdShort = requireNonBlank(JacksonUtils.getTextField(jnode, FIELD_SUBMODEL_ID_SHORT),
											FIELD_SUBMODEL_ID_SHORT);
		String instId = requireNonBlank(JacksonUtils.getTextField(jnode, FIELD_INSTANCE_ID), FIELD_INSTANCE_ID);
		return ofIdShort(instId, smIdShort);
	}

	private static String requireNonBlank(String value, String fieldName) throws JacksonDeserializationException {
		if ( value.isBlank() ) {
			throw new JacksonDeserializationException(String.format("invalid field '%s': blank", fieldName));
		}
		return value;
	}
	
	/**
	 * Submodel 식별자로 {@link ByIdSubmodelReference}를 생성한다.
	 *
	 * @param submodelId	대상 Submodel의 식별자.
	 * @return 생성된 참조 객체.
	 */
	public static ByIdSubmodelReference ofId(String submodelId) {
		return new ByIdSubmodelReference(submodelId);
	}

	/**
	 * MDTInstance 식별자와 Submodel idShort로 {@link ByIdShortSubmodelReference}를 생성한다.
	 *
	 * @param instanceId		대상 Submodel이 소속된 MDTInstance의 식별자.
	 * @param submodelIdShort	대상 Submodel의 idShort.
	 * @return 생성된 참조 객체.
	 */
	public static ByIdShortSubmodelReference ofIdShort(String instanceId, String submodelIdShort) {
		Preconditions.checkNotNullArgument(instanceId, "instanceId is null");
		Preconditions.checkNotNullArgument(submodelIdShort, "submodelIdShort is null");

		return new ByIdShortSubmodelReference(instanceId, submodelIdShort);
	}

	/**
	 * MdtExpr 표현식 문자열로부터 {@link DefaultSubmodelReference}를 파싱한다.
	 * <p>
	 * 형식: {@code <instanceId>:[idShort=]<submodelIdShort>} 또는 {@code submodel:id=<submodelId>}.
	 *
	 * @param refExpr	Submodel 참조 표현식.
	 * @return 파싱된 {@link DefaultSubmodelReference} 객체.
	 * @throws IllegalArgumentException	{@code refExpr}이 {@code null}인 경우.
	 * @throws mdt.model.expr.MDTParserException	표현식의 문법이 틀렸거나 뒤에 남는 입력이 있는 경우.
	 */
	public static DefaultSubmodelReference parseStringExpr(String refExpr) {
		Preconditions.checkArgument(refExpr != null, "Submodel reference is null");

		return MDTExpressionParser.parseSubmodelReference(refExpr).evaluate();
	}
}
