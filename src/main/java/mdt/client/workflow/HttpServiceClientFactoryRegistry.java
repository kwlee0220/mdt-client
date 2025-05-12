package mdt.client.workflow;

import java.util.Map;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.google.common.collect.Maps;

import okhttp3.OkHttpClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpServiceClientFactoryRegistry {
	@SuppressWarnings("unused")
	private final OkHttpClient m_httpClient;
	private final Map<Class<?>, String> m_serviceBaseUrls = Maps.newHashMap();
	
	public HttpServiceClientFactoryRegistry(OkHttpClient httpClient) {
		m_httpClient = httpClient;
	}
	
	public void register(Class<?> serviceClass, String baseUrl) {
		m_serviceBaseUrls.put(serviceClass, baseUrl);
	}
	
	public <T> T createClient(Class<T> serviceClass) {
		String baseUrl = m_serviceBaseUrls.get(serviceClass);
		if ( baseUrl == null ) {
			throw new IllegalArgumentException("Unknown service class: " + serviceClass);
		}
		
		RestClient client = RestClient.create(baseUrl);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client))
																	.build();
		return factory.createClient(serviceClass);
	}
}
