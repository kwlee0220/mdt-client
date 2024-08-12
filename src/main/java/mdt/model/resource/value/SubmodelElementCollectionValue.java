package mdt.model.resource.value;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementCollectionValue implements SubmodelElementValue,
															Supplier< Map<String,SubmodelElementValue>> {
	private String m_idShort;
	private Map<String,SubmodelElementValue> m_elementValues;

	public SubmodelElementCollectionValue() {
		this.m_elementValues = Maps.newHashMap();
	}
	
	public String getIdShort() {
		return m_idShort;
	}
	
	public void setIdShort(String id) {
		m_idShort = id;
	}
	
	public Map<String,SubmodelElementValue> get() {
		return m_elementValues;
	}
	
	public void addElementValue(String key, SubmodelElementValue value) {
		this.m_elementValues.put(key, value);
	}

	@Override
	public Object toJsonObject() {
		Map<String,Object> elmJsonObjs = FStream.from(this.m_elementValues)
												.mapValue((k,v) -> v.toJsonObject())
												.toMap();
		return Map.of(m_idShort, elmJsonObjs);
	}
}
