package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DefaultElementReference extends SubmodelBasedElementReference
											implements MDTElementReference {
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_ELEMENT_PATH = "elementPath";
	
	private final MDTSubmodelReference m_smRef;
	private final String m_path;
	
	private DefaultElementReference(MDTSubmodelReference smRef, String path) {
		Preconditions.checkNotNull(smRef != null);
		Preconditions.checkNotNull(path != null);

		m_smRef = smRef;
		m_path = path;
	}

	public MDTSubmodelReference getSubmodelReference() {
		return m_smRef;
	}
	
	@Override
	public String getInstanceId() {
		return m_smRef.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() {
		return m_smRef.getInstance();
	}

	@Override
	public String getSubmodelIdShort() {
		return m_smRef.getSubmodelIdShort();
	}
	
	public String getSubmodelId() {
		return m_smRef.getSubmodelId();
	}
	
	@Override
	public String getElementPath() {
		return m_path;
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_smRef.get();
	}
	
	public DefaultElementReference child(String name) {
		return new DefaultElementReference(m_smRef, m_path + "." + name);
	}
	
	public boolean isActivated() {
		return m_smRef.isActivated();
	}
	
	public void activate(MDTInstanceManager manager) {
		m_smRef.activate(manager);
	}
	
	public SubmodelElement read() {
		return getSubmodelService().getSubmodelElementByPath(m_path);
	}
	
	@Override
	public void write(SubmodelElement sme) throws ResourceNotFoundException {
		getSubmodelService().putSubmodelElementByPath(m_path, sme);
	}

	@Override
	public SubmodelElement update(SubmodelElementValue value) throws ResourceNotFoundException {
		SubmodelService svc = getSubmodelService();
		svc.patchSubmodelElementValueByPath(m_path, value);
		return svc.getSubmodelElementByPath(m_path);
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s/%s/%s (%s)", getInstanceId(), getSubmodelIdShort(), m_path, actStr);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_smRef, m_path);
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
				&& Objects.equals(m_path, other.m_path);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeObjectField(FIELD_SUBMODEL_REF, getSubmodelReference());
		gen.writeStringField(FIELD_ELEMENT_PATH, m_path);
		gen.writeEndObject();
	}
	
	public static DefaultElementReference newInstance(String instanceId, String submodelIdShort,
																String smeIdShortPath) {
		Preconditions.checkNotNull(instanceId != null);
		Preconditions.checkNotNull(submodelIdShort != null);
		Preconditions.checkNotNull(smeIdShortPath != null);
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(instanceId, submodelIdShort);
		return new DefaultElementReference(smRef, smeIdShortPath);
	}
	
	public static DefaultElementReference newInstance(MDTSubmodelReference smRef, String idShortPath) {
		return new DefaultElementReference(smRef, idShortPath);
	}
	
	public static DefaultElementReference parseString(String refExpr) {
		// 문자열 형태: {instanceId}/{submodelIdShort}/{elementPath}
		String[] parts = refExpr.split("/");
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException("invalid DefaultElementReference: " + refExpr);
		}
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(parts[0], parts[1]);
		return newInstance(smRef, parts[2]);
	}

	public static DefaultElementReference newInstance(MDTInstanceManager manager, Reference ref)
		throws ResourceNotFoundException {
		Tuple<String,List<String>> info = SubmodelUtils.parseSubmodelReference(ref);

		MDTInstance inst = manager.getInstanceBySubmodelId(info._1);
		String submodelIdShort = inst.getInstanceSubmodelDescriptorById(info._1).getIdShort();
		String idShortPath = FStream.from(info._2).join('.');
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(inst, submodelIdShort);
		return DefaultElementReference.newInstance(smRef, idShortPath);
	}
	
	public static DefaultElementReference parseJson(ObjectNode topNode) throws IOException {
		MDTSubmodelReference smRef = MDTModelSerDe.readValue(topNode.get(FIELD_SUBMODEL_REF),
															MDTSubmodelReference.class);
		String idShortPath = topNode.get(FIELD_ELEMENT_PATH).asText();
		
		return DefaultElementReference.newInstance(smRef, idShortPath);
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		DefaultElementReference ref = DefaultElementReference.newInstance("test", "Data",
																		"DataInfo.Equipment.EquipmentID");
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsString());
		
		String json = ref.toJsonString();
		System.out.println(json);
		System.out.println(ElementReferenceUtils.parseJsonString(json));
		
		ObjectNode node = (ObjectNode)ref.toJsonNode();
		node.put(FIELD_ELEMENT_PATH, "DataInfo.Equipment.EquipmentName");
		ref = (DefaultElementReference)ElementReferenceUtils.parseJsonNode(node);
		ref.activate(manager);
		System.out.println(ref.toString() + ": " + ref.readAsString());
	}
}
