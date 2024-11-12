package mdt.model.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.base.Preconditions;

import utils.Indexed;
import utils.KeyedValueList;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.sm.data.DefaultEquipmentParameter;
import mdt.model.sm.data.DefaultEquipmentParameterValue;
import mdt.model.sm.data.EquipmentParameter;
import mdt.model.sm.data.EquipmentParameterValue;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ParameterCollectionBase implements ParameterCollection {
	private final SubmodelService m_service;
	
	private final String m_entityTypeName;
	private AtomicReference<Map<String,Integer>> m_parameterBindings = new AtomicReference<>();
	private AtomicReference<Map<String,Integer>> m_parameterValueBindings = new AtomicReference<>();
	
	protected ParameterCollectionBase(SubmodelService service, String entityTypeName) {
		m_service = service;
		m_entityTypeName = entityTypeName;
	}

	@Override
	public Parameter getParameter(int paramIdx) throws IllegalArgumentException {
		String path = getIdShortPathOfParameter(paramIdx);
		return toParameter(m_service.getSubmodelElementByPath(path));
	}

	@Override
	public ParameterValue getParameterValue(int paramIdx) throws IllegalArgumentException {
		String path = getIdShortPathOfParameterValue(paramIdx);
		return toParameterValue(m_service.getSubmodelElementByPath(path));
	}

//	@Override
	public String getIdShortPathOfParameter(String paramId) {
		Preconditions.checkArgument(paramId != null);
		
		Integer idx = getParameterBindings().get(paramId);
		if ( idx != null ) {
			return getIdShortPathOfParameterValue(idx);
		}
		else {
			String resourceType = String.format("%sParameter", m_entityTypeName);
			throw new ResourceNotFoundException(resourceType, "param-id=" + paramId);
		}
	}

//	@Override
	public String getIdShortPathOfParameterValue(String paramId) {
		Preconditions.checkArgument(paramId != null);
		
		Integer idx = getParameterValueBindings().get(paramId);
		if ( idx != null ) {
			return String.format("DataInfo.%s.%sParameterValues[%d]", m_entityTypeName, m_entityTypeName, idx);
		}
		else {
			String resourceType = String.format("%sParameterValue", m_entityTypeName);
			throw new ResourceNotFoundException(resourceType, "param-id=" + paramId);
		}
	}
	
	@Override
	public EquipmentParameter getParameter(String paramId) {
		return toParameter(getParameterAsSubmodelElement(paramId));
	}

//	@Override
	public SubmodelElement getParameterAsSubmodelElement(String paramId) throws ResourceNotFoundException {
		String path = getIdShortPathOfParameter(paramId);
		return m_service.getSubmodelElementByPath(path);
	}

	@Override
	public EquipmentParameterValue getParameterValue(String paramId) {
		return toParameterValue(getParameterValueAsSubmodelElement(paramId));
	}

//	@Override
	public SubmodelElement getParameterValueAsSubmodelElement(String paramId) throws ResourceNotFoundException {
		String path = getIdShortPathOfParameterValue(paramId);
		return m_service.getSubmodelElementByPath(path);
	}

	@Override
	public KeyedValueList<String, Parameter> getParameterList() {
		String idShortPath = String.format("DataInfo.%s.%sParameters", m_entityTypeName, m_entityTypeName);
		SubmodelElementList parameters = (SubmodelElementList)m_service.getSubmodelElementByPath(idShortPath);
		return FStream.from(parameters.getValue())
						.map(this::toParameter)
						.collect(KeyedValueList.newInstance(Parameter::getParameterId), KeyedValueList::add);
	}

	@Override
	public KeyedValueList<String, ParameterValue> getParameterValueList() {
		String idShortPath = String.format("DataInfo.%s.%sParameterValues", m_entityTypeName, m_entityTypeName);
		SubmodelElementList values = (SubmodelElementList)m_service.getSubmodelElementByPath(idShortPath);
		return FStream.from(values.getValue())
						.map(this::toParameterValue)
						.collect(KeyedValueList.newInstance(ParameterValue::getParameterId), KeyedValueList::add);
	}

	private String getIdShortPathOfParameter(int idx) {
		Map<String,Integer> bindings = getParameterBindings();
		Preconditions.checkArgument(idx >= 0 && idx < bindings.size());
		
		return String.format("DataInfo.%s.%sParameters[%d]", m_entityTypeName, m_entityTypeName, idx);
	}

	private String getIdShortPathOfParameterValue(int idx) {
		Map<String,Integer> bindings = getParameterValueBindings();
		Preconditions.checkArgument(idx >= 0 && idx < bindings.size());
		
		return String.format("DataInfo.%s.%sParameterValues[%d]", m_entityTypeName, m_entityTypeName, idx);
	}
	
	private Map<String,Integer> getParameterBindings() {
		return m_parameterBindings.updateAndGet(prev -> FOption.getOrElseThrow(prev, this::loadParameterBindings));
	}
	private Map<String,Integer> loadParameterBindings() {
		String idShortPath = String.format("DataInfo.%s.%sParameters", m_entityTypeName, m_entityTypeName);
		SubmodelElementList parameters = (SubmodelElementList)m_service.getSubmodelElementByPath(idShortPath);
		return FStream.from(parameters.getValue())
						.map(SubmodelElement::getIdShort)
						.zipWithIndex()
						.toMap(Indexed::value, Indexed::index);
	}

	private Map<String,Integer> getParameterValueBindings() {
		return m_parameterValueBindings.updateAndGet(prev -> FOption.getOrElseThrow(prev, this::loadParameterValueBindings));
	}
	private Map<String,Integer> loadParameterValueBindings() {
		String idShortPath = String.format("DataInfo.%s.%sParameterValues", m_entityTypeName, m_entityTypeName);
		SubmodelElementList values = (SubmodelElementList)m_service.getSubmodelElementByPath(idShortPath);
		return FStream.from(values.getValue())
								.map(SubmodelElement::getIdShort)
								.zipWithIndex()
								.toMap(Indexed::value, Indexed::index);
	}
	
	private DefaultEquipmentParameter toParameter(SubmodelElement elm) {
		DefaultEquipmentParameter param = new DefaultEquipmentParameter();
		param.updateFromAasModel((SubmodelElementCollection)elm);
		return param;
	}
	private DefaultEquipmentParameterValue toParameterValue(SubmodelElement elm) {
		DefaultEquipmentParameterValue paramv = new DefaultEquipmentParameterValue();
		paramv.updateFromAasModel((SubmodelElementCollection)elm);
		return paramv;
	}
}
