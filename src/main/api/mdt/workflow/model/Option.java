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
@JsonSerialize(using = Options.Serializer.class)
@JsonDeserialize(using = Options.Deserializer.class)
public interface Option<T> {
	public String getName();
	public T getValue();
	
	public List<String> toCommandOptionSpec();

	public String getSerializationType();
	public void serializeFields(JsonGenerator gen) throws IOException;
}
