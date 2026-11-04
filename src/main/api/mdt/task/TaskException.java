package mdt.task;

/**
 * {@link MDTTask} 수행이 실패한 경우 발생하는 예외.
 * <p>
 * 원인 예외가 있으면 {@link #getMessage()}가 원인을 덧붙인 메시지를 돌려준다. 메시지에 원인이 이미 들어 있으면
 * 다시 붙이지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@SuppressWarnings("serial")
public class TaskException extends Exception {
	/**
	 * 상세 메시지를 갖는 예외를 만든다.
	 *
	 * @param details	상세 메시지.
	 */
	public TaskException(String details) {
		super(details);
	}
	
	/**
	 * 원인 예외를 감싼 예외를 만든다.
	 *
	 * @param cause	원인 예외.
	 */
	public TaskException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 상세 메시지와 원인 예외를 갖는 예외를 만든다.
	 *
	 * @param details	상세 메시지.
	 * @param cause	원인 예외.
	 */
	public TaskException(String details, Throwable cause) {
		super(details, cause);
	}
	
	/**
	 * 예외 메시지를 반환한다. 원인 예외가 있으면 {@code "<메시지>, cause=<원인>"} 형식으로 덧붙인다.
	 * <p>
	 * 메시지에 원인의 {@link Throwable#toString()}이 이미 들어 있으면 덧붙이지 않는다.
	 * {@link #TaskException(Throwable)}는 메시지가 곧 원인 문자열이고, 호출자가 메시지에 원인을 직접 넣는 경우도 있다
	 * (예: {@code new TaskException("failed: cause=" + e, e)}).
	 *
	 * @return	예외 메시지.
	 */
	@Override
	public String getMessage() {
		String msg = super.getMessage();
		Throwable cause = getCause();
		if ( cause == null ) {
			return msg;
		}
		
		String causeStr = cause.toString();
		if ( msg != null && msg.contains(causeStr) ) {
			return msg;
		}
		return String.format("%s, cause=%s", msg, causeStr);
	}
}
