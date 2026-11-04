package mdt.client.registry;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import com.google.common.base.Preconditions;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.client.MDTRESTfulClients;
import mdt.aas.ShellRegistry;
import mdt.client.HttpMDTServiceProxy;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import mdt.model.MDTModelSerDe;


/**
 * HTTP(REST)를 통해 원격 AAS 레지스트리 서버의 shell descriptor들을 관리하는 {@link ShellRegistry} 클라이언트.
 * <p>
 * 모든 요청은 동기적으로 전송되며, URL 경로에 포함되는 식별자는 Base64 URL-safe로 인코딩된다.
 * 서버 오류는 {@link MDTRESTfulClients}에 등록된 MDT 예외 타입(예: {@link mdt.model.ResourceNotFoundException})
 * 또는 {@link utils.http.RESTfulRemoteException}으로, 통신 오류는 {@link utils.http.RESTfulIOException}으로
 * 전달된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpShellRegistryClient implements ShellRegistry, HttpMDTServiceProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	shell descriptor 레지스트리 endpoint (예: {@code <mdt-url>/aas_registry/shell-descriptors}).
	 * @throws IllegalArgumentException	{@code endpoint}가 {@code null}인 경우.
	 */
	public HttpShellRegistryClient(OkHttpClient client, String endpoint) {
		Preconditions.checkArgument(endpoint != null, getClass().getSimpleName() + ": endpoint is null");
		
		m_endpoint = endpoint;
		ErrorEntityDeserializer errorDeser = new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER);
		m_restfulClient = MDTRESTfulClients.builder()
											.httpClient(client)
											.errorEntityDeserializer(errorDeser)
											.build();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String shellId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(shellId));
		return m_restfulClient.get(url, m_shellDeser);
	}

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors() {
		String url = String.format("%s", m_endpoint);
		return m_restfulClient.get(url, m_shellListDeser);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorsByIdShort(String idShort) {
		String url = withQuery(m_endpoint, "idShort", idShort);
		return m_restfulClient.get(url, m_shellListDeser);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorByAssetId(String assetId) {
		String encoded = AASUtils.encodeBase64UrlSafe(assetId);
		String url = String.format("%s/asset/%s", m_endpoint, encoded);

		return m_restfulClient.get(url, m_shellListDeser);
	}

	@Override
	public AssetAdministrationShellDescriptor
	postAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor desc) {
		String url = String.format("%s", m_endpoint);
		
		String reqBodyStr = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(reqBodyStr);
		return m_restfulClient.post(url, reqBody, m_shellDeser);
	}

	@Override
	public AssetAdministrationShellDescriptor
	putAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor descriptor) {
		String url = String.format("%s", m_endpoint);

		String reqBodyStr = MDTModelSerDe.toJsonString(descriptor);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(reqBodyStr);
		return m_restfulClient.put(url, reqBody, m_shellDeser);
	}

	@Override
	public void deleteAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(aasId));
		
		m_restfulClient.delete(url);
	}
	
	/**
	 * 주어진 AAS의 descriptor에 등록된 endpoint 목록을 주어진 AAS 저장소 endpoint 하나로 교체한다.
	 * <p>
	 * descriptor를 읽어와 interface가 {@code AAS-3.0}인 endpoint로 대체한 후 다시 갱신한다.
	 *
	 * @param aasId	대상 AAS 식별자.
	 * @param endpoint	새로 설정할 AAS 저장소 endpoint.
	 */
	public void setAASRepositoryEndpoint(String aasId, String endpoint) {
		AssetAdministrationShellDescriptor desc = getAssetAdministrationShellDescriptorById(aasId);
		
		Endpoint ep = DescriptorUtils.newEndpoint(endpoint, "AAS-3.0");
		desc.setEndpoints(Arrays.asList(ep));
		
		putAssetAdministrationShellDescriptor(desc);
	}
	
	@Override
	public String toString() {
		return String.format("AssetAdministrationShellRegistry: endpoint=%s", m_endpoint);
	}

	private ResponseBodyDeserializer<AssetAdministrationShellDescriptor> m_shellDeser
																				= new ResponseBodyDeserializer<>() {
		@Override
		public AssetAdministrationShellDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, AssetAdministrationShellDescriptor.class);
		}
	};
	private ResponseBodyDeserializer<List<AssetAdministrationShellDescriptor>> m_shellListDeser
																				= new ResponseBodyDeserializer<>() {
		@Override
		public List<AssetAdministrationShellDescriptor> deserialize(Headers headers, String respBody)
			throws IOException {
			return MDTModelSerDe.readValueList(respBody, AssetAdministrationShellDescriptor.class);
		}
	};

	// 쿼리 값에 '#', '&', 공백 등이 있어도 요청이 깨지지 않도록 percent-encoding 한다.
	private static String withQuery(String endpoint, String name, String value) {
		return HttpUrl.get(endpoint).newBuilder().addQueryParameter(name, value).build().toString();
	}
}
