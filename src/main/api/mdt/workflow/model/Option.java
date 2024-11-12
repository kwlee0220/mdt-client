package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = OptionSerializer.class)
@JsonDeserialize(using = OptionDeserializer.class)
public interface Option {
	public String getName();
	
	public List<String> toCommandOptionSpec();
	public void serialize(JsonGenerator gen) throws IOException;
}
