package mdt.model.sm.ref;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.io.IOUtils;
import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;


/**
 * 로컬 파일에 Json 형태로 저장된 {@link SubmodelElement}를 참조하는
 * {@link ElementReference} 구현체이다.
 * <p>
 * 읽기/쓰기는 대상 파일 전체를 Json으로 읽고 쓰는 방식으로 수행되며,
 * 첨부파일 입출력은 지원하지 않는다.
 * <p>
 * Json 직렬화 시 {@link #SERIALIZATION_TYPE}({@code "mdt:ref:file"}) 타입으로 식별되며,
 * 파일 경로는 절대 경로로 직렬화되므로 다른 장비에서 역직렬화하는 경우에는 호환되지
 * 않을 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileStoreReference extends AbstractElementReference implements ElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:file";

	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(FileStoreReference.class);

	private final File m_file;

	/**
	 * 주어진 파일에 저장된 SubmodelElement를 참조하는 {@code FileStoreReference}를 생성한다.
	 *
	 * @param storeFile	SubmodelElement가 Json 형태로 저장된(또는 저장될) 파일.
	 */
	public FileStoreReference(File storeFile) {
		Preconditions.checkNotNullArgument(storeFile, "File is null");

		m_file = storeFile;
	}

	/**
	 * 참조 대상 SubmodelElement가 저장된 파일을 반환한다.
	 *
	 * @return	저장 파일 객체.
	 */
	public File getFile() {
		return m_file;
	}
	
	@Override
	public SubmodelElement getPrototype() {
		try {
			return read();
		}
		catch ( IOException e ) {
			throw new UncheckedIOException(e);
		}
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
	public ElementValue readValue() throws IOException {
		return ElementValues.getValue(read());
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		SubmodelElement holder = read();
		ElementValues.update(holder, smev);
		write(holder);
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		ElementValue proto = readValue();
		ElementValue newVal = ElementValues.parseValueJsonString(valueJsonString, proto);
		updateValue(newVal);
	}

	/**
	 * 파일 기반 참조는 첨부파일을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void readAttachment(OutputStream out) throws IOException {
		throw new UnsupportedOperationException("readAttachment is not supported for FileStoreReference");
	}

	/**
	 * 파일 기반 참조는 첨부파일을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void updateAttachment(FileValue file, InputStream content) throws IOException {
		throw new UnsupportedOperationException("updateAttachment is not supported for FileStoreReference");
	}

	/**
	 * 파일 기반 참조는 첨부파일을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void removeAttachment() throws IOException {
		throw new UnsupportedOperationException("removeAttachment is not supported for FileStoreReference");
	}

	/**
	 * 파일 경로 문자열로부터 {@code FileStoreReference}를 생성한다.
	 *
	 * @param valueExpr	저장 파일의 경로 문자열.
	 * @return 생성된 {@link FileStoreReference} 객체.
	 */
	public static FileStoreReference parseString(String valueExpr) {
		return new FileStoreReference(new File(valueExpr));
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	/**
	 * 이 참조의 필드들을 주어진 {@link JsonGenerator}로 직렬화한다.
	 * <p>
	 * 저장 파일의 절대 경로를 {@code path} 필드로 기록한다.
	 *
	 * @param gen	직렬화에 사용할 {@link JsonGenerator}.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStringField("path", m_file.getAbsolutePath());
	}

	/**
	 * Json 객체로부터 {@link FileStoreReference}를 복원한다.
	 *
	 * @param jnode	{@code path} 필드를 담은 Json 노드.
	 * @return 복원된 {@link FileStoreReference} 객체.
	 * @throws IOException	{@code path} 필드가 존재하지 않는 경우.
	 */
	public static FileStoreReference deserializeFields(JsonNode jnode) throws IOException {
		String path = JacksonUtils.getStringField(jnode, "path");

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