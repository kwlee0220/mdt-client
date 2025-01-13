package mdt.model.instance;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Preconditions;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class FileParameterInfo extends DefaultParameterInfo {
	@JsonProperty("contentType") private final String m_contentType;
	@JsonProperty("value") private final String m_value;
	
	public static FileParameterInfo from(String id, File file) {
        return new FileParameterInfo(id, file.getContentType(), file.getValue());
	}
	
	@JsonCreator
	public FileParameterInfo(@JsonProperty("id") String propId,
								@JsonProperty("contentType") String contentType,
								@JsonProperty("value") String value) {
		super(propId, "File");
		Preconditions.checkArgument(contentType != null, "Null contentType");
		
		m_contentType = contentType;
		m_value = value;
	}

	public String getContentType() {
		return m_contentType;
	}

	@Override
	public String getValue() {
		return m_value;
	}
	
	@Override
	public String toValueString() {
		return ( this.m_value != null && this.m_value.length() > 0 )
					? String.format("%s (%s)", this.m_value, this.m_contentType)
					: "None";
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("id", getId());
		gen.writeStringField("type", getType());
		gen.writeStringField("contentType", m_contentType);
		gen.writeStringField("value", m_value);
		gen.writeEndObject();
	}

	@Override
	public String toString() {
		return String.format("File{%s=%s}", getId(), toValueString());
	}
	
	public static final void main(String... args) throws Exception {
		DefaultFile file = new DefaultFile();
		file.setIdShort("file1");
		file.setValue("test.jpg");
		file.setContentType("image/jpeg");
		
		FileParameterInfo param = FileParameterInfo.from("param1", file);
		String jsonStr = MDTModelSerDe.toJsonString(param);
		System.out.println(jsonStr);
		
		String jsonStr2 = """
		{
			"id": "param2",
			"type" : "File",
			"contentType" : "image/jpeg",
			"value" : "test.jpg"
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr2, FileParameterInfo.class));
		
		String jsonStr3 = """
		{
			"id": "param3",
			"type" : "File",
			"contentType" : "image/jpeg",
			"value" : null
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr3, FileParameterInfo.class));
		
		String jsonStr4 = """
		{
			"id": "task4",
			"type" : "File",
			"contentType" : "image/jpeg",
		}""";
		try {
			System.out.println(MDTModelSerDe.readValue(jsonStr4, FileParameterInfo.class));
		}
		catch ( IOException e ) {
			System.out.println(e);
		}
		
		String jsonStr5 = """
		{
			"id": "task5",
			"type" : "File",
			"value" : "test.jpg"
		}""";
		try {
			System.out.println(MDTModelSerDe.readValue(jsonStr5, FileParameterInfo.class));
		}
		catch ( IOException e ) {
			System.out.println(e);
		}
	}
}
