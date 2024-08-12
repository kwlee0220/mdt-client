package mdt.model.resource.value;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementListValue implements SubmodelElementValue, Supplier<List<SubmodelElementValue>> {
	private String m_idShort;
	private List<SubmodelElementValue> m_elementValues = Lists.newArrayList();
	
	public String getIdShort() {
		return m_idShort;
	}
	
	public void setIdShort(String id) {
		m_idShort = id;
	}
	
	@Override
	public List<SubmodelElementValue> get() {
		return m_elementValues;
	}
	
	public void addElementValue(SubmodelElementValue value) {
		this.m_elementValues.add(value);
	}

	@Override
	public Object toJsonObject() {
		List<Object> elmJsonObjs = FStream.from(this.m_elementValues)
											.map(SubmodelElementValue::toJsonObject)
											.toList();
		return Map.of(m_idShort, elmJsonObjs);
	}
}
