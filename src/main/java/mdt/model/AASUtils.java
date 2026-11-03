package mdt.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
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
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

import utils.Throwables;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.http.OkHttpClientUtils;
import utils.io.FileUtils;
import utils.stream.FStream;

import mdt.client.MDTRESTfulClients;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.instance.MDTInstanceManagerException;


/**
 * AAS 모델과 FA³ST 서버 접근에 관련된 공통 유틸리티 클래스이다.
 * <p>
 * 식별자의 Base64 URL-safe 인코딩, idShortPath 인코딩, Submodel 서비스 클라이언트 생성, descriptor에
 * endpoint 부착, AAS {@link Environment}의 읽기/쓰기 기능을 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class AASUtils {
	/** XML {@code Duration} 등의 생성에 사용하는 공용 {@link DatatypeFactory}. */
	public static final DatatypeFactory DATATYPE_FACTORY;
	private static final Encoder BASE64URL_ENCODER = Base64.getUrlEncoder();
	private static final Decoder BASE64URL_DECODER = Base64.getUrlDecoder();
	
	private AASUtils() {
		throw new AssertionError("Should not be called: class=" + FileUtils.class.getName());
	}
	
	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		}
		catch ( DatatypeConfigurationException e ) {
			throw new AssertionError("" + e);
		}
	}
	
	/**
	 * 주어진 문자열을 UTF-8 바이트로 변환한 뒤 Base64 URL-safe 형식으로 인코딩한다.
	 * <p>
	 * AAS API에서 식별자를 URL 경로에 넣을 때 사용한다.
	 *
	 * @param src	인코딩할 문자열.
	 * @return	인코딩된 문자열. {@code src}가 {@code null}이면 {@code null}.
	 */
	public static String encodeBase64UrlSafe(String src) {
		if ( Objects.isNull(src) ) {
			return null;
		}
		else {
			return BASE64URL_ENCODER.encodeToString(src.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Base64 URL-safe 형식의 문자열을 디코딩하여 UTF-8 문자열로 반환한다.
	 *
	 * @param src	디코딩할 문자열.
	 * @return	디코딩된 문자열. {@code src}가 {@code null}이면 {@code null}.
	 * @throws IllegalArgumentException	{@code src}가 올바른 Base64 형식이 아닌 경우.
	 */
	public static String decodeBase64UrlSafe(String src) {
		if ( Objects.isNull(src) ) {
			return null;
		}
		else {
			return new String(BASE64URL_DECODER.decode(src), StandardCharsets.UTF_8);
		}
	};
	
	/**
	 * 주어진 idShortPath를 URL 경로에 사용할 수 있도록 UTF-8 기준으로 URL 인코딩한다.
	 *
	 * @param idShortPath	인코딩할 idShortPath.
	 * @return	인코딩된 idShortPath.
	 */
	public static String encodeIdShortPath(String idShortPath) {
		return URLEncoder.encode(idShortPath, StandardCharsets.UTF_8);
	}

	/**
	 * 주어진 FA³ST 서버에 등록된 AAS를 조회하여 반환한다.
	 * <p>
	 * {@code <faastUrl>/shells}를 호출하며, 서버에는 AAS가 정확히 하나 등록되어 있어야 한다.
	 *
	 * @param httpClient	HTTP 요청에 사용할 클라이언트.
	 * @param faastUrl	FA³ST 서버의 API endpoint (예: {@code https://host:port/api/v3.0}).
	 * @return	조회된 AAS 객체.
	 * @throws MDTInstanceManagerException	등록된 AAS가 하나가 아닌 경우.
	 */
	public static AssetAdministrationShell getAssetAdministrationShell(OkHttpClient httpClient, String faastUrl) {
		JsonMapper mapper = MDTModelSerDe.getJsonMapper();
		HttpRESTfulClient client = MDTRESTfulClients.builder()
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

	/**
	 * 주어진 Submodel 서비스 endpoint에 접속하는 {@link SubmodelService} 클라이언트를 생성한다.
	 * <p>
	 * 모든 서버 인증서를 신뢰하는 HTTP 클라이언트를 새로 생성하여 사용한다.
	 *
	 * @param submodelServiceEndpoint	Submodel 서비스 endpoint.
	 * @return	생성된 {@link SubmodelService} 객체.
	 * @throws IOException	HTTP 클라이언트 생성에 실패한 경우.
	 */
	public static SubmodelService newSubmodelService(String submodelServiceEndpoint) throws IOException {
		try {
			OkHttpClient httpClient = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
			return new HttpSubmodelServiceClient(httpClient, submodelServiceEndpoint);
		}
		catch ( Exception e ) {
			throw new IOException("failed to create an OkHttpClient: cause=" + e);
		}
	}
	/**
	 * 주어진 HTTP 클라이언트로 Submodel 서비스 endpoint에 접속하는 {@link SubmodelService} 클라이언트를 생성한다.
	 *
	 * @param httpClient	HTTP 요청에 사용할 클라이언트.
	 * @param submodelServiceEndpoint	Submodel 서비스 endpoint.
	 * @return	생성된 {@link SubmodelService} 객체.
	 */
	public static SubmodelService newSubmodelService(OkHttpClient httpClient, String submodelServiceEndpoint) {
		return new HttpSubmodelServiceClient(httpClient, submodelServiceEndpoint);
	}
	
	/**
	 * FA³ST 서버 endpoint와 Submodel 식별자로 Submodel 서비스 클라이언트를 생성한다.
	 * <p>
	 * Submodel 서비스 endpoint는 {@link #toSubmodelServiceEndpointString(String, String)}로 만든다.
	 *
	 * @param httpClient	HTTP 요청에 사용할 클라이언트.
	 * @param faastUrl	FA³ST 서버의 API endpoint.
	 * @param submodelId	Submodel 식별자.
	 * @return	생성된 {@link SubmodelService} 객체.
	 */
	public static SubmodelService newSubmodelService(OkHttpClient httpClient, String faastUrl, String submodelId) {
		String url = AASUtils.toSubmodelServiceEndpointString(faastUrl, submodelId);
		return new HttpSubmodelServiceClient(httpClient, url);
	}

	/**
	 * 서비스 endpoint와 Submodel 식별자로부터 Submodel 서비스 endpoint를 만든다.
	 * <p>
	 * 결과는 {@code <instanceServiceEndpoint>/submodels/<Base64 URL-safe 인코딩된 submodelId>} 형식이다.
	 *
	 * @param instanceServiceEndpoint	MDTInstance(FA³ST) 서비스 endpoint.
	 * @param submodelId	Submodel 식별자.
	 * @return	Submodel 서비스 endpoint. {@code instanceServiceEndpoint}가 {@code null}이면 {@code null}.
	 */
	public static String toSubmodelServiceEndpointString(String instanceServiceEndpoint, String submodelId) {
		if ( instanceServiceEndpoint != null ) {
			String encodedSubmodelId = encodeBase64UrlSafe(submodelId);
			return String.format("%s/submodels/%s", instanceServiceEndpoint, encodedSubmodelId);
		}
		else {
			return null;
		}
	}
	
	/**
	 * 주어진 AAS descriptor와 그에 포함된 모든 Submodel descriptor에 서비스 endpoint를 설정한다.
	 * <p>
	 * AAS descriptor에는 {@link DescriptorUtils#toAASServiceEndpointString(String, String)}로 만든
	 * {@code "AAS-3.0"} 인터페이스 endpoint가, 각 Submodel descriptor에는
	 * {@link #attachEndpoint(SubmodelDescriptor, String)}로 만든 endpoint가 기존 endpoint를 대체하여 설정된다.
	 * 인자로 주어진 descriptor 객체 자체가 변경된다.
	 *
	 * @param shell	대상 AAS descriptor.
	 * @param serviceEndpoint	MDTInstance 서비스 endpoint.
	 * @return	endpoint가 설정된 {@code shell} 객체.
	 * @throws IllegalArgumentException	{@code serviceEndpoint}가 {@code null}인 경우.
	 */
	public static AssetAdministrationShellDescriptor attachEndpoint(AssetAdministrationShellDescriptor shell,
																	String serviceEndpoint) {
		Preconditions.checkArgument(serviceEndpoint != null, "serviceEndpoint is null");
		
		if ( serviceEndpoint != null ) {
			String aasEp = DescriptorUtils.toAASServiceEndpointString(serviceEndpoint, shell.getId());
			shell.setEndpoints(DescriptorUtils.newEndpoints(aasEp, "AAS-3.0"));
			
			for ( SubmodelDescriptor smDesc: shell.getSubmodelDescriptors() ) {
				attachEndpoint(smDesc, serviceEndpoint);
			}
		}
		
		return shell;
	}
	
	/**
	 * 주어진 Submodel descriptor에 서비스 endpoint를 설정한다.
	 * <p>
	 * {@link #toSubmodelServiceEndpointString(String, String)}로 만든 {@code "SUBMODEL-3.0"} 인터페이스
	 * endpoint가 기존 endpoint를 대체한다. 인자로 주어진 descriptor 객체 자체가 변경된다.
	 *
	 * @param smDesc	대상 Submodel descriptor.
	 * @param serviceEndpoint	MDTInstance 서비스 endpoint.
	 * @return	endpoint가 설정된 {@code smDesc} 객체.
	 * @throws IllegalArgumentException	{@code serviceEndpoint}가 {@code null}인 경우.
	 */
	public static SubmodelDescriptor attachEndpoint(SubmodelDescriptor smDesc, String serviceEndpoint) {
		Preconditions.checkArgument(serviceEndpoint != null, "serviceEndpoint is null");
		
		String smEp = AASUtils.toSubmodelServiceEndpointString(serviceEndpoint ,smDesc.getId());
		smDesc.setEndpoints(DescriptorUtils.newEndpoints(smEp, "SUBMODEL-3.0"));
		
		return smDesc;
	}
	
	/**
	 * 주어진 FA³ST 서버로부터 AAS와 그 AAS가 참조하는 모든 Submodel을 읽어 {@link Environment}를 구성한다.
	 * <p>
	 * 서버에는 AAS가 정확히 하나 등록되어 있어야 하며, 각 Submodel은 개별 Submodel 서비스 호출로 읽는다.
	 *
	 * @param httpClient	HTTP 요청에 사용할 클라이언트.
	 * @param faastUrl	FA³ST 서버의 API endpoint.
	 * @return	읽은 AAS와 Submodel들로 구성된 {@link Environment}.
	 * @throws MDTInstanceManagerException	등록된 AAS가 하나가 아닌 경우.
	 */
	public static Environment readEnvironment(OkHttpClient httpClient, String faastUrl) {
		AssetAdministrationShell shell = getAssetAdministrationShell(httpClient, faastUrl);
		List<Submodel> submodels = FStream.from(shell.getSubmodels())
										.map(ref -> {
											String smId = ref.getKeys().get(0).getValue();
											String smUrl = AASUtils.toSubmodelServiceEndpointString(faastUrl, smId);
											SubmodelService smSvc = new HttpSubmodelServiceClient(httpClient, smUrl);
											return smSvc.getSubmodel();
										})
										.toList();
		
		return new DefaultEnvironment.Builder()
									.assetAdministrationShells(shell)
									.submodels(submodels)
									.build();
	}
	
	/**
	 * 주어진 JSON 파일로부터 AAS {@link Environment}를 읽고 그 구성을 검증한다.
	 * <p>
	 * Environment에는 AAS가 정확히 하나 있어야 하고, Submodel 식별자가 중복되지 않아야 하며, AAS가
	 * 참조하는 모든 Submodel이 Environment에 포함되어 있어야 한다.
	 *
	 * @param aasEnvFile	AAS Environment JSON 파일.
	 * @return	읽은 {@link Environment} 객체.
	 * @throws IOException	파일을 읽지 못했거나 JSON 파싱에 실패한 경우.
	 * @throws ModelValidationException	Environment 구성이 위 조건을 만족하지 않는 경우.
	 * @throws MDTInstanceManagerException	그 밖의 오류가 발생한 경우.
	 */
	public static Environment readEnvironment(File aasEnvFile)
		throws IOException, ModelValidationException, MDTInstanceManagerException {
		JsonDeserializer deser = MDTModelSerDe.getJsonDeserializer();
		
		try ( FileInputStream fis = new FileInputStream(aasEnvFile) ) {
			Environment env = deser.read(fis, Environment.class);
			int naas = env.getAssetAdministrationShells().size();
			if ( naas == 0 ) {
				throw new ModelValidationException("No AAS in the Environment: file=" + aasEnvFile);
			}
			else if ( naas > 1 ) {
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
			throw new IOException("failed to parse Environment: file=" + aasEnvFile, e);
		}
		catch ( ModelValidationException | IOException e ) {
			throw e;
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new MDTInstanceManagerException("Failed to read Environment: file=" + aasEnvFile, cause);
		}
	}
	
	/**
	 * 주어진 AAS {@link Environment}를 JSON 형식으로 파일에 기록한다.
	 *
	 * @param aasEnvFile	기록할 파일. 이미 존재하는 경우 덮어쓴다.
	 * @param env	기록할 Environment 객체.
	 * @throws IOException	파일 입출력 오류가 발생한 경우.
	 * @throws SerializationException	JSON 직렬화에 실패한 경우.
	 */
	public static void writeEnvironment(File aasEnvFile, Environment env)
		throws IOException, SerializationException {
		JsonSerializer ser = new JsonSerializer();
		try ( FileOutputStream fos = new FileOutputStream(aasEnvFile) ) {
			ser.write(fos, env);
		}
	}
	
	/** FA³ST {@code /shells} 응답 본문의 {@code result} 배열을 AAS 리스트로 변환하는 deserializer. */
	public static ResponseBodyDeserializer<List<AssetAdministrationShell>> SHELL_LIST_DESER
		= new ResponseBodyDeserializer<>() {
				@Override
				public List<AssetAdministrationShell> deserialize(Headers headers, String respBody) throws IOException {
					JsonMapper mapper = JsonMapper.builder().build();
					JsonNode root = mapper.readTree(respBody);
					JsonNode result = root.path("result");
					return MDTModelSerDe.readValueList(result, AssetAdministrationShell.class);
				}
			};
	
	/** 아무 멤버도 갖지 않는 빈 클래스. 클래스 타입 인자의 자리 표시(placeholder) 용도이다. */
	public static class NULLClass { }
	
	/**
	 * 고정된 FA³ST 서버 주소에서 Environment를 읽어 출력하는 시험용 메소드.
	 *
	 * @param args	사용되지 않음.
	 * @throws Exception	Environment 읽기에 실패한 경우.
	 */
	public static final void main(String... args) throws Exception {
		OkHttpClient httpClient = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
        String faastUrl = "https://129.254.91.134:19000/api/v3.0";
        Environment env = readEnvironment(httpClient, faastUrl);
        System.out.println(env);
	}
}
