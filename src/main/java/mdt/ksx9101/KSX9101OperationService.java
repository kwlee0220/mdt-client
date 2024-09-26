package mdt.ksx9101;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.Indexed;
import utils.stream.FStream;

import mdt.ksx9101.model.Operation;
import mdt.ksx9101.model.OperationParameter;
import mdt.ksx9101.model.OperationParameterValue;
import mdt.ksx9101.model.impl.DefaultOperation;
import mdt.ksx9101.model.impl.DefaultOperationParameter;
import mdt.ksx9101.model.impl.DefaultOperationParameterValue;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;

import lombok.experimental.Delegate;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class KSX9101OperationService implements KSX9101DataService {
	@Delegate private final SubmodelService m_service;
	private Map<String,Integer> m_parameterBindings;
	private Map<String,Integer> m_parameterValueBindings;
	
	public KSX9101OperationService(SubmodelService service) {
		m_service = service;
	}
	
	public Operation getOperation() {
		SubmodelElement sme = m_service.getSubmodelElementByPath("DataInfo.Operation");
		DefaultOperation eq = new DefaultOperation();
		eq.fromAasModel(sme);
		
		return eq;
	}

	@Override
	public String getParameterIdShortPath(String paramId) {
		Integer idx = loadParameterBindings().get(paramId);
		if ( idx != null ) {
			return String.format("DataInfo.Operation.OperationParameters[%d]", idx);
		}
		else {
			throw new ResourceNotFoundException("OperationParameter", "id=" + paramId);
		}
	}

	@Override
	public String getParameterValueIdShortPath(String paramId) {
		Integer idx = loadParameterBindings().get(paramId);
		if ( idx != null ) {
			return String.format("DataInfo.Operation.OperationParameterValues[%d]", idx);
		}
		else {
			throw new ResourceNotFoundException("OperationParameterValues", "id=" + paramId);
		}
	}
	
	@Override
	public OperationParameter getParameter(String id) {
		Integer idx = loadParameterBindings().get(id);
		if ( idx != null ) {
			String idShortPath = String.format("DataInfo.Operation.OperationParameters[%d]", idx);
			SubmodelElement sme = m_service.getSubmodelElementByPath(idShortPath);
			
			DefaultOperationParameter param = new DefaultOperationParameter();
			param.fromAasModel(sme);
			return param;
		}
		else {
			throw new ResourceNotFoundException("OperationParameter", "id=" + id);
		}
	}

	@Override
	public OperationParameterValue getParameterValue(String id) {
		Integer idx = loadParameterValueBindings().get(id);
		if ( idx != null ) {
			String idShortPath = String.format("DataInfo.Operation.OperationParameterValues[%d]", idx);
			SubmodelElement sme = m_service.getSubmodelElementByPath(idShortPath);
			
			DefaultOperationParameterValue value = new DefaultOperationParameterValue();
			value.fromAasModel(sme);
			return value;
		}
		else {
			throw new ResourceNotFoundException("OperationParameterValue", "id=" + id);
		}
	}
	
	private Map<String,Integer> loadParameterBindings() {
		SubmodelElementList parameters = (SubmodelElementList)m_service
													.getSubmodelElementByPath("DataInfo.Operation.OperationParameters");
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
													.getSubmodelElementByPath("DataInfo.Operation.OperationParameterValues");
		if ( m_parameterValueBindings == null ) {
			m_parameterValueBindings = FStream.from(values.getValue())
												.map(SubmodelElement::getIdShort)
												.zipWithIndex()
												.toMap(Indexed::value, Indexed::index);
		}
		return m_parameterValueBindings;
	}
}
