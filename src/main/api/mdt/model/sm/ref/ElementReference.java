package mdt.model.sm.ref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.apache.tika.Tika;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.FileValue;


/**
 * 특정 {@link SubmodelElement}에 대한 참조(reference)를 추상화한 인터페이스이다.
 * <p>
 * 참조 대상의 실제 저장 위치(로컬 모델, 원격 Submodel 저장소 등)와 무관하게 다음 기능을
 * 일관된 방식으로 제공한다.
 * <ul>
 *   <li>읽기: {@link #read()}, {@link #readValue()} 및 타입별 편의 메소드
 *       ({@link #readAsProperty()}, {@link #readCollection()}, {@link #readList()} 등).</li>
 *   <li>갱신: {@link #write(SubmodelElement)}, {@link #update(SubmodelElement)},
 *       {@link #updateValue(ElementValue)}.</li>
 *   <li>'File' SubmodelElement의 첨부 파일 입출력:
 *       {@link #readAttachment(OutputStream)}, {@link #updateAttachment(FileValue, InputStream)},
 *       {@link #removeAttachment()}.</li>
 *   <li>Json 직렬화: {@link #toJsonString()}, {@link #toJsonNode()},
 *       {@link #serializeFields(JsonGenerator)}.</li>
 * </ul>
 * 구체 구현체는 {@link #getSerializationType()}이 반환하는 타입 식별자를 통해 Json
 * 직렬화/역직렬화 과정에서 구분된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ElementReferences.Serializer.class)
@JsonDeserialize(using = ElementReferences.Deserializer.class)
public interface ElementReference {
	/**
	 * 참조가 가리키는 {@link SubmodelElement}의 원형(prototype) 객체를 반환한다.
	 * <p>
	 * 원형은 실제 값을 읽지 않고도 참조 대상의 구조(식별자, 타입 등)를 알 수 있도록
	 * 제공되는 기준 객체이다.
	 *
	 * @return 원형 {@link SubmodelElement} 객체.
	 */
	public SubmodelElement getPrototype();
	
	/**
	 * 참조가 가리키는 {@link SubmodelElement}을 읽어 반환한다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	@NotNull public SubmodelElement read() throws IOException;

	/**
	 * 참조가 가리키는 {@link SubmodelElement}의 값(value)에
	 * 해당하는 부분만 읽어서 반환한다.
	 * 
	 * @return	{@link ElementValue} 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	@Nullable public ElementValue readValue() throws IOException;
	
	/**
	 * 참조가 가리키는 {@link SubmodelElement}을 읽어 {@link Property}로 반환한다.
	 *
	 * @return	{@link Property} 객체.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link Property}가 아닌 경우.
	 */
	@NotNull public default Property readAsProperty() throws IOException {
		SubmodelElement sme = read();
		if ( sme instanceof Property prop ) {
			return prop;
		}
		else {
			throw new IOException("not a Property: element=" + sme);
		}
	}
	/**
	 * 참조가 가리키는 {@link Property}의 문자열 값을 읽어 반환한다.
	 *
	 * @return	{@link Property}의 문자열 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 STRING 타입 {@link Property}가
	 * 					아닌 경우.
	 */
	@Nullable public default String readAsString() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.STRING ) {
			return prop.getValue();
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.STRING, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link SubmodelElement}을 읽어 {@link SubmodelElementCollection}으로 반환한다.
	 *
	 * @return	{@link SubmodelElementCollection} 객체.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이
	 * 					{@link SubmodelElementCollection}이 아닌 경우.
	 */
	public default SubmodelElementCollection readCollection() throws IOException {
		SubmodelElement smev = read();
		if ( smev instanceof SubmodelElementCollection ecv ) {
			return ecv;
		}
		else {
			throw new IOException("The SubmodelElement is not a SubmodelElementCollection: type="
									+ smev.getClass().getSimpleName());
		}
	}
	
	/**
	 * 참조가 가리키는 {@link SubmodelElement}을 읽어 {@link SubmodelElementList}로 반환한다.
	 *
	 * @return	{@link SubmodelElementList} 객체.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이
	 * 					{@link SubmodelElementList}가 아닌 경우.
	 */
	public default SubmodelElementList readList() throws IOException {
		SubmodelElement smev = read();
		if ( smev instanceof SubmodelElementList ecv ) {
			return ecv;
		}
		else {
			throw new IOException("The SubmodelElement is not a SubmodelElementList: type="
									+ smev.getClass().getSimpleName());
		}
	}
	
	/**
	 * 참조가 가리키는 'File' SubmodelElement을 읽어 값에 해당하는 부분을 반환한다.
	 * 
	 * @return	'File' SubmodelElement의 값에 해당하는 객체.
	 * @throws	IOException    읽기 과정에서 예외가 발생한 경우.
	 */
	@Nullable public default FileValue readAASFileValue() throws IOException {
		ElementValue smev = readValue();
		if ( smev == null ) {
			return null;
		}
		else if ( smev instanceof FileValue file ) {
			return file;
		}
		else {
			throw new IOException("The referenced SubmodelElement is not a FileValue: type="
											+ smev.getClass().getSimpleName());
		}
	}
	
	/**
	 * 참조가 가리키는 {@link SubmodelElement}의 값(value)을 읽어
	 * {@link ElementCollectionValue}로 반환한다.
	 *
	 * @return	{@link ElementCollectionValue} 객체. 값이 존재하지 않는 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 값이
	 * 					{@link ElementCollectionValue}가 아닌 경우.
	 */
	@Nullable public default ElementCollectionValue readCollectionValue() throws IOException {
		ElementValue smev = readValue();
		if ( smev == null ) {
			return null;
		}
		else if ( smev instanceof ElementCollectionValue ecv ) {
			return ecv;
		}
		else {
			throw new IOException("The ElementValue is not a ElementCollectionValue: type="
									+ smev.getClass().getSimpleName());
		}
	}
	
	/**
	 * 참조가 가리키는 {@link SubmodelElement}의 값(value)을 읽어
	 * {@link ElementListValue}로 반환한다.
	 *
	 * @return	{@link ElementListValue} 객체. 값이 존재하지 않는 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 값이
	 * 					{@link ElementListValue}가 아닌 경우.
	 */
	@Nullable public default ElementListValue readListValue() throws IOException {
		ElementValue smev = readValue();
		if ( smev == null ) {
			return null;
		}
		else if ( smev instanceof ElementListValue elv ) {
			return elv;
		}
		else {
			throw new IOException("The ElementValue is not a ElementListValue: type="
									+ smev.getClass().getSimpleName());
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@code int} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 정수 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#INT}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default Integer readAsInt() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.INT ) {
			try {
				return FOption.map(prop.getValue(), Integer::parseInt);
			}
			catch ( NumberFormatException e ) {
				String json = MDTModelSerDe.toJsonString(prop);
				throw new IOException(String.format("invalid %s value: prop=%s", DataTypeDefXsd.INT, json), e);
			}
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.INT, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@link BigInteger} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 {@link BigInteger} 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#INTEGER}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default BigInteger readAsInteger() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.INTEGER ) {
			try {
				return FOption.map(prop.getValue(), BigInteger::new);
			}
			catch ( NumberFormatException e ) {
				String json = MDTModelSerDe.toJsonString(prop);
				throw new IOException(String.format("invalid %s value: prop=%s", DataTypeDefXsd.INTEGER, json), e);
			}
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.INTEGER, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@code long} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 {@code long} 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#LONG}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default Long readAsLong() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.LONG ) {
			try {
				return FOption.map(prop.getValue(), Long::parseLong);
			}
			catch ( NumberFormatException e ) {
				String json = MDTModelSerDe.toJsonString(prop);
				throw new IOException(String.format("invalid %s value: prop=%s", DataTypeDefXsd.LONG, json), e);
			}
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.LONG, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@code boolean} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 {@code boolean} 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#BOOLEAN}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default Boolean readAsBoolean() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.BOOLEAN ) {
			return FOption.map(prop.getValue(), Boolean::parseBoolean);
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.BOOLEAN, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@code float} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 {@code float} 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#FLOAT}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default Float readAsFloat() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.FLOAT ) {
			try {
				return FOption.map(prop.getValue(), Float::parseFloat);
			}
			catch ( NumberFormatException e ) {
				String json = MDTModelSerDe.toJsonString(prop);
				throw new IOException(String.format("invalid %s value: prop=%s", DataTypeDefXsd.FLOAT, json), e);
			}
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.FLOAT, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@link Duration} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 {@link Duration} 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#DURATION}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default Duration readAsDuration() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.DURATION ) {
			try {
				return FOption.map(prop.getValue(), Duration::parse);
			}
			catch ( DateTimeParseException e ) {
				String json = MDTModelSerDe.toJsonString(prop);
				throw new IOException(String.format("invalid %s value: prop=%s", DataTypeDefXsd.DURATION, json), e);
			}
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.DURATION, json));
		}
	}
	
	/**
	 * 참조가 가리키는 {@link Property}의 값을 {@link LocalDateTime} 값으로 읽어 반환한다.
	 *
	 * @return	{@link Property}의 {@link LocalDateTime} 값. 값이 설정되지 않은 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 {@link DataTypeDefXsd#DATE_TIME}
	 * 					타입 {@link Property}가 아닌 경우.
	 */
	@Nullable public default LocalDateTime readAsDateTime() throws IOException {
		Property prop = readAsProperty();
		if ( prop.getValueType() == DataTypeDefXsd.DATE_TIME ) {
			try {
				return FOption.map(prop.getValue(), LocalDateTime::parse);
			}
			catch ( DateTimeParseException e ) {
				String json = MDTModelSerDe.toJsonString(prop);
				throw new IOException(String.format("invalid %s value: prop=%s", DataTypeDefXsd.DATE_TIME, json), e);
			}
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.DATE_TIME, json));
		}
	}
	
	/**
	 * 참조가 가리키는 'File' SubmodelElement에 첨부된 파일을 읽어
	 * 주어진 OutputStream으로 출력한다.
	 * 
	 * @param out	첨부 파일 내용을 출력할 OutputStream.
	 * @throws	IOException	읽기 과정에서 예외가 발생한 경우.
	 */
	public void readAttachment(OutputStream out) throws IOException;
	
	/**
	 * 참조가 가리키는 'File' SubmodelElement에 첨부된 파일을 읽어
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
	 * 참조가 가리키는 SubmodelElement을 주어진 SubmodelElement으로 갱신한다. 
	 *
	 * @param newElm	갱신할 새 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void write(SubmodelElement newElm) throws IOException;

	/**
	 * 참조가 가리키는 SubmodelElement의 값 부분을 주어진 SubmodelElement의 값으로 갱신한다.
	 *
	 * @param sme	갱신할 값을 포함한 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void update(SubmodelElement sme) throws IOException;

	/**
	 * 참조가 가리키는 SubmodelElement의 값 부분을 주어진 ElementValue으로 갱신한다.
	 *
	 * @param smev    갱신할 값.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateValue(ElementValue smev) throws IOException;

	/**
	 * 주어진 Json 문자열을 이용하여 참조가 가리키는 SubmodelElement의 값 부분을 갱신한다.
	 * 
	 * @param valueJsonString    New Json string
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateValue(String valueJsonString) throws IOException;
	
	/**
	 * 주어진 InputStream에서 내용을 읽어 참조가 가리키는
	 * 'File' SubmodelElement에 첨부된 파일 내용을 갱신한다.
	 * 
	 * @param file	갱신할 'File' SubmodelElement의 값 객체.
	 * @param content	첨부 파일 내용이 담긴 InputStream.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public void updateAttachment(FileValue file, InputStream content) throws IOException;
	
	/**
	 * 주어진 InputStream에서 내용을 읽어 참조가 가리키는
	 * 'File' SubmodelElement에 첨부된 파일 내용을 갱신한다.
	 * 
	 * @param content	첨부 파일 내용이 담긴 InputStream.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public default void updateAttachment(InputStream content) throws IOException {
		ElementValue sme = readValue();
		if ( sme == null ) {
			throw new IllegalStateException("The referenced SubmodelElement has no value");
		}
		if ( sme instanceof FileValue file ) {
			updateAttachment(file, content);
		}
		else {
			throw new IOException("The referenced SubmodelElement is not a FileValue: type="
								+ sme.getClass().getSimpleName());
		}
	}
	
	/**
	 * 주어진 파일을 읽어 참조가 가리키는
	 * 'File' SubmodelElement에 첨부된 파일 내용을 갱신한다.
	 * 
	 * @param contentFile	첨부 파일 내용이 담긴 파일 객체.
	 * @return	갱신된 'File' SubmodelElement의 값 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public default FileValue updateAttachment(File contentFile) throws IOException {
		ElementValue sme = readValue();
		if ( sme == null || sme instanceof FileValue ) {
			String contentType = new Tika().detect(contentFile);
			try ( InputStream in = new FileInputStream(contentFile) ) {
				FileValue fileVal = new FileValue(contentFile.getName(), contentType);
				updateAttachment(fileVal, in);
				
				return fileVal;
			}
		}
		else {
			throw new IOException("The referenced SubmodelElement is not a FileValue: type="
									+ sme.getClass().getSimpleName());
		}
	}
	
	/**
	 * 참조가 가리키는 'File' SubmodelElement에 첨부된 파일 내용을 제거한다.
	 * 
	 * @throws IOException 제거 과정에서 예외가 발생한 경우.
	 */
	public void removeAttachment() throws IOException;

	/**
	 * 참조가 가리키는 SubmodelElement의 경로 표현식을 반환한다.
	 * 
	 * @return 경로 표현식 문자열.
	 */
	public String toStringExpr();

	/**
	 * 참조의 직렬화 타입 식별자를 반환한다.
	 * <p>
	 * Json 직렬화/역직렬화 시 구체적인 {@link ElementReference} 구현체를 구분하는 데 사용된다.
	 *
	 * @return 직렬화 타입 식별 문자열.
	 */
	public String getSerializationType();
	
	/**
	 * JsonGenerator를 이용하여 참조가 가리키는 SubmodelElement의 값을 Json으로 직렬화한다.
	 * 
	 * @param gen	Json 직렬화 과정에서 사용할 JsonGenerator.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public void serializeFields(JsonGenerator gen) throws IOException;
	
	/**
	 * 참조가 가리키는 SubmodelElement의 값을 Json 문자열로 직렬화한다.
	 * 
	 * @return	Json 문자열.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public String toJsonString() throws IOException;
	
	/**
	 * 참조가 가리키는 SubmodelElement의 값을 JsonNode로 직렬화한다.
	 * 
	 * @return	JsonNode 객체.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	public JsonNode toJsonNode() throws IOException;
}
