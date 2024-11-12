package mdt.aas;

import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.MDTSubmodelReference;
import mdt.model.sm.SubmodelUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DefaultSubmodelReference implements MDTSubmodelReference, MDTInstanceManagerAwareReference {
	private final String m_instanceId;
	private final String m_submodelIdShort;
	
	private volatile MDTInstance m_instance;
	private volatile String m_submodelId;
	
	private DefaultSubmodelReference(String twinId, String submodelIdShort) {
		Preconditions.checkNotNull(twinId != null);
		Preconditions.checkNotNull(submodelIdShort != null);
		
		m_instanceId = twinId;
		m_submodelIdShort = submodelIdShort;
	}
	
	@Override
	public String getInstanceId() {
		return m_instanceId;
	}
	
	@Override
	public String getSubmodelId() {
		if ( m_submodelId == null ) {
			m_submodelId = getInstance().getInstanceSubmodelDescriptorByIdShort(m_submodelIdShort).getId();
		}
		
		return m_submodelId;
	}

	@Override
	public String getSubmodelIdShort() {
		return m_submodelIdShort;
	}
	
	public MDTInstance getInstance() {
		Preconditions.checkState(m_instance != null, "SubmodelReference is not activated");
		
		return m_instance;
	}

	@Override
	public boolean isActivated() {
		return m_instanceId != null;
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		Preconditions.checkNotNull(manager);
		
		m_instance = manager.getInstance(m_instanceId);
	}

	public DefaultSubmodelReference activate(MDTInstance instance) {
		m_instance = instance;
		return this;
	}
	
	@Override
	public SubmodelService get() throws ResourceNotFoundException {
		return getInstance().getSubmodelServiceByIdShort(m_submodelIdShort);
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s/%s (%s)", m_instanceId, m_submodelIdShort, actStr);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_instanceId, m_submodelIdShort);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		DefaultSubmodelReference other = (DefaultSubmodelReference)obj;
		return Objects.equals(m_instanceId, other.m_instanceId)
				&& Objects.equals(m_submodelIdShort, other.m_submodelIdShort);
	}
	
	public static DefaultSubmodelReference newInstance(String instanceId, String submodelIdShort) {
		return new DefaultSubmodelReference(instanceId, submodelIdShort);
	}
	
	public static DefaultSubmodelReference newInstance(MDTInstance instance, String submodelIdShort) {
		return new DefaultSubmodelReference(instance.getId(), submodelIdShort).activate(instance);
	}
	
	public static DefaultSubmodelReference newInstance(MDTInstanceManager manager, Submodel submodel) {
		MDTInstance inst = manager.getInstanceBySubmodelId(submodel.getId());
		return newInstance(inst, submodel.getIdShort());
	}
	
	public static DefaultSubmodelReference newInstance(MDTInstanceManager manager, String submodelId) {
		MDTInstance inst = manager.getInstanceBySubmodelId(submodelId);
		Submodel submodel = inst.getSubmodelServiceById(submodelId).getSubmodel();
		return newInstance(inst, submodel.getIdShort());
	}
	
	public static DefaultSubmodelReference parseString(String refExpr) {
		Preconditions.checkNotNull(refExpr, "Invalid Submodel reference: {}", refExpr);
		
		String[] parts = refExpr.split("/");
		if ( parts.length != 2 ) {
			throw new IllegalArgumentException("Invalid Submodel reference: " + refExpr);
		}
		
		return newInstance(parts[0], parts[1]);
	}

	public static DefaultSubmodelReference newInstance(MDTInstanceManager manager, Reference ref)
		throws ResourceNotFoundException {
		Tuple<String,List<String>> info = SubmodelUtils.parseModelReference(ref);
		
		String smId = info._1;
		String idShortPath = FStream.from(info._2).join('.');
		
		// Submodel id 
		MDTInstance inst = manager.getInstanceBySubmodelId(smId);
		return DefaultSubmodelReference.newInstance(inst, idShortPath);
	}
}
