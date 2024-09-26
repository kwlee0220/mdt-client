package mdt.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Sets;

import utils.InternalException;

import lombok.experimental.UtilityClass;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.resource.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class AASUtils {
	public static final DatatypeFactory DATATYPE_FACTORY;
	private static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
	public static final JsonDeserializer JSON_DESERIALIZER = new JsonDeserializer();
	private static final Encoder BASE64URL_ENCODER = Base64.getUrlEncoder();
	private static final Decoder BASE64URL_DECODER = Base64.getUrlDecoder();
	private static final JsonMapper MAPPER = JsonMapper.builder()
															.addModule(new JavaTimeModule())
															.build();
	
	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		}
		catch ( DatatypeConfigurationException e ) {
			throw new AssertionError("" + e);
		}
	}
	
	public static JsonMapper getJsonMapper() {
		return MAPPER;
	}
	
	public static String toJsonString(JsonNode node) {
		try {
			return MAPPER.writeValueAsString(node);
		}
		catch ( JsonProcessingException e ) {
			throw new InternalException("" + e);
		}
	}

	public static String encodeBase64UrlSafe(String src) {
		if ( Objects.isNull(src) ) {
			return null;
		}
		else {
			return BASE64URL_ENCODER.encodeToString(src.getBytes(StandardCharsets.UTF_8));
		}
	}

	public static String decodeBase64UrlSafe(String src) {
		if ( Objects.isNull(src) ) {
			return null;
		}
		else {
			return new String(BASE64URL_DECODER.decode(src), StandardCharsets.UTF_8);
		}
	};
	
	public static JsonSerializer getJsonSerializer() {
		return JSON_SERIALIZER;
	}
	
	public static JsonDeserializer getJsonDeserializer() {
		return JSON_DESERIALIZER;
	}
	
	public static JsonNode readJsonNode(File jsonFile) {
		try {
			return MAPPER.readTree(jsonFile);
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to parse JSON: file=%s, cause=%s", jsonFile, e);
			throw new InternalException(msg);
		}
	}
	
	public static JsonNode readJsonNode(String jsonString) {
		try {
			return MAPPER.readTree(jsonString);
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to parse JSON: string=%s, cause=%s", jsonString, e);
			throw new InternalException(msg);
		}
	}
	
	public static <T> T readJson(String json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, str=%s, cause=%s", cls, json, e);
			throw new InternalException(msg);
		}
	}
	
	public static <T> T readJson(JsonNode json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, JsonNode=%s, cause=%s", cls, json, e);
			throw new InternalException(msg);
		}
	}
	
	public static <T> T readJson(File jsonFile, Class<T> cls) {
		try ( InputStream is = new FileInputStream(jsonFile) ) {
			return JSON_DESERIALIZER.read(is, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, file=%s, cause=%s", cls, jsonFile, e);
			throw new InternalException(msg);
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to parse JSON: class=%s, file=%s, cause=%s", cls, jsonFile, e);
			throw new InternalException(msg);
		}
	}
	
	public static <T> List<T> readListJson(String json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.readList(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON (List): class=%s, str=%s, cause=%s",
										cls, json, e);
			throw new InternalException(msg);
		}
	}
	
	public static <T> List<T> readListJson(JsonNode json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.readList(json, cls);
		}
		catch ( DeserializationException e ) {
			String msg = String.format("Failed to parse JSON (List): class=%s, JsonNode=%s, cause=%s",
										cls, json, e);
			throw new InternalException(msg);
		}
	}
	
	public static String writeJson(Object modelObj) {
		try {
			return JSON_SERIALIZER.write(modelObj);
		}
		catch ( SerializationException e ) {
			String msg = String.format("Failed to writeJson: %s, cause=%s", modelObj, e);
			throw new InternalException(msg);
		}
	}
	
	public static String writeJson(SubmodelElementValue smev) {
		try {
			return JSON_SERIALIZER.write(smev.toJsonObject());
		}
		catch ( SerializationException e ) {
			String msg = String.format("Failed to writeJson: value=%s, cause=%s", smev, e);
			throw new InternalException(msg);
		}
	}

	public static Environment readEnvironment(File aasEnvFile)
		throws IOException, ResourceAlreadyExistsException, ResourceNotFoundException {
		JsonDeserializer deser = new JsonDeserializer();
		
		try ( FileInputStream fis = new FileInputStream(aasEnvFile) ) {
			Environment env = deser.read(fis, Environment.class);
			if ( env.getAssetAdministrationShells().size() > 1
				|| env.getAssetAdministrationShells().size() == 0 ) {
				throw new MDTInstanceManagerException("Not supported: Multiple AAS descriptors in the Environment");
			}
			
			Set<String> submodelIds = Sets.newHashSet();
			for ( Submodel submodel: env.getSubmodels() ) {
				if ( submodelIds.contains(submodel.getId()) ) {
					throw new ResourceAlreadyExistsException("Submodel", submodel.getId());
				}
				submodelIds.add(submodel.getId());
			}
			
			AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
			for ( Reference ref: aas.getSubmodels() ) {
				String refId = ref.getKeys().get(0).getValue();
				if ( !submodelIds.contains(refId) ) {
					throw new ResourceNotFoundException("Submodel", refId);
				}
			}
			
			return env;
		}
		catch ( DeserializationException e ) {
			throw new MDTInstanceManagerException("failed to parse Environment: file=" + aasEnvFile);
		}
	}
	
	public static class NULLClass { }
}
