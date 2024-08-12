package mdt.model.resource.value;

import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import utils.func.Tuple;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RangeValue implements SubmodelElementValue, Supplier<Tuple<Object,Object>> {
	private String m_idShort;
	private DataTypeDefXsd m_type = DataTypeDefXsd.STRING;
	private Object m_min;
	private Object m_max;
	
	public String getIdShort() {
		return m_idShort;
	}
	
	public void setIdShort(String id) {
		m_idShort = id;
	}
	
	public DataTypeDefXsd getValueType() {
		return m_type;
	}
	
	public void setValueType(DataTypeDefXsd type) {
		m_type = type;
	}
	
	public Object getMin() {
		return m_min;
	}
	
	public void setMin(Object min) {
		m_min = min;
	}
	
	public Object getMax() {
		return m_max;
	}
	
	public void setMax(Object max) {
		m_max = max;
	}

	@Override
	public Tuple<Object,Object> get() {
		return Tuple.of(m_min, m_max);
	}

	@Override
	public Object toJsonObject() {
		return Map.of(m_idShort, Map.of("min", m_min, "max", m_max));
	}
}
