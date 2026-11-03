package mdt.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import utils.http.OkHttpClientUtils;

import mdt.client.registry.HttpShellRegistryClient;
import mdt.client.registry.HttpSubmodelRegistryClient;
import mdt.client.repository.HttpSubmodelRepositoryClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.ServiceFactory;

import okhttp3.OkHttpClient;

/**
 * HTTP 기반 AAS 서비스 클라이언트들을 생성하는 {@link ServiceFactory} 구현.
 * <p>
 * 생성된 모든 클라이언트는 이 팩토리가 만든 하나의 OkHttp 클라이언트(모든 인증서를 신뢰)를 공유한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpServiceFactory implements ServiceFactory {
	private final OkHttpClient m_httpClient;
	
	/**
	 * 팩토리 객체를 생성한다. 모든 인증서를 신뢰하는 OkHttp 클라이언트를 생성한다.
	 *
	 * @throws KeyManagementException	SSL context 초기화에 실패한 경우.
	 * @throws NoSuchAlgorithmException	필요한 SSL 알고리즘을 사용할 수 없는 경우.
	 */
	public HttpServiceFactory() throws KeyManagementException, NoSuchAlgorithmException {
		m_httpClient = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
	}
	
	/**
	 * 생성되는 클라이언트들이 공유하는 OkHttp 클라이언트를 반환한다.
	 *
	 * @return	OkHttp 클라이언트.
	 */
	public OkHttpClient getHttpClient() {
		return m_httpClient;
	}
	
	@Override
	public HttpShellRegistryClient getAssetAdministrationShellRegistry(String endpoint) {
		return new HttpShellRegistryClient(m_httpClient, endpoint);
	}

	@Override
	public HttpSubmodelRegistryClient getSubmodelRegistry(String endpoint) {
		return new HttpSubmodelRegistryClient(m_httpClient, endpoint);
	}

	@Override
	public HttpSubmodelRepositoryClient getSubmodelRepository(String endpoint) {
		return new HttpSubmodelRepositoryClient(m_httpClient, endpoint);
	}

	@Override
	public HttpAASServiceClient getAssetAdministrationShellService(String endpoint) {
		return new HttpAASServiceClient(m_httpClient, endpoint);
	}

	@Override
	public HttpSubmodelServiceClient getSubmodelService(String endpoint) {
		return new HttpSubmodelServiceClient(m_httpClient, endpoint);
	}
}
