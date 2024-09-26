package mdt.ksx9101.model;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import mdt.ksx9101.KSX9101DataService;
import mdt.ksx9101.KSX9101Instance;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.resource.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ParameterValueReference {
	private final KSX9101Instance m_instance;
	private final KSX9101DataService m_dataService;
	private final String m_parameterId;
	private final String m_valueIdShortPath;
	
	private ParameterValueReference(KSX9101Instance instance, String parameterId) {
		Preconditions.checkArgument(parameterId != null && parameterId.trim().length() > 0);
		
		m_instance = instance;
		m_dataService = instance.getDataSubmodelService();
		m_parameterId = parameterId;
		m_valueIdShortPath = m_dataService.getParameterValueIdShortPath(m_parameterId) + ".ParameterValue";
	}
	
	public SubmodelElement get() throws ResourceNotFoundException {
		return m_dataService.getSubmodelElementByPath(m_valueIdShortPath);
	}
	
	public Property getAsProperty() {
		SubmodelElement sme = get();
		if ( sme instanceof Property prop ) {
			return prop;
		}
		else {
			throw new IllegalStateException("Target ParameterValue is not a Property: " + sme);
		}
	}
	
	public void set(SubmodelElement sme) {
		m_dataService.patchSubmodelElementByPath(m_valueIdShortPath, sme);
	}
	
	public void set(SubmodelElementValue value) {
		m_dataService.patchSubmodelElementValueByPath(m_valueIdShortPath, value);
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", m_instance.getId(), m_parameterId);
	}
	
	public static ParameterValueReference newInstance(MDTInstanceManager manager, String instanceId,
															String parameterId) {
		KSX9101Instance instance = new KSX9101Instance(manager.getInstance(instanceId));
		return new ParameterValueReference(instance, parameterId);
	}
	
	public static ParameterValueReference parseString(MDTInstanceManager manager, String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 2 ) {
			throw new IllegalArgumentException("invalid ParameterValueReference: " + refExpr);
		}
		return newInstance(manager, parts[0], parts[1]);
	}
}
