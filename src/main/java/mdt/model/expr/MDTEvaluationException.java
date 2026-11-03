package mdt.model.expr;

import mdt.model.MDTException;

/**
 * MDT 표현식 평가 중 오류가 발생한 경우 발생되는 예외이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTEvaluationException extends MDTException {
    private static final long serialVersionUID = 1L;

    /**
     * 주어진 메시지로 예외 객체를 생성한다.
     *
     * @param message	예외 메시지.
     */
    public MDTEvaluationException(final String message) {
        super(message);
    }

    /**
     * 주어진 메시지와 원인 예외로 예외 객체를 생성한다.
     *
     * @param message	예외 메시지.
     * @param cause	원인 예외.
     */
    public MDTEvaluationException(final String message, Throwable cause) {
        super(message, cause);
    }
}
