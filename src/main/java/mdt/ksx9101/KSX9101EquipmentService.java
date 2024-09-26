package mdt.ksx9101;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.Indexed;
import utils.stream.FStream;

import mdt.ksx9101.model.Equipment;
import mdt.ksx9101.model.EquipmentParameter;
import mdt.ksx9101.model.EquipmentParameterValue;
import mdt.ksx9101.model.impl.DefaultEquipment;
import mdt.ksx9101.model.impl.DefaultEquipmentParameter;
import mdt.ksx9101.model.impl.DefaultEquipmentParameterValue;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;

import lombok.experimental.Delegate;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class KSX9101EquipmentService implements KSX9101DataService {
	@Delegate private final SubmodelService m_service;
	private Map<String,Integer> m_parameterBindings;
	private Map<String,Integer> m_parameterValueBindings;
	
	public KSX9101EquipmentService(SubmodelService service) {
		m_service = service;
	}
	
	public Equipment getEquipment() {
		SubmodelElement sme = m_service.getSubmodelElementByPath("DataInfo.Equipment");
		DefaultEquipment eq = new DefaultEquipment();
		eq.fromAasModel(sme);
		
		return eq;
	}

	@Override
	public String getParameterIdShortPath(String paramId) {
		Integer idx = loadParameterBindings().get(paramId);
		if ( idx != null ) {
			return String.format("DataInfo.Equipment.EquipmentParameters[%d]", idx);
		}
		else {
			throw new ResourceNotFoundException("EquipmentParameter", "id=" + paramId);
		}
	}

	@Override
	public String getParameterValueIdShortPath(String paramId) {
		Integer idx = loadParameterBindings().get(paramId);
		if ( idx != null ) {
			return String.format("DataInfo.Equipment.EquipmentParameterValues[%d]", idx);
		}
		else {
			throw new ResourceNotFoundException("EquipmentParameterValues", "id=" + paramId);
		}
	}
	
	@Override
	public EquipmentParameter getParameter(String paramId) {
		Integer idx = loadParameterBindings().get(paramId);
		if ( idx != null ) {
			String idShortPath = String.format("DataInfo.Equipment.EquipmentParameters[%d]", idx);
			SubmodelElement sme = m_service.getSubmodelElementByPath(idShortPath);
			
			DefaultEquipmentParameter param = new DefaultEquipmentParameter();
			param.fromAasModel(sme);
			return param;
		}
		else {
			throw new ResourceNotFoundException("EquipmentParameter", "id=" + paramId);
		}
	}

	@Override
	public EquipmentParameterValue getParameterValue(String paramId) {
		Integer idx = loadParameterValueBindings().get(paramId);
		if ( idx != null ) {
			String idShortPath = String.format("DataInfo.Equipment.EquipmentParameterValues[%d]", idx);
			SubmodelElement sme = m_service.getSubmodelElementByPath(idShortPath);
			
			DefaultEquipmentParameterValue value = new DefaultEquipmentParameterValue();
			value.fromAasModel(sme);
			return value;
		}
		else {
			throw new ResourceNotFoundException("EquipmentParameterValue", "id=" + paramId);
		}
	}
	
	private Map<String,Integer> loadParameterBindings() {
		SubmodelElementList parameters = (SubmodelElementList)m_service
													.getSubmodelElementByPath("DataInfo.Equipment.EquipmentParameters");
		if ( m_parameterBindings == null ) {
			m_parameterBindings = FStream.from(parameters.getValue())
											.map(SubmodelElement::getIdShort)
											.zipWithIndex()
											.toMap(Indexed::value, Indexed::index);
		}
		return m_parameterBindings;
	}
	
	private Map<String,Integer> loadParameterValueBindings() {
		SubmodelElementList values = (SubmodelElementList)m_service
													.getSubmodelElementByPath("DataInfo.Equipment.EquipmentParameterValues");
		if ( m_parameterValueBindings == null ) {
			m_parameterValueBindings = FStream.from(values.getValue())
												.map(SubmodelElement::getIdShort)
												.zipWithIndex()
												.toMap(Indexed::value, Indexed::index);
		}
		return m_parameterValueBindings;
	}
}
