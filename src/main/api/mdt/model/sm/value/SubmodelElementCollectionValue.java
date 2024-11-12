package mdt.model.sm.value;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Maps;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementCollectionValue implements SubmodelElementValue,
															Supplier<Map<String,SubmodelElementValue>> {
	private Map<String, SubmodelElementValue> m_elements;
	
	public SubmodelElementCollectionValue() {
		m_elements = Maps.newHashMap();
	}
	
	public SubmodelElementCollectionValue(Map<String, SubmodelElementValue> elements) {
		m_elements = elements;
	}
	
	public Map<String,SubmodelElementValue> get() {
		return m_elements;
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		
		for ( Map.Entry<String, SubmodelElementValue> ent: m_elements.entrySet() ) {
			gen.writeObjectField(ent.getKey(), ent.getValue());
		}
		
		gen.writeEndObject();
	}
}
