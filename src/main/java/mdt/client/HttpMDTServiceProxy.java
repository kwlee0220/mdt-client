package mdt.client;

import utils.http.HttpClientProxy;

/**
 * HTTP로 원격 MDT 서비스에 접근하는 클라이언트(proxy) 객체임을 나타내는 표식 인터페이스.
 * <p>
 * {@link HttpClientProxy}에 추가로 정의하는 메소드는 없다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface HttpMDTServiceProxy extends HttpClientProxy {
}
