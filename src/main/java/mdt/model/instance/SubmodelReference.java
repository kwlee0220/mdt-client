package mdt.model.instance;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import utils.func.Tuple;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelUtils;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelReference {
	private final MDTInstance m_instance;
	private final String m_submodelIdShort;
	private volatile String m_submodelId;
	private volatile SubmodelService m_submodelService;
	
	private SubmodelReference(MDTInstance instance, String submodelIdShort) {
		m_instance = instance;
		m_submodelIdShort = submodelIdShort;
	}
	
	public MDTInstance getInstance() {
		return m_instance;
	}
	
	public String getSubmodelId() {
		if ( m_submodelId == null ) {
			m_submodelId = m_instance.getInstanceSubmodelDescriptorByIdShort(m_submodelIdShort).getId();
		}
		
		return m_submodelId;
	}
	
	public SubmodelService get() throws ResourceNotFoundException {
		if ( m_submodelService == null ) {
			m_submodelService = m_instance.getSubmodelServiceByIdShort(m_submodelIdShort);
		}
		
		return m_submodelService;
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", m_instance.getId(), m_submodelIdShort);
	}
	
	public static SubmodelReference newInstance(MDTInstanceManager manager, String instanceId,
															String submodelIdShort) {
		return new SubmodelReference(manager.getInstance(instanceId), submodelIdShort);
	}
	
	public static SubmodelReference parseString(MDTInstanceManager manager, String refExpr) {
		String[] parts = refExpr.split("/");
		return newInstance(manager, parts[0], parts[1]);
	}

	public static SubmodelReference newInstance(MDTInstanceManager manager, Reference ref)
		throws ResourceNotFoundException {
		Tuple<String,List<String>> info = SubmodelUtils.parseModelReference(ref);

		MDTInstance inst = manager.getInstanceBySubmodelId(info._1);
		String submodelIdShort = inst.getInstanceSubmodelDescriptorById(info._1).getIdShort();
		return new SubmodelReference(inst, submodelIdShort);
	}
}
