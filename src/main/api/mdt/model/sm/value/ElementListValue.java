package mdt.model.sm.value;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.InternalException;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementListValue implements ElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:list";
	
	private final List<ElementValue> m_elementValues;
	
	public ElementListValue(List<ElementValue> values) {
		m_elementValues = values;
	}
	
	public List<ElementValue> getElementAll() {
		return m_elementValues;
	}
	
	public static ElementListValue parseJsonNode(JsonNode valueNode, Class<? extends ElementValue> elmClass)
		throws IOException {
		try {
			Method parseElementJsonNode = elmClass.getDeclaredMethod("parseJsonNode", JsonNode.class);
			List<ElementValue> elements
						= FStream.from(valueNode.elements())
								.mapOrThrow(elmNode -> (ElementValue)parseElementJsonNode.invoke(null, elmNode))
								.toList();
			return new ElementListValue(elements);
		}
		catch ( SecurityException | ReflectiveOperationException e ) {
			throw new InternalException("Failed to parse JSON node: " + valueNode, e);
		}
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		for ( ElementValue smev: m_elementValues ) {
			gen.writeObject(smev);
		}
		gen.writeEndArray();
	}
}
