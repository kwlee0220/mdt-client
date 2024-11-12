package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementListValue implements SubmodelElementValue, Supplier<List<SubmodelElementValue>> {
	private final List<SubmodelElementValue> m_elementValues;
	
	public SubmodelElementListValue(List<SubmodelElementValue> values) {
		m_elementValues = values;
	}
	
	@Override
	public List<SubmodelElementValue> get() {
		return m_elementValues;
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		
		for ( SubmodelElementValue smev: m_elementValues ) {
			gen.writeObject(smev);
		}
		
		gen.writeEndArray();
	}
}
