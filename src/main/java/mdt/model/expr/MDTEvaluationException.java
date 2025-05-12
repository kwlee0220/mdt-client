package mdt.model.expr;

import mdt.model.MDTException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTEvaluationException extends MDTException {
    private static final long serialVersionUID = 1L;

    public MDTEvaluationException(final String message) {
        super(message);
    }

    public MDTEvaluationException(final String message, Throwable cause) {
        super(message, cause);
    }
}
