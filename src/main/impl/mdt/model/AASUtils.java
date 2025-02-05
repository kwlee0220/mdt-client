package mdt.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Sets;

import utils.Throwables;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.http.OkHttpClientUtils;
import utils.stream.FStream;

import lombok.experimental.UtilityClass;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.instance.MDTInstanceManagerException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class AASUtils {
	public static final DatatypeFactory DATATYPE_FACTORY;
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

	public static AssetAdministrationShell getAssetAdministrationShell(OkHttpClient httpClient, String faastUrl) {
		JsonMapper mapper = MDTModelSerDe.getJsonMapper();
		HttpRESTfulClient client = HttpRESTfulClient.builder()
												.httpClient(httpClient)
												.jsonMapper(mapper)
												.errorEntityDeserializer(new JacksonErrorEntityDeserializer(mapper))
												.build();

		String url = String.format("%s/shells", faastUrl);
		List<AssetAdministrationShell> shells = client.get(url, SHELL_LIST_DESER);
		if ( shells.size() != 1 ) {
			throw new MDTInstanceManagerException("Not supported: Multiple AAS descriptors in the Environment");
		}
		return shells.get(0);
	}
	
	public static SubmodelService newSubmodelService(OkHttpClient httpClient, String faastUrl, String submodelId) {
		String url = DescriptorUtils.toSubmodelServiceEndpointString(faastUrl, submodelId);
		return new HttpSubmodelServiceClient(httpClient, url);
	}
	
	public static Environment readEnvironment(OkHttpClient httpClient, String faastUrl) {
		AssetAdministrationShell shell = getAssetAdministrationShell(httpClient, faastUrl);
		List<Submodel> submodels = FStream.from(shell.getSubmodels())
										.map(ref -> {
											String smId = ref.getKeys().get(0).getValue();
											String smUrl = DescriptorUtils.toSubmodelServiceEndpointString(faastUrl, smId);
											SubmodelService smSvc = new HttpSubmodelServiceClient(httpClient, smUrl);
											return smSvc.getSubmodel();
										})
										.toList();
		
		return new DefaultEnvironment.Builder()
									.assetAdministrationShells(shell)
									.submodels(submodels)
									.build();
	}
	
	public static Environment readEnvironment(File aasEnvFile)
		throws IOException, ModelValidationException, MDTInstanceManagerException {
		JsonDeserializer deser = MDTModelSerDe.getJsonDeserializer();
		
		try ( FileInputStream fis = new FileInputStream(aasEnvFile) ) {
			Environment env = deser.read(fis, Environment.class);
			if ( env.getAssetAdministrationShells().size() > 1
				|| env.getAssetAdministrationShells().size() == 0 ) {
				throw new ModelValidationException("Not supported: Multiple AAS descriptors in the Environment");
			}
			
			Set<String> submodelIds = Sets.newHashSet();
			for ( Submodel submodel: env.getSubmodels() ) {
				if ( submodelIds.contains(submodel.getId()) ) {
					throw new ModelValidationException("Submodel already exists: id=" + submodel.getId());
				}
				submodelIds.add(submodel.getId());
			}
			
			AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
			for ( Reference ref: aas.getSubmodels() ) {
				String refId = ref.getKeys().get(0).getValue();
				if ( !submodelIds.contains(refId) ) {
					throw new ModelValidationException("Submodel is not found: " + refId);
				}
			}
			
			return env;
		}
		catch ( DeserializationException e ) {
			throw new IOException("failed to parse Environment: file=" + aasEnvFile);
		}
		catch ( ModelValidationException e ) {
			throw e;
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new MDTInstanceManagerException("Failed to read Environment: file=" + aasEnvFile, cause);
		}
	}
	
	public static void writeEnvironment(File aasEnvFile, Environment env)
		throws IOException, SerializationException {
		JsonSerializer ser = new JsonSerializer();
		try ( FileOutputStream fos = new FileOutputStream(aasEnvFile) ) {
			ser.write(fos, env);
		}
	}
	
	public ResponseBodyDeserializer<List<AssetAdministrationShell>> SHELL_LIST_DESER
		= new ResponseBodyDeserializer<>() {
				@Override
				public List<AssetAdministrationShell> deserialize(Headers headers, String respBody) throws IOException {
					JsonMapper mapper = JsonMapper.builder().build();
					JsonNode root = mapper.readTree(respBody);
					JsonNode result = root.path("result");
					return MDTModelSerDe.readValueList(result, AssetAdministrationShell.class);
				}
			};
	
	public static class NULLClass { }
	
	public static final void main(String... args) throws Exception {
		OkHttpClient httpClient = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
        String faastUrl = "https://129.254.91.134:19000/api/v3.0";
        Environment env = readEnvironment(httpClient, faastUrl);
        System.out.println(env);
	}
}
