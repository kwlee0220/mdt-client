package mdt.model.sm.ref;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import utils.io.IOUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileStoreReference extends AbstractElementReference implements ElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:file";
	
	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(FileStoreReference.class);

	private final File m_file;
	
	public FileStoreReference(File storeFile) {
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

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		SubmodelElement holder = read();
		ElementValues.update(holder, smev);
		write(holder);
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		SubmodelElement proto = read();
		ElementValue newVal = ElementValues.parseValueJsonString(proto, valueJsonString);
		updateValue(newVal);
	}

	public static FileStoreReference parseString(String valueExpr) {
		return new FileStoreReference(new File(valueExpr));
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStringField("path", m_file.getAbsolutePath());
	}

	public static FileStoreReference deserializeFields(JsonNode jnode) {
		String path = jnode.get("path").asText();
		
		return new FileStoreReference(new File(path));
	}

	@Override
	public String toStringExpr() {
		return String.format("%s", m_file.getAbsolutePath());
	}
	
	@Override
	public String toString() {
		return toStringExpr();
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		FileStoreReference other = (FileStoreReference)obj;
		return Objects.equals(m_file, other.m_file);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_file);
	}
}