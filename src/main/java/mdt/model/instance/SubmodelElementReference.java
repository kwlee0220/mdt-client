package mdt.model.instance;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelUtils;
import mdt.model.resource.value.SubmodelElementValue;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementReference {
	private final MDTInstance m_instance;
	private final String m_submodelIdShort;
	private final String m_idShortPath;
	private volatile String m_submodelId;
	private volatile SubmodelService m_submodelService;
	
	private SubmodelElementReference(MDTInstance instance, String submodelIdShort, String idShortPath) {
		Preconditions.checkArgument(idShortPath != null && idShortPath.trim().length() > 0);
		
		m_instance = instance;
		m_submodelIdShort = submodelIdShort;
		m_idShortPath = idShortPath;
	}
	
	public MDTInstance getInstance() {
		return m_instance;
	}
	
	public SubmodelService getSubmodelService() {
		if ( m_submodelService == null ) {
			m_submodelService = m_instance.getSubmodelServiceByIdShort(m_submodelIdShort);
		}
		
		return m_submodelService;
	}
	
	public String getSubmodelId() {
		if ( m_submodelId == null ) {
			m_submodelId = m_instance.getInstanceSubmodelDescriptorByIdShort(m_submodelIdShort).getId();
		}
		
		return m_submodelId;
	}
	
	public String getIdShortPath() {
		return m_idShortPath;
	}
	
	public SubmodelElement get() throws ResourceNotFoundException {
		SubmodelElement sme = getSubmodelService().getSubmodelElementByPath(m_idShortPath);
		if ( sme == null ) {
			throw new ResourceNotFoundException("SubmodelElement", "idShortPath=" + m_idShortPath);
		}
		
		return sme;
	}
	
	public Property getAsProperty() {
		SubmodelElement sme = get();
		if ( sme instanceof Property prop ) {
			return prop;
		}
		else {
			throw new IllegalStateException("Target SubmodelElement is not a Property: " + sme);
		}
	}
	
	public Operation getAsOperation() {
		SubmodelElement sme = get();
		if ( sme instanceof Operation op ) {
			return op;
		}
		else {
			throw new IllegalStateException("Target SubmodelElement is not an Operation: " + sme);
		}
	}
	
	public void set(SubmodelElement sme) {
		getSubmodelService().patchSubmodelElementByPath(m_idShortPath, sme);
	}
	
	public void set(SubmodelElementValue value) {
		getSubmodelService().patchSubmodelElementValueByPath(m_idShortPath, value);
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s/%s", m_instance.getId(), m_submodelIdShort, m_idShortPath);
	}
	
	public static SubmodelElementReference newInstance(MDTInstanceManager manager, String instanceId,
															String submodelIdShort, String idShortPath) {
		return new SubmodelElementReference(manager.getInstance(instanceId), submodelIdShort, idShortPath);
	}
	
	public static SubmodelElementReference parseString(MDTInstanceManager manager, String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException("invalid SubmodelElementReference: " + refExpr);
		}
		return newInstance(manager, parts[0], parts[1], parts[2]);
	}

	public static SubmodelElementReference newInstance(MDTInstanceManager manager, Reference ref)
		throws ResourceNotFoundException {
		Tuple<String,List<String>> info = SubmodelUtils.parseModelReference(ref);

		MDTInstance inst = manager.getInstanceBySubmodelId(info._1);
		String submodelIdShort = inst.getInstanceSubmodelDescriptorById(info._1).getIdShort();
		String idShortPath = FStream.from(info._2).join('.');
		return new SubmodelElementReference(inst, submodelIdShort, idShortPath);
	}
}
