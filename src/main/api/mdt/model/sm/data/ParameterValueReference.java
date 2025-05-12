package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import utils.Tuple;
import utils.Utilities;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTModelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ParameterValueReference {
	private final String m_instanceId;
	private final String m_parameterId;
	
	private volatile MDTModelService m_mdtInfo;
	private volatile SubmodelService m_svc;
	private volatile String m_idShortPath;
	
	private ParameterValueReference(String instanceId, String parameterId) {
		Preconditions.checkArgument(parameterId != null && parameterId.trim().length() > 0);
		
		m_instanceId = instanceId;
		m_parameterId = parameterId;
	}
	
	public ParameterValueReference activate(MDTInstanceManager manager) {
		return activate(manager.getInstance(m_instanceId));
	}
	
	public ParameterValueReference activate(MDTInstance instance) {
		m_mdtInfo =  MDTModelService.of(instance);
		
		m_svc = FStream.from(instance.getSubmodelServiceAllBySemanticId(Data.SEMANTIC_ID))
				        .findFirst()
						.getOrThrow(() -> new ResourceNotFoundException("DataService"));
		
		try {
			Equipment equip = m_mdtInfo.getData()
										.getDataInfo()
										.getFirstSubmodelElementEntityByClass(Equipment.class);
			int paramIdx = equip.getParameterIndex(m_parameterId);
			m_idShortPath = String.format("DataInfo.Equipment.EquipmentParameterValues[%d].ParameterValue", paramIdx);
		}
		catch ( ResourceNotFoundException e ) {
			Operation op = m_mdtInfo.getData()
									.getDataInfo()
									.getFirstSubmodelElementEntityByClass(Operation.class);
			int paramIdx = op.getParameterIndex(m_parameterId);
			m_idShortPath = String.format("DataInfo.Operation.OperationParameterValues[%d].ParameterValue", paramIdx);
		}
		
		return this;
	}
	
	public SubmodelElement get() throws ResourceNotFoundException {
		Preconditions.checkState(m_idShortPath != null, "ParameterValueReference is not activated");
		
		return m_svc.getSubmodelElementByPath(m_idShortPath);
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
		Preconditions.checkState(m_idShortPath != null, "ParameterValueReference is not activated");
		
		m_svc.updateSubmodelElementByPath(m_idShortPath, sme);
	}
	
	public void set(ElementValue value) {
		Preconditions.checkState(m_idShortPath != null, "ParameterValueReference is not activated");

		m_svc.updateSubmodelElementValueByPath(m_idShortPath, value);
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", m_instanceId, m_parameterId);
	}
	
	public static ParameterValueReference newInstance(String instanceId, String parameterId) {
		return new ParameterValueReference(instanceId, parameterId);
	}
	
	public static ParameterValueReference parseString(String refExpr) {
		Tuple<String,String> parts = Utilities.split(refExpr, '/');
		if ( parts == null ) {
			throw new IllegalArgumentException("invalid ParameterValueReference: " + refExpr);
		}
		
		return newInstance(parts._1, parts._2);
	}
}
