package mdt.client.registry;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.Preconditions;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.client.MDTRESTfulClients;
import mdt.aas.SubmodelRegistry;
import mdt.client.HttpMDTServiceProxy;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import mdt.model.MDTModelSerDe;


/**
 * HTTP(REST)를 통해 원격 Submodel 레지스트리 서버의 submodel descriptor들을 관리하는
 * {@link SubmodelRegistry} 클라이언트.
 * <p>
 * 모든 요청은 동기적으로 전송되며, URL 경로에 포함되는 식별자는 Base64 URL-safe로 인코딩된다.
 * 서버 오류는 {@link MDTRESTfulClients}에 등록된 MDT 예외 타입(예: {@link mdt.model.ResourceNotFoundException})
 * 또는 {@link utils.http.RESTfulRemoteException}으로, 통신 오류는 {@link utils.http.RESTfulIOException}으로
 * 전달된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRegistryClient implements SubmodelRegistry, HttpMDTServiceProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	submodel descriptor 레지스트리 endpoint
	 * 					(예: {@code <mdt-url>/aas_registry/submodel-descriptors}).
	 * @throws IllegalArgumentException	{@code endpoint}가 {@code null}인 경우.
	 */
	public HttpSubmodelRegistryClient(OkHttpClient client, String endpoint) {
		Preconditions.checkNotNullArgument(endpoint, "endpoint is null");
		
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
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
		String url = String.format("%s", m_endpoint);
		return m_restfulClient.get(url, m_smDescListDeser);
	}

	@Override
	public List<SubmodelDescriptor>
	getAllSubmodelDescriptorsByIdShort(String idShort) {
		String url = withQuery(m_endpoint, "idShort", idShort);
		return m_restfulClient.get(url, m_smDescListDeser);
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsBySemanticId(String semanticId) {
		String url = withQuery(m_endpoint, "semanticId", semanticId);
		return m_restfulClient.get(url, m_smDescListDeser);
	}

	@Override
	public SubmodelDescriptor getSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(submodelId));

		return m_restfulClient.get(url, m_smDescDeser);
	}

	@Override
	public SubmodelDescriptor postSubmodelDescriptor(SubmodelDescriptor desc) {
		String url = String.format("%s", m_endpoint);
		String reqBodyStr = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(reqBodyStr);
		return m_restfulClient.post(url, reqBody, m_smDescDeser);
	}

	@Override
	public SubmodelDescriptor putSubmodelDescriptor(SubmodelDescriptor descriptor) {
		String url = String.format("%s", m_endpoint);
		String reqBodyStr = MDTModelSerDe.toJsonString(descriptor);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(reqBodyStr);
		return m_restfulClient.put(url, reqBody, m_smDescDeser);
	}

	@Override
	public void deleteSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(submodelId));
		
		m_restfulClient.delete(url);
	}
	
	/**
	 * 주어진 submodel들의 descriptor에 등록된 endpoint 목록을 주어진 submodel 저장소 endpoint 하나로 교체한다.
	 * <p>
	 * 각 descriptor를 읽어와 interface가 {@code SUBMODEL-3.0}인 endpoint로 대체한 후 다시 갱신한다.
	 * 갱신은 submodel별로 순차적으로 수행되며, 도중에 실패하면 이전까지의 갱신은 유지된다.
	 *
	 * @param submodelIdList	대상 submodel 식별자 목록.
	 * @param endpoint	새로 설정할 submodel 저장소 endpoint.
	 */
	public void setSubmodelRepositoryEndpoint(List<String> submodelIdList, String endpoint) {
		for ( String submodelId: submodelIdList ) {
			SubmodelDescriptor desc = getSubmodelDescriptorById(submodelId);
			Endpoint ep = DescriptorUtils.newEndpoint(endpoint, "SUBMODEL-3.0");
			desc.setEndpoints(Arrays.asList(ep));
			
			putSubmodelDescriptor(desc);
		}
	}
	
	@Override
	public String toString() {
		return String.format("SubmodelRegistry: endpoint=%s", m_endpoint);
	}

	private ResponseBodyDeserializer<SubmodelDescriptor> m_smDescDeser = new ResponseBodyDeserializer<>() {
		@Override
		public SubmodelDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, SubmodelDescriptor.class);
		}
	};
	private ResponseBodyDeserializer<List<SubmodelDescriptor>> m_smDescListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<SubmodelDescriptor> deserialize(Headers headers, String respBody)
			throws IOException {
			return MDTModelSerDe.readValueList(respBody, SubmodelDescriptor.class);
		}
	};

	// 쿼리 값에 '#', '&', 공백 등이 있어도 요청이 깨지지 않도록 percent-encoding 한다.
	private static String withQuery(String endpoint, String name, String value) {
		return HttpUrl.get(endpoint).newBuilder().addQueryParameter(name, value).build().toString();
	}
}
