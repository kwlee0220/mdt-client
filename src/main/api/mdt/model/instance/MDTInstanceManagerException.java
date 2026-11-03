package mdt.model.instance;

import mdt.model.MDTException;

/**
 * {@link MDTInstanceManager}의 작업 수행 중 발생한 오류를 나타내는 예외.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceManagerException extends MDTException {
	private static final long serialVersionUID = 1L;

	/**
	 * 상세 메시지와 원인 예외를 갖는 예외 객체를 생성한다.
	 *
	 * @param details	상세 메시지.
	 * @param cause		원인 예외.
	 */
	public MDTInstanceManagerException(String details, Throwable cause) {
		super(details, cause);
	}
	
	/**
	 * 상세 메시지를 갖는 예외 객체를 생성한다.
	 *
	 * @param details	상세 메시지.
	 */
	public MDTInstanceManagerException(String details) {
		super(details);
	}
	
	/**
	 * 원인 예외를 갖는 예외 객체를 생성한다.
	 *
	 * @param cause	원인 예외.
	 */
	public MDTInstanceManagerException(Throwable cause) {
		super(cause);
	}
}
