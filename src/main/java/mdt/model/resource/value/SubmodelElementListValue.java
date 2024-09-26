package mdt.model.resource.value;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementListValue implements SubmodelElementValue, Supplier<List<SubmodelElementValue>> {
	private final String m_idShort;
	private final List<SubmodelElementValue> m_elementValues;
	
	public SubmodelElementListValue(String id, List<SubmodelElementValue> values) {
		m_idShort = id;
		m_elementValues = values;
	}
	
	public String getIdShort() {
		return m_idShort;
	}
	
	@Override
	public List<SubmodelElementValue> get() {
		return m_elementValues;
	}

	@Override
	public Object toJsonObject() {
		List<Object> elmJsonObjs = FStream.from(this.m_elementValues)
											.map(SubmodelElementValue::toJsonObject)
											.toList();
		return Map.of(m_idShort, elmJsonObjs);
	}
}
