package mdt.ksx9101.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import utils.KeyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ParametricEntity {
	public List<Parameter> getParameters();
	public void setParameters(List<Parameter> parameters);
	
	public List<ParameterValue> getParameterValues();
	public void setParameterValues(List<ParameterValue> parameterValues);
	
	default public Optional<Parameter> getParameter(String parameterId) {
		return getParameters()
					.stream()
					.filter(p -> p.getParameterId().equals(parameterId))
					.findFirst();
	}
	default public Optional<ParameterValue> getParameterValue(String parameterId) {
		return getParameterValues()
					.stream()
					.filter(p -> p.getParameterId().equals(parameterId))
					.findFirst();
	}
	default public List<KeyValue<Parameter,ParameterValue>> getParameterValuePairs() {
		Map<String,ParameterValue> valueMap = getParameterValues()
													.stream()
													.collect(Collectors.toMap(ParameterValue::getParameterId,
																				Function.identity()));
		return getParameters()
					.stream()
					.map(p -> KeyValue.of(p, valueMap.get(p.getParameterId())))
					.toList();
	}
}
