package mdt.model.sm;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.aas.DefaultSubmodelReference;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultSubmodelElementReference extends AbstractSubmodelElementReference
											implements MDTSubmodelElementReference, MDTInstanceManagerAwareReference {
	private final MDTSubmodelReference m_smRef;
	private final String m_idShortPath;
	
	private DefaultSubmodelElementReference(MDTSubmodelReference smRef, String smeIdShortPath) {
		Preconditions.checkNotNull(smRef != null);
		Preconditions.checkNotNull(smeIdShortPath != null);

		m_smRef = smRef;
		m_idShortPath = smeIdShortPath;
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
	public String getElementIdShortPath() {
		return m_idShortPath;
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_smRef.get();
	}
	
	public DefaultSubmodelElementReference child(String name) {
		return new DefaultSubmodelElementReference(m_smRef, m_idShortPath + "." + name);
	}
	
	public boolean isActivated() {
		return m_smRef.isActivated();
	}
	
	public void activate(MDTInstanceManager manager) {
		m_smRef.activate(manager);
	}
	
	public SubmodelElement read() {
		return getSubmodelService().getSubmodelElementByPath(m_idShortPath);
	}
	
	public Property getAsProperty() throws IOException {
		SubmodelElement sme = read();
		if ( sme instanceof Property prop ) {
			return prop;
		}
		else {
			throw new IllegalStateException("Target SubmodelElement is not a Property: " + sme);
		}
	}
	
	@Override
	public void write(SubmodelElement sme) throws ResourceNotFoundException {
		getSubmodelService().patchSubmodelElementByPath(m_idShortPath, sme);
	}

	@Override
	public void update(SubmodelElement sme) throws IOException {
		getSubmodelService().patchSubmodelElementByPath(m_idShortPath, sme);
	}

	@Override
	public void update(SubmodelElementValue value) throws ResourceNotFoundException {
		getSubmodelService().patchSubmodelElementValueByPath(m_idShortPath, value);
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s/%s/%s (%s)", getInstanceId(), getSubmodelIdShort(), m_idShortPath, actStr);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_smRef, m_idShortPath);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		DefaultSubmodelElementReference other = (DefaultSubmodelElementReference)obj;
		return Objects.equals(m_smRef, other.m_smRef)
				&& Objects.equals(m_idShortPath, other.m_idShortPath);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField("referenceType", SubmodelElementReferenceType.DEFAULT.name().toLowerCase());
		gen.writeStringField("twinId", m_smRef.getInstanceId());
		gen.writeStringField("submodelIdShort", m_smRef.getSubmodelIdShort());
		gen.writeStringField("idShortPath", m_idShortPath);
		gen.writeEndObject();
	}
	
	public static DefaultSubmodelElementReference newInstance(String instanceId, String submodelIdShort,
																String smeIdShortPath) {
		Preconditions.checkNotNull(instanceId != null);
		Preconditions.checkNotNull(submodelIdShort != null);
		Preconditions.checkNotNull(smeIdShortPath != null);
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(instanceId, submodelIdShort);
		return new DefaultSubmodelElementReference(smRef, smeIdShortPath);
	}
	
	public static DefaultSubmodelElementReference newInstance(MDTSubmodelReference smRef, String idShortPath) {
		return new DefaultSubmodelElementReference(smRef, idShortPath);
	}
	
	public static DefaultSubmodelElementReference parseString(String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException("invalid SubmodelElementReference: " + refExpr);
		}
		return newInstance(DefaultSubmodelReference.newInstance(parts[0], parts[1]), parts[2]);
	}

	public static DefaultSubmodelElementReference newInstance(MDTInstanceManager manager, Reference ref)
		throws ResourceNotFoundException {
		Tuple<String,List<String>> info = SubmodelUtils.parseSubmodelReference(ref);

		MDTInstance inst = manager.getInstanceBySubmodelId(info._1);
		String submodelIdShort = inst.getInstanceSubmodelDescriptorById(info._1).getIdShort();
		String idShortPath = FStream.from(info._2).join('.');
		
		return DefaultSubmodelElementReference.newInstance(DefaultSubmodelReference.newInstance(inst, submodelIdShort),
															idShortPath);
	}
	
	public static DefaultSubmodelElementReference parseJson(ObjectNode topNode) {
		String mdtId = topNode.get("twinId").asText();
		String submodelIdShort = topNode.get("submodelIdShort").asText();
		String idShortPath = topNode.get("idShortPath").asText();

		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(mdtId, submodelIdShort);
		return new DefaultSubmodelElementReference(smRef, idShortPath);
	}
}
