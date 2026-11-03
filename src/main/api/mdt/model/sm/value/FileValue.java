package mdt.model.sm.value;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.apache.tika.Tika;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.Preconditions;
import utils.json.JacksonUtils;


/**
 * AAS {@link File} SubmodelElement의 값(파일 경로와 content type)을 표현하는 클래스.
 * <p>
 * 값 객체({@link #toValueObject()})와 value-only JSON은 {@code {"contentType": ..., "value": ...}}
 * 형태이며, polymorphic JSON의 타입 식별자는 {@value #SERIALIZATION_TYPE}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileValue extends AbstractElementValue implements DataElementValue {
	/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
	public static final String SERIALIZATION_TYPE = "mdt:value:file";
	
	@NotNull private final String m_contentType;
	private final String m_value;
	
	/**
	 * 주어진 파일 경로와 content type으로 {@code FileValue} 객체를 생성한다.
	 *
	 * @param path			파일 경로(AAS File의 {@code value}). {@code null}일 수 있다.
	 * @param contentType	파일의 content type(MIME 타입).
	 * @throws IllegalArgumentException	{@code contentType}이 {@code null}인 경우.
	 */
	public FileValue(String path, @NotNull String contentType) {
		Preconditions.checkNotNullArgument(contentType, "contentType must not be null");
		
		m_contentType = contentType;
		m_value = path;
	}
	
	private static final Tika TIKA = new Tika();

	/**
	 * 로컬 파일로부터 {@code FileValue} 객체를 생성한다.
	 * <p>
	 * 경로로는 파일 이름({@link java.io.File#getName()})만 사용하고, content type은 Apache Tika로
	 * 파일 내용을 검사하여 결정한다.
	 *
	 * @param file	대상 파일.
	 * @return	생성된 {@code FileValue} 객체.
	 * @throws IOException	content type 판별을 위해 파일을 읽는 중 오류가 발생한 경우.
	 */
	public static FileValue of(java.io.File file) throws IOException {
		return new FileValue(file.getName(), TIKA.detect(file));
	}
	
	/**
	 * AAS {@link File} 객체의 값({@code value}, {@code contentType})으로 {@code FileValue} 객체를 생성한다.
	 *
	 * @param aasFile	값을 추출할 AAS File 객체.
	 * @return	생성된 {@code FileValue} 객체.
	 * @throws IllegalArgumentException	{@code aasFile}의 content type이 {@code null}인 경우.
	 */
	public static FileValue from(File aasFile) {
		return new FileValue(aasFile.getValue(), aasFile.getContentType());
	}
	
	/**
	 * 파일의 content type(MIME 타입)을 반환한다.
	 *
	 * @return	content type.
	 */
	@NotNull public String getMimeType() {
		return m_contentType;
	}
	
	/**
	 * 파일 경로(AAS File의 {@code value})를 반환한다.
	 *
	 * @return	파일 경로. 설정되지 않은 경우는 {@code null}.
	 */
	public String getValue() {
		return m_value;
	}
	
	/**
	 * 주어진 AAS {@link File} 객체의 content type과 값을 이 객체의 것으로 갱신한다.
	 *
	 * @param aasFile	갱신할 AAS File 객체.
	 */
	public void update(File aasFile) {
		aasFile.setContentType(m_contentType);
		aasFile.setValue(m_value);
	}

	@Override
	public Map<String,String> toValueObject() {
		Map<String,String> value = Maps.newLinkedHashMap();
		value.put(FIELD_CONTENT_TYPE, m_contentType);
		value.put(FIELD_VALUE, m_value);
		return value;
	}
	
	/**
	 * 값 객체({@code contentType}/{@code value} 키를 가진 {@link Map})로부터 {@code FileValue}를 생성한다.
	 *
	 * @param value	값 객체.
	 * @return	생성된 {@code FileValue} 객체.
	 * @throws IOException	{@code value}가 {@link Map}이 아니거나 {@code contentType} 키가 없는 경우.
	 */
	public static FileValue fromValueObject(Object value) throws IOException {
		if ( value instanceof Map vmap ) {
			String contentType = (String) vmap.get(FIELD_CONTENT_TYPE);
			if ( contentType == null ) {
				throw new IOException("FileValue value has no contentType: value=" + value);
			}
			String val = (String) vmap.get(FIELD_VALUE);
			return new FileValue(val, contentType);
		}
		else {
			throw new IOException("FileValue value is not a Map: value=" + value);
		}
	}
	
	/**
	 * value-only JSON 노드({@code {"contentType": ..., "value": ...}})로부터 {@code FileValue}를 생성한다.
	 *
	 * @param jnode	JSON 노드. {@code value} 필드는 생략할 수 있다.
	 * @return	생성된 {@code FileValue} 객체.
	 * @throws IOException	노드가 JSON 객체가 아니거나 {@code contentType} 필드가 없거나 문자열이 아닌 경우.
	 */
	public static FileValue parseValueJsonNode(JsonNode jnode) throws IOException {
		if ( !jnode.isObject() ) {
			throw new IOException("FileValue expects 'Object' node: JsonNode=" + jnode);
		}
		
		String contentType = JacksonUtils.getStringField(jnode, FIELD_CONTENT_TYPE);
		String value = JacksonUtils.getStringFieldOrNull(jnode, FIELD_VALUE);
		
		return new FileValue(value, contentType);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_value, m_contentType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || FileValue.class != obj.getClass() ) {
			return false;
		}
		
		FileValue other = (FileValue) obj;
		return Objects.equals(m_value, other.m_value)
				&& Objects.equals(m_contentType, other.m_contentType);
	}
	
	@Override
	public String toString() {
		return String.format("file:'%s' ('%s')", (m_value != null) ? m_value : "None", this.m_contentType);
	}

	private static final String FIELD_CONTENT_TYPE = "contentType";
	private static final String FIELD_VALUE = "value";

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_CONTENT_TYPE, m_contentType);
		gen.writeStringField(FIELD_VALUE, m_value);
		gen.writeEndObject();
	}
	
	/**
	 * polymorphic JSON의 {@code value} 하위 노드로부터 {@code FileValue}를 생성한다.
	 * <p>
	 * {@link ElementValues}의 역직렬화 등록부에서 사용된다.
	 *
	 * @param vnode	{@code {"contentType": ..., "value": ...}} 형태의 JSON 노드.
	 * @return	생성된 {@code FileValue} 객체.
	 * @throws IOException	{@code contentType} 필드가 없거나 문자열이 아닌 경우.
	 */
	public static FileValue deserializeValue(JsonNode vnode) throws IOException {
		String mimeType = JacksonUtils.getStringField(vnode, FIELD_CONTENT_TYPE);
		String value = JacksonUtils.getStringFieldOrNull(vnode, FIELD_VALUE);
		return new FileValue(value, mimeType);
	}
}
