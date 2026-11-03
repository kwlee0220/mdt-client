package mdt.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import utils.InternalException;

/**
 * MDT 모델 객체의 JSON 직렬화/역직렬화를 위한 공용 serializer/deserializer와 유틸리티 메소드를 제공한다.
 * <p>
 * AAS 모델 객체는 AAS4J {@link JsonSerializer}/{@link JsonDeserializer}로, 일반 JSON 트리는
 * {@link JavaTimeModule}이 등록되고 날짜를 timestamp가 아닌 문자열로 기록하는 Jackson
 * {@link JsonMapper}로 처리한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
//@UtilityClass
public class MDTModelSerDe {
	/** AAS 모델 객체용 공용 AAS4J JSON deserializer. */
	public static final JsonDeserializer JSON_DESERIALIZER = new JsonDeserializer();
	/** AAS 모델 객체용 공용 AAS4J JSON serializer. */
	public static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
	/** 공용 Jackson {@link JsonMapper}. 날짜/시간 값은 ISO 문자열로 기록된다. */
	public static final JsonMapper MAPPER = JsonMapper.builder()
							.addModule(new JavaTimeModule())
							.findAndAddModules()
							.build();
	/** 들여쓰기된 출력을 생성하는 공용 Jackson {@link JsonMapper}. 그 밖의 설정은 {@link #MAPPER}와 같다. */
	public static final JsonMapper PRETTY_MAPPER = JsonMapper.builder()
							.addModule(new JavaTimeModule())
							.findAndAddModules()
							.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
							.build();
	
	static {
		MAPPER.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		PRETTY_MAPPER.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
	
	private MDTModelSerDe() {
		throw new AssertionError();
	}

	/**
	 * 공용 AAS4J JSON serializer를 반환한다.
	 *
	 * @return	{@link #JSON_SERIALIZER}.
	 */
	public static JsonSerializer getJsonSerializer() {
		return JSON_SERIALIZER;
	}

	/**
	 * 공용 AAS4J JSON deserializer를 반환한다.
	 *
	 * @return	{@link #JSON_DESERIALIZER}.
	 */
	public static JsonDeserializer getJsonDeserializer() {
		return JSON_DESERIALIZER;
	}

	/**
	 * 공용 Jackson {@link JsonMapper}를 반환한다.
	 *
	 * @return	{@link #MAPPER}.
	 */
	public static JsonMapper getJsonMapper() {
		return MAPPER;
	}
	
	/**
	 * 들여쓰기 출력용 공용 Jackson {@link JsonMapper}를 반환한다.
	 *
	 * @return	{@link #PRETTY_MAPPER}.
	 */
	public static JsonMapper getPrettyJsonMapper() {
		return PRETTY_MAPPER;
	}
	
	/**
	 * 주어진 JSON 파일을 읽어 JSON 트리로 반환한다.
	 *
	 * @param jsonFile	JSON 파일.
	 * @return	JSON 노드.
	 * @throws IOException	파일 읽기 또는 JSON 파싱에 실패한 경우.
	 */
	public static JsonNode readJsonNode(File jsonFile) throws IOException {
		return MAPPER.readTree(jsonFile);
	}

	/**
	 * 주어진 JSON 문자열을 JSON 트리로 읽는다.
	 * <p>
	 * 첫 JSON 값만 읽고 그 뒤의 입력은 무시한다. 전체 입력을 검사하려면
	 * {@link #readSingleJsonNode(String)}을 사용한다.
	 *
	 * @param jsonString	JSON 문자열.
	 * @return	JSON 노드.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static JsonNode readJsonNode(String jsonString) throws IOException {
		return MAPPER.readTree(jsonString);
	}

	/**
	 * 문자열 전체가 JSON 값 하나인지 확인하며 트리로 읽는다.
	 * <p>
	 * {@link #readJsonNode(String)}({@code readTree})는 첫 JSON 값만 읽고 뒤의 입력을 무시한다(예:
	 * {@code {...} garbage}, {@code {...} {}}). 이 메소드는 공유 매퍼의 설정은 바꾸지 않고
	 * {@code FAIL_ON_TRAILING_TOKENS}를 켠 reader로 읽어 그런 입력을 거부한다. 뒤의 공백·개행은 허용한다.
	 *
	 * @param jsonString	JSON 문자열.
	 * @return	JSON 노드.
	 * @throws IOException	입력이 비었거나, JSON 문법이 틀렸거나, 첫 값 뒤에 다른 입력이 있는 경우.
	 */
	public static JsonNode readSingleJsonNode(String jsonString) throws IOException {
		return MAPPER.readerFor(JsonNode.class)
					.with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
					.readValue(jsonString);
	}

	/**
	 * AAS4J deserializer로 JSON 문자열을 주어진 클래스의 모델 객체로 읽는다.
	 *
	 * @param <T>	읽을 객체의 타입.
	 * @param json	JSON 문자열.
	 * @param cls	읽을 객체의 클래스.
	 * @return	읽은 객체.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static <T> T readValue(String json, Class<T> cls) throws IOException {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, str=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

	/**
	 * AAS4J deserializer로 JSON 노드를 주어진 클래스의 모델 객체로 읽는다.
	 *
	 * @param <T>	읽을 객체의 타입.
	 * @param json	JSON 노드.
	 * @param cls	읽을 객체의 클래스.
	 * @return	읽은 객체. {@code json}이 {@code null}이거나 JSON {@code null}이면 {@code null}.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static <T> T readValue(JsonNode json, Class<T> cls) throws IOException {
		if ( json == null || json.isNull() ) {
			return null;
		}
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, JsonNode=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

	/**
	 * AAS4J deserializer로 JSON 파일을 주어진 클래스의 모델 객체로 읽는다.
	 *
	 * @param <T>	읽을 객체의 타입.
	 * @param jsonFile	JSON 파일.
	 * @param cls	읽을 객체의 클래스.
	 * @return	읽은 객체.
	 * @throws IOException	파일 읽기 또는 JSON 파싱에 실패한 경우.
	 */
	public static <T> T readValue(File jsonFile, Class<T> cls) throws IOException {
		try ( InputStream is = new FileInputStream(jsonFile) ) {
			return JSON_DESERIALIZER.read(is, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, file=%s, cause=%s", cls, jsonFile, e);
			throw new IOException(msg, e);
		}
	}

	/**
	 * AAS4J deserializer로 JSON 배열 문자열을 주어진 클래스의 모델 객체 리스트로 읽는다.
	 *
	 * @param <T>	리스트 원소의 타입.
	 * @param json	JSON 배열 문자열.
	 * @param cls	리스트 원소의 클래스.
	 * @return	읽은 객체 리스트.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static <T> List<T> readValueList(String json, Class<T> cls) throws IOException {
		try {
			return JSON_DESERIALIZER.readList(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON (List): class=%s, str=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

	/**
	 * AAS4J deserializer로 JSON 배열 노드를 주어진 클래스의 모델 객체 리스트로 읽는다.
	 *
	 * @param <T>	리스트 원소의 타입.
	 * @param json	JSON 배열 노드.
	 * @param cls	리스트 원소의 클래스.
	 * @return	읽은 객체 리스트.
	 * @throws IOException	JSON 파싱에 실패한 경우.
	 */
	public static <T> List<T> readValueList(JsonNode json, Class<T> cls) throws IOException {
		try {
			return JSON_DESERIALIZER.readList(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON (List): class=%s, JsonNode=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

	/**
	 * AAS4J serializer로 주어진 모델 객체를 JSON 문자열로 변환한다.
	 *
	 * @param modelObj	변환할 모델 객체.
	 * @return	JSON 문자열.
	 * @throws InternalException	직렬화에 실패한 경우.
	 */
	public static String toJsonString(Object modelObj) {
		try {
			return JSON_SERIALIZER.write(modelObj);
//			return MAPPER.writeValueAsString(modelObj);
		}
		catch ( SerializationException e ) {
			String msg = String.format("Failed to writeJson: %s, cause=%s", modelObj, e);
			throw new InternalException(msg, e);
		}
	}

	/**
	 * 공용 Jackson 매퍼로 주어진 JSON 노드를 문자열로 변환한다.
	 *
	 * @param node	변환할 JSON 노드.
	 * @return	JSON 문자열.
	 * @throws IOException	직렬화에 실패한 경우.
	 */
	public static String toJsonString(JsonNode node) throws IOException {
		return MAPPER.writeValueAsString(node);
	}

	/**
	 * AAS4J serializer로 주어진 모델 객체를 JSON 노드로 변환한다.
	 *
	 * @param modelObj	변환할 모델 객체.
	 * @return	JSON 노드.
	 */
	public static JsonNode toJsonNode(Object modelObj) {
		return JSON_SERIALIZER.toNode(modelObj);
	}
}