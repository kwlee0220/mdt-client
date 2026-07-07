package mdt.model.sm.ref;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
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
	
	protected volatile MDTInstance m_instance;	// 활성화 여부를 결정

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
	 * {@code submodelId} 필드가 있으면 {@link ByIdSubmodelReference}로, 그렇지 않고
	 * {@code instanceId}/{@code submodelIdShort} 필드가 있으면
	 * {@link ByIdShortSubmodelReference}로 복원한다.
	 *
	 * @param jnode	Submodel 참조 정보를 담은 Json 노드.
	 * @return 복원된 {@link DefaultSubmodelReference} 객체.
	 * @throws IOException	필수 필드가 존재하지 않는 경우.
	 */
	public static DefaultSubmodelReference parseJson(JsonNode jnode) throws IOException {
		String smId = JacksonUtils.getStringFieldOrNull(jnode, FIELD_SUBMODEL_ID);
		if ( smId != null ) {
			return ofId(smId);
		}
		
		String smIdShort = JacksonUtils.getStringFieldOrNull(jnode, FIELD_SUBMODEL_ID_SHORT);
		if ( smIdShort == null ) {
			throw new IOException(String.format("Invalid SubmodelReference: %s", jnode.toString()));
		}
		
		String instId = JacksonUtils.getStringFieldOrNull(jnode, FIELD_INSTANCE_ID);
		if ( instId == null ) {
			throw new IOException(String.format("Invalid SubmodelReference: %s", jnode.toString()));
		}
		return ofIdShort(instId, smIdShort);
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
	 * @throws IllegalArgumentException	표현식이 올바르지 않은 경우.
	 */
	public static DefaultSubmodelReference parseStringExpr(String refExpr) {
		Preconditions.checkArgument(refExpr != null, "Submodel reference is null");

		return MDTExpressionParser.parseSubmodelReference(refExpr).evaluate();
	}
}
