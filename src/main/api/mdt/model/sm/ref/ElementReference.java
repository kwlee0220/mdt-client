package mdt.model.sm.ref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tika.Tika;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import utils.func.Unchecked;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ElementReferences.Serializer.class)
@JsonDeserialize(using = ElementReferences.Deserializer.class)
public interface ElementReference {
	/**
	 * SubmodelElement의 참조가 가리키는 {@link SubmodelElement}을 읽어 반환한다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	public SubmodelElement read() throws IOException;

	/**
	 * SubmodelElement의 참조가 가리키는 {@link SubmodelElement}을 읽어서
	 * 그 중 값에 해당하는 부분만 반환한다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	public default ElementValue readValue() throws IOException {
		SubmodelElement sme = read();
		return (sme != null) ? ElementValues.getValue(sme) : null;
	}
	
	public default Property readAsProperty() throws IOException {
		SubmodelElement sme = read();
		if ( sme == null ) {
			return null;
		}
		else if ( sme instanceof Property prop ) {
			return prop;
		}
		else {
			throw new IOException("not a Property: element=" + sme);
		}
	}
	public default String readAsString() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.STRING ) {
			return prop.getValue().toString();
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.STRING, json));
		}
	}
	
	/**
	 * SubmodelElement의 참조가 가리키는 'File' SubmodelElement을 읽어 값에 해당하는 부분을 반환한다.
	 * 
	 * @return	'File' SubmodelElement의 값에 해당하는 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	public default FileValue readAASFileValue() throws IOException {
		ElementValue smev = readValue();
		if ( smev instanceof FileValue file ) {
			return file;
		}
		else {
			throw new IllegalStateException("The referenced SubmodelElement is not a FileValue: type="
											+ smev.getClass().getSimpleName());
		}
	}
	
	/**
	 * SubmodelElement의 참조가 가리키는 'File' SubmodelElement에 첨부된 파일을 읽어
	 * 주어진 OutputStream으로 출력한다.
	 * 
	 * @param out	첨부 파일 내용을 출력할 OutputStream.
	 * @throws	IOException	읽기 과정에서 예외가 발생한 경우.
	 */
	public void readAttachment(OutputStream out) throws IOException;
	
	/**
	 * SubmodelElement의 참조가 가리키는 'File' SubmodelElement에 첨부된 파일을 읽어
	 * 주어진 파일로 저장한다.
	 *
	 * @param outputFile	첨부 파일 내용을 저장할 파일 객체.
	 * @throws IOException	읽기 과정에서 예외가 발생한 경우.
	 */
	public default void readAttachment(File outputFile) throws IOException {
		try ( OutputStream out = new FileOutputStream(outputFile) ) {
			readAttachment(out);
		}
	}
	
	/**
	 * SubmodelElement의 참조가 가리키는 SubmodelElement을 주어진 SubmodelElement으로 갱신한다. 
	 *
	 * @param newElm	갱신할 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void write(SubmodelElement newElm) throws IOException;

	/**
	 * SubmodelElement의 참조가 가리키는 SubmodelElement의 값 부분을 주어진 SubmodelElement의 값으로 갱신한다.
	 *
	 * @param sme	갱신할 값을 포함한 SubmodelElement 객체.
	 * @return    갱신된 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void update(SubmodelElement sme) throws IOException;

	/**
	 * SubmodelElement의 참조가 가리키는 SubmodelElement의 값 부분을 주어진 ElementValue으로 갱신한다.
	 *
	 * @param smev    갱신할 값.
	 * @return    갱신된 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateValue(ElementValue smev) throws IOException;

	/**
	 * 주어진 Json 문자열을 이용하여 SubmodelElement의 참조가 가리키는 SubmodelElement의 값 부분을 갱신한다.
	 * 
	 * @param valueJsonString    New Json string
	 * @return    갱신된 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateValue(String valueJsonString) throws IOException;
	
	/**
	 * 주어진 InputStream에서 내용을 읽어 SubmodelElement의 참조가 가리키는
	 * 'File' SubmodelElement에 첨부된 파일 내용을 갱신한다.
	 * 
	 * @param file	갱신할 'File' SubmodelElement의 값 객체.
	 * @param content	첨부 파일 내용이 담긴 InputStream.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateAttachment(FileValue file, InputStream content) throws IOException;
	
	/**
	 * 주어진 InputStream에서 내용을 읽어 SubmodelElement의 참조가 가리키는
	 * 'File' SubmodelElement에 첨부된 파일 내용을 갱신한다.
	 * 
	 * @param content	첨부 파일 내용이 담긴 InputStream.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public default void updateAttachment(InputStream content) throws IOException {
		ElementValue sme = readValue();
		if ( sme instanceof FileValue file ) {
			updateAttachment(file, content);
		}
		else {
			throw new IllegalStateException("The referenced SubmodelElement is not a FileValue: type="
											+ sme.getClass().getSimpleName());
		}
	}
	
	/**
	 * 주어진 파일을 읽어 SubmodelElement의 참조가 가리키는
	 * 'File' SubmodelElement에 첨부된 파일 내용을 갱신한다.
	 * 
	 * @param contentFile	첨부 파일 내용이 담긴 파일 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public default void updateAttachment(File contentFile) throws IOException {
		ElementValue sme = readValue();
		if ( sme instanceof FileValue file ) {
			if ( file.getValue() != null ) {
				Unchecked.runOrIgnore(this::removeAttachment);
			}
			
			String contentType = new Tika().detect(contentFile);
			try ( InputStream out = new FileInputStream(contentFile) ) {
				updateAttachment(new FileValue(contentFile.getName(), contentType), out);
			}
		}
		else {
			throw new IllegalStateException("The referenced SubmodelElement is not a FileValue: type="
											+ sme.getClass().getSimpleName());
		}
	}
	
	public void removeAttachment() throws IOException;

	public String toStringExpr();

	public String getSerializationType();
	
	/**
	 * JsonGenerator를 이용하여 SubmodelElement의 참조가 가리키는 SubmodelElement의 값을 Json으로 직렬화한다.
	 * 
	 * @param gen	Json 정렬화 과정에서 사용할 JsonGenerator.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public void serializeFields(JsonGenerator gen) throws IOException;
	
	/**
	 * SubmodelElement의 참조가 가리키는 SubmodelElement의 값을 Json 문자열로 직렬화한다.
	 * 
	 * @return	Json 문자열.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public String toJsonString() throws IOException;
	
	/**
	 * SubmodelElement의 참조가 가리키는 SubmodelElement의 값을 JsonNode로 직렬화한다.
	 * 
	 * @return	JsonNode 객체.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public JsonNode toJsonNode() throws IOException;
}
