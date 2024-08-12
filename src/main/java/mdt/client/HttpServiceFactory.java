package mdt.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import mdt.client.registry.HttpShellRegistryClient;
import mdt.client.registry.HttpSubmodelRegistryClient;
import mdt.client.repository.HttpAASRepositoryClient;
import mdt.client.repository.HttpSubmodelRepositoryClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.ServiceFactory;
import okhttp3.OkHttpClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpServiceFactory implements ServiceFactory {
	private final OkHttpClient m_httpClient;
	
	public HttpServiceFactory() throws KeyManagementException, NoSuchAlgorithmException {
		m_httpClient = SSLUtils.newTrustAllOkHttpClientBuilder().build();
	}
	
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
	public HttpAASRepositoryClient getAssetAdministrationShellRepository(String url) {
		return new HttpAASRepositoryClient(m_httpClient, url);
	}

	@Override
	public HttpSubmodelRepositoryClient getSubmodelRepository(String url) {
		return new HttpSubmodelRepositoryClient(m_httpClient, url);
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
