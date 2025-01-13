package mdt.model.sm.ref;

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
import com.google.common.base.Preconditions;

import utils.io.IOUtils;

import mdt.model.MDTModelSerDe;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public final class FileStoreElementReference extends AbstractElementReference implements ElementReference {
	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(FileStoreElementReference.class);

	private final File m_file;
	
	public FileStoreElementReference(@JsonProperty("file") File storeFile) {
		Preconditions.checkNotNull(storeFile, "File is null");
		
		m_file = storeFile;
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

	public static FileStoreElementReference parseString(String valueExpr) {
		return new FileStoreElementReference(new File(valueExpr));
	}
	
	public static FileStoreElementReference parseJson(ObjectNode topNode) {
		String path = topNode.get("path").asText();
		
		return new FileStoreElementReference(new File(path));
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField(ElementReference.FIELD_REFERENCE_TYPE, ElementReferenceType.FILE.getCode());
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
		
		FileStoreElementReference other = (FileStoreElementReference)obj;
		return Objects.equals(m_file, other.m_file);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_file);
	}
}