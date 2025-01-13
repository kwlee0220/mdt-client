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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

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
//															.addModule(new JavaTimeModule())
															.findAndAddModules()
															.build();
	
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
	
	public static JsonNode readJsonNode(File jsonFile) throws IOException {
		return MAPPER.readTree(jsonFile);
	}

	public static JsonNode readJsonNode(String jsonString) throws IOException {
		return MAPPER.readTree(jsonString);
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
		}
		catch ( SerializationException e ) {
			String msg = String.format("Failed to writeJson: %s, cause=%s", modelObj, e);
			throw new InternalException(msg);
		}
	}

//	public static String toJsonString(SubmodelElementValue smev) {
//		try {
//			return JSON_SERIALIZER.write(smev);
//		}
//		catch ( SerializationException e ) {
//			String msg = String.format("Failed to writeJson: value=%s, cause=%s", smev, e);
//			throw new InternalException(msg);
//		}
//	}

	public static String toJsonString(JsonNode node) throws IOException {
		return MAPPER.writeValueAsString(node);
	}

	public static JsonNode toJsonNode(Object modelObj) {
		return JSON_SERIALIZER.toNode(modelObj);
	}
}
