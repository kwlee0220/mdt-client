package mdt.model.sm.ref;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.References;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DefaultElementReference extends SubmodelBasedElementReference implements MDTElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:element";
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_ELEMENT_PATH = "elementPath";
	
	private final MDTSubmodelReference m_smRef;
	private final String m_elementPath;
	
	private DefaultElementReference(MDTSubmodelReference smRef, String path) {
		Preconditions.checkNotNull(smRef != null);
		Preconditions.checkNotNull(path != null);

		m_smRef = smRef;
		m_elementPath = path;
	}
	
	@Override
	public String getInstanceId() {
		return m_smRef.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() {
		return m_smRef.getInstance();
	}

	public MDTSubmodelReference getSubmodelReference() {
		return m_smRef;
	}

	@Override
	public String getIdShortPathString() {
		return m_elementPath;
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_smRef.get();
	}
	
	public DefaultElementReference child(String name) {
		return new DefaultElementReference(m_smRef, m_elementPath + "." + name);
	}
	
	public boolean isActivated() {
		return m_smRef.isActivated();
	}

	@Override
	public String toStringExpr() {
		return m_smRef.toStringExpr() + ":" + m_elementPath;
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_smRef.activate(manager);
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_smRef, m_elementPath);
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
		return Objects.equals(m_smRef, other.m_smRef)
				&& Objects.equals(m_elementPath, other.m_elementPath);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeObjectField(FIELD_SUBMODEL_REF, getSubmodelReference());
		gen.writeStringField(FIELD_ELEMENT_PATH, m_elementPath);
	}
	
	public static DefaultElementReference deserializeFields(JsonNode jnode) throws IOException {
		MDTSubmodelReference smRef = MDTModelSerDe.readValue(jnode.get(FIELD_SUBMODEL_REF),
															MDTSubmodelReference.class);
		if ( smRef == null ) {
			String json = MDTModelSerDe.toJsonString(jnode);
			throw new IllegalArgumentException("Failed to parse MDTSubmodelReference: ref=" + json);
		}
		
		String idShortPath = jnode.get(FIELD_ELEMENT_PATH).asText();
		return DefaultElementReference.newInstance(smRef, idShortPath);
	}
	
	public static DefaultElementReference newInstance(MDTSubmodelReference smRef, String elementPath) {
		return new DefaultElementReference(smRef, elementPath);
	}

	public static DefaultElementReference newInstance(Reference ref) throws ResourceNotFoundException {
		return References.toSubmodelElementReference(ref);
	}
	
	public static DefaultElementReference parseJson(ObjectNode topNode) throws IOException {
		MDTSubmodelReference smRef = MDTModelSerDe.readValue(topNode.get(FIELD_SUBMODEL_REF),
															MDTSubmodelReference.class);
		if ( smRef == null ) {
			String json = MDTModelSerDe.toJsonString(topNode);
			throw new IllegalArgumentException("Failed to parse MDTSubmodelReference: ref=" + json);
		}
		
		String idShortPath = topNode.get(FIELD_ELEMENT_PATH).asText();
		return DefaultElementReference.newInstance(smRef, idShortPath);
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Data");
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef, "DataInfo.Equipment.EquipmentID");
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsString());
		
		String json = ref.toJsonString();
		System.out.println(json);
		System.out.println(ElementReferences.parseJsonString(json));
		
		ObjectNode node = (ObjectNode)ref.toJsonNode();
		node.put(FIELD_ELEMENT_PATH, "DataInfo.Equipment.EquipmentName");
		ref = (DefaultElementReference)ElementReferences.parseJsonNode(node);
		ref.activate(manager);
		System.out.println(ref.toString() + ": " + ref.readAsString());
	}
}
