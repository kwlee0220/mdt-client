package mdt.client;

import utils.http.HttpRESTfulClient;

import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTException;
import mdt.model.ModelValidationException;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceException;
import mdt.model.ResourceNotFoundException;


/**
 * MDT 서버와 통신하는 {@link HttpRESTfulClient}의 빌더를 만든다.
 * <p>
 * MDT 서버(mdt-manager, mdt-workflow 등)는 오류 응답의 {@code code}에 예외 클래스 이름을 담는다.
 * {@link HttpRESTfulClient}는 보안상 원격 응답이 지정한 클래스를 로딩하지 않고
 * {@link utils.http.RESTfulRemoteException}으로 감싸므로, 그대로 두면 {@link ResourceNotFoundException}
 * 등을 타입으로 잡는 호출부가 동작하지 않는다 (예: {@code mdt add wfmodel --force}가
 * {@link ResourceAlreadyExistsException}을 잡지 못해 덮어쓰지 못했다).
 * <p>
 * 이 빌더는 MDT API의 예외들을 허용 목록으로 등록하여({@link HttpRESTfulClient.Builder#remoteException})
 * 원래 타입으로 복원한다. 등록하지 않은 예외는 기존처럼 {@code RESTfulRemoteException}으로 전달된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MDTRESTfulClients {
	private MDTRESTfulClients() {
		throw new AssertionError("Should not be called: " + getClass());
	}

	/**
	 * MDT API 예외를 복원하도록 설정된 {@link HttpRESTfulClient} 빌더를 반환한다.
	 *
	 * @return	빌더. 나머지 설정(HTTP 클라이언트, JSON 매퍼 등)은 호출자가 이어서 지정한다.
	 */
	public static HttpRESTfulClient.Builder builder() {
		return HttpRESTfulClient.builder()
				.remoteException(ResourceNotFoundException.class.getName(), ResourceNotFoundException::new)
				.remoteException(ResourceAlreadyExistsException.class.getName(), ResourceAlreadyExistsException::new)
				.remoteException(InvalidResourceStatusException.class.getName(), InvalidResourceStatusException::new)
				.remoteException(ResourceException.class.getName(), ResourceException::new)
				.remoteException(ModelValidationException.class.getName(), ModelValidationException::new)
				.remoteException(MDTException.class.getName(), MDTException::new);
	}
}
