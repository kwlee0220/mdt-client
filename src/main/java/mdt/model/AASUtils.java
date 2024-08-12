package mdt.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
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
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import utils.InternalException;

import lombok.experimental.UtilityClass;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.registry.ResourceAlreadyExistsException;
import mdt.model.registry.ResourceNotFoundException;
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
	
	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		}
		catch ( DatatypeConfigurationException e ) {
			throw new AssertionError("" + e);
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
	
	public static <T> T readJson(String json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			throw new InternalException("Failed to parse JSON: " + json);
		}
	}
	
	public static <T> T readJson(JsonNode json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			throw new InternalException("Failed to parse JSON: " + json);
		}
	}
	
	public static String writeJson(Object modelObj) {
		try {
			return JSON_SERIALIZER.write(modelObj);
		}
		catch ( SerializationException e ) {
			throw new InternalException("Failed to write to JSON: " + modelObj);
		}
	}
	
	public static String writeJson(SubmodelElementValue smev) {
		try {
			return JSON_SERIALIZER.write(smev.toJsonObject());
		}
		catch ( SerializationException e ) {
			throw new InternalException("Failed to write to JSON: " + smev);
		}
	}
	
	public static String toJson(SubmodelElement sme) {
		try {
			return JSON_SERIALIZER.write(sme);
		}
		catch ( SerializationException e ) {
			throw new InternalException("Failed to serialize SubmodelElement: " + sme);
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
