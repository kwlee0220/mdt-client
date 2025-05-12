package mdt.model.sm.ref;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;


/**
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
	
	public static final class ByIdSubmodelReference extends DefaultSubmodelReference {
		private final String m_submodelId;
		
		private ByIdSubmodelReference(String submodelId) {
			Preconditions.checkArgument(submodelId != null, "submodelId is null");
			
			m_submodelId = submodelId;
		}
		
		public String getSubmodelId() {
			return m_submodelId;
		}
		
		@Override
		public String getInstanceId() {
			return getInstance().getId();
		}
		
		@Override
		public void activate(MDTInstanceManager manager) throws ResourceNotFoundException {
			Preconditions.checkArgument(manager != null);
			
			m_instance = manager.getInstanceBySubmodelId(m_submodelId);
		}
		
		@Override
		public SubmodelService get() throws ResourceNotFoundException {
			return getInstance().getSubmodelServiceById(m_submodelId);
		}

		@Override
		public void serialize(JsonGenerator gen) throws IOException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_SUBMODEL_ID, m_submodelId);
			gen.writeEndObject();
		}

		@Override
		public String toStringExpr() {
			return String.format("submodel['%s']", getInstanceId(), m_submodelId);
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
	
	public static final class ByIdShortSubmodelReference extends DefaultSubmodelReference {
		private final String m_instanceId;
		private final String m_submodelIdShort;
		
		private ByIdShortSubmodelReference(String instanceId, String submodelIdShort) {
			Preconditions.checkArgument(instanceId != null, "instanceId is null");
			Preconditions.checkArgument(submodelIdShort != null);
			
			m_instanceId = instanceId;
			m_submodelIdShort = submodelIdShort;
		}
		
		@Override
		public String getInstanceId() {
			return m_instanceId;
		}
		
		public String getSubmodelIdShort() {
			return m_submodelIdShort;
		}
		
		@Override
		public void activate(MDTInstanceManager manager) throws ResourceNotFoundException {
			Preconditions.checkArgument(manager != null);
			
			m_instance = manager.getInstance(m_instanceId);
		}
		
		@Override
		public SubmodelService get() throws ResourceNotFoundException {
			return getInstance().getSubmodelServiceByIdShort(m_submodelIdShort);
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
	
	public static DefaultSubmodelReference parseJson(JsonNode jnode) {
		JsonNode idNode = jnode.get(FIELD_SUBMODEL_ID);
		if ( idNode != null && !idNode.isNull() ) {
			return ofId(idNode.asText());
		}
		JsonNode idShortNode = jnode.get(FIELD_SUBMODEL_ID_SHORT);
		if ( idShortNode != null && !idShortNode.isNull() ) {
			String instanceId = jnode.get(FIELD_INSTANCE_ID).asText();
			return ofIdShort(instanceId, idShortNode.asText());
		}
		
		throw new IllegalArgumentException(String.format("Invalid SubmodelReference: %s", jnode.toString()));
	}
	
	public static ByIdSubmodelReference ofId(String submodelId) {
		return new ByIdSubmodelReference(submodelId);
	}
	
	public static ByIdShortSubmodelReference ofIdShort(String instanceId, String submodelIdShort) {
		return new ByIdShortSubmodelReference(instanceId, submodelIdShort);
	}
	
	public static DefaultSubmodelReference parseStringExpr(String refExpr) {
		Preconditions.checkArgument(refExpr != null, "Submodel reference is null");
		
		return MDTExprParser.parseSubmodelReference(refExpr).evaluate();
	}
}
