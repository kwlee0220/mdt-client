package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.value.SubmodelElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractSubmodelElementReference implements SubmodelElementReference {
	@Override
	public SubmodelElementValue readValue() throws IOException {
		SubmodelElement sme = read();
		if ( sme != null ) {
			return ElementValues.getValue(sme);
		}
		else {
			return null;
		}
	}

	@Override
	public void update(SubmodelElement sme) throws IOException {
		update(ElementValues.getValue(sme));
	}

	@Override
	public void update(SubmodelElementValue smev) throws IOException {
		SubmodelElement sme = read();
		if ( sme != null ) {
			write(ElementValues.update(sme, smev));
		}
	}

	@Override
	public void updateWithValueJsonNode(JsonNode valueNode) throws IOException {
		SubmodelElement sme = read();
		write(ElementValues.update(sme, valueNode));
	}

	@Override
	public void updateWithValueJsonString(String valueJsonString) throws IOException {
		updateWithValueJsonNode(MDTModelSerDe.readJsonNode(valueJsonString));
	}

	@Override
	public void updateWithExternalString(String externStr) throws IOException {
		externStr = externStr.trim();
		JsonNode rawValue = ( externStr.startsWith("{") )
							? MDTModelSerDe.readJsonNode(externStr)
							: new TextNode(externStr);
		updateWithValueJsonNode(rawValue);
	}

	@Override
	public String toExternalString() throws IOException {
		SubmodelElementValue smev = readValue();
		if ( smev != null ) {
			return ( smev instanceof PropertyValue propv )
					? FOption.getOrElse(propv.getValue(), "")
					: MDTModelSerDe.toJsonString(smev);
		}
		else {
			return null;
		}
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		throw new UnsupportedOperationException();
	}
}
