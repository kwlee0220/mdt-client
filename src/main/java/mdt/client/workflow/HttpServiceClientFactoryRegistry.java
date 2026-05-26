package mdt.client.workflow;

import java.util.Map;

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
}
