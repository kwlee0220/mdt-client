package mdt.workflow;

import mdt.model.MDTException;

/**
 * 워크플로우 관리자 작업 중 오류가 발생한 경우 사용하는 예외.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTWorkflowManagerException extends MDTException {
	private static final long serialVersionUID = 1L;

	/**
	 * 상세 메시지와 원인 예외를 갖는 예외를 만든다.
	 *
	 * @param details	상세 메시지.
	 * @param cause	원인 예외.
	 */
	public MDTWorkflowManagerException(String details, Throwable cause) {
		super(details, cause);
	}
	
	/**
	 * 상세 메시지를 갖는 예외를 만든다.
	 *
	 * @param details	상세 메시지.
	 */
	public MDTWorkflowManagerException(String details) {
		super(details);
	}
	
	/**
	 * 원인 예외를 감싼 예외를 만든다.
	 *
	 * @param cause	원인 예외.
	 */
	public MDTWorkflowManagerException(Throwable cause) {
		super(cause);
	}
}
