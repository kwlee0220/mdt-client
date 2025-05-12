package mdt.model.sm.value;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ElementValues.Serializer.class)
public interface ElementValue {
	public String getSerializationType();
	public void serialize(JsonGenerator gen) throws IOException;
}
