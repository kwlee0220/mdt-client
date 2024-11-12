package mdt.model.sm;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.io.IOUtils;

import mdt.model.MDTModelSerDe;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public final class FileSMEReference extends AbstractSubmodelElementReference implements SubmodelElementReference {
	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(FileSMEReference.class);

	private final File m_file;
	
	public FileSMEReference(@JsonProperty("file") File file) {
		m_file = file;
	}
	
	public void initialize(SubmodelElement element) throws IOException {
		String jsonStr = MDTModelSerDe.toJsonString(element);
		IOUtils.toFile(jsonStr, m_file);
	}
	
	public File getFile() {
		return m_file;
	}

	@Override
	public SubmodelElement read() throws IOException {
		return MDTModelSerDe.readValue(m_file, SubmodelElement.class);
	}

	@Override
	public void write(SubmodelElement newElm) throws IOException {
		String jsonStr = MDTModelSerDe.toJsonString(newElm);
		IOUtils.toFile(jsonStr, m_file);
	}

	public static FileSMEReference parseString(String valueExpr) {
		return new FileSMEReference(new File(valueExpr));
	}
	
	public static FileSMEReference parseJson(ObjectNode topNode) {
		String path = topNode.get("path").asText();
		
		return new FileSMEReference(new File(path));
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField("referenceType", SubmodelElementReferenceType.FILE.name().toLowerCase());
		gen.writeStringField("path", m_file.getAbsolutePath());
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("%s", m_file.getAbsolutePath());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		FileSMEReference other = (FileSMEReference)obj;
		return Objects.equals(m_file, other.m_file);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_file);
	}
}