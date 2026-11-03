package mdt.client.workflow;

import java.util.Map;

import com.google.common.collect.Maps;

import okhttp3.OkHttpClient;

/**
 * 서비스 인터페이스 클래스별 base URL을 등록해 두는 레지스트리.
 * <p>
 * {@link mdt.client.HttpMDTManager}가 생성 시 하위 서비스들의 base URL을 등록한다.
 * 현재는 등록만 할 뿐 등록된 정보를 조회하거나 클라이언트를 생성하는 기능은 없다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpServiceClientFactoryRegistry {
	@SuppressWarnings("unused")
	private final OkHttpClient m_httpClient;
	private final Map<Class<?>, String> m_serviceBaseUrls = Maps.newHashMap();
	
	/**
	 * 레지스트리 객체를 생성한다.
	 *
	 * @param httpClient	서비스 클라이언트 생성에 사용할 OkHttp 클라이언트 (현재는 사용되지 않는다).
	 */
	public HttpServiceClientFactoryRegistry(OkHttpClient httpClient) {
		m_httpClient = httpClient;
	}
	
	/**
	 * 서비스 클래스의 base URL을 등록한다. 이미 등록된 클래스이면 base URL을 교체한다.
	 *
	 * @param serviceClass	서비스 인터페이스 클래스.
	 * @param baseUrl	서비스의 base URL.
	 */
	public void register(Class<?> serviceClass, String baseUrl) {
		m_serviceBaseUrls.put(serviceClass, baseUrl);
	}
}
