package mdt.model;


/**
 * MDT 프레임워크에서 발생되는 비검사 예외들의 최상위 클래스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 주어진 상세 메시지와 원인 예외로 예외 객체를 생성한다.
	 * <p>
	 * 예외 메시지는 {@code "<details>, cause=<cause>"} 형식으로 구성된다.
	 *
	 * @param details	상세 메시지.
	 * @param cause	원인 예외.
	 */
	public MDTException(String details, Throwable cause) {
		super(String.format("%s, cause=%s", details, cause), cause);
	}
	
	/**
	 * 주어진 상세 메시지로 예외 객체를 생성한다.
	 *
	 * @param details	상세 메시지.
	 */
	public MDTException(String details) {
		super(details);
	}
	
	/**
	 * 주어진 원인 예외로 예외 객체를 생성한다.
	 *
	 * @param cause	원인 예외.
	 */
	public MDTException(Throwable cause) {
		super(cause);
	}
}
