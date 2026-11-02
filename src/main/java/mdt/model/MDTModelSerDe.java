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
 *
 * @author Kang-Woo Lee (ETRI)
 */
//@UtilityClass
public class MDTModelSerDe {
	public static final JsonDeserializer JSON_DESERIALIZER = new JsonDeserializer();
	public static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
	public static final JsonMapper MAPPER = JsonMapper.builder()
							.addModule(new JavaTimeModule())
							.findAndAddModules()
							.build();
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

	public static JsonSerializer getJsonSerializer() {
		return JSON_SERIALIZER;
	}

	public static JsonDeserializer getJsonDeserializer() {
		return JSON_DESERIALIZER;
	}

	public static JsonMapper getJsonMapper() {
		return MAPPER;
	}
	
	public static JsonMapper getPrettyJsonMapper() {
		return PRETTY_MAPPER;
	}
	
	public static JsonNode readJsonNode(File jsonFile) throws IOException {
		return MAPPER.readTree(jsonFile);
	}

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

	public static <T> T readValue(String json, Class<T> cls) throws IOException {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, str=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

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

	public static <T> T readValue(File jsonFile, Class<T> cls) throws IOException {
		try ( InputStream is = new FileInputStream(jsonFile) ) {
			return JSON_DESERIALIZER.read(is, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, file=%s, cause=%s", cls, jsonFile, e);
			throw new IOException(msg, e);
		}
	}

	public static <T> List<T> readValueList(String json, Class<T> cls) throws IOException {
		try {
			return JSON_DESERIALIZER.readList(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON (List): class=%s, str=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

	public static <T> List<T> readValueList(JsonNode json, Class<T> cls) throws IOException {
		try {
			return JSON_DESERIALIZER.readList(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON (List): class=%s, JsonNode=%s, cause=%s", cls, json, e);
			throw new IOException(msg, e);
		}
	}

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

	public static String toJsonString(JsonNode node) throws IOException {
		return MAPPER.writeValueAsString(node);
	}

	public static JsonNode toJsonNode(Object modelObj) {
		return JSON_SERIALIZER.toNode(modelObj);
	}
}