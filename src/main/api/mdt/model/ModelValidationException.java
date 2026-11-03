package mdt.model;

/**
 * 모델(예: MDTInstance 구현체에 포함된 AAS 모델)의 검증에 실패한 경우 발생되는 예외이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ModelValidationException extends MDTException {
    private static final long serialVersionUID = 1L;

	/**
	 * 주어진 메시지로 예외 객체를 생성한다.
	 *
	 * @param msg	예외 메시지.
	 */
	public ModelValidationException(String msg) {
        super(msg);
    }
}
