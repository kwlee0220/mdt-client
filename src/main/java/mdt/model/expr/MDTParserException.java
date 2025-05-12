package mdt.model.expr;

import mdt.model.MDTException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParserException extends MDTException {
    private static final long serialVersionUID = 1L;

    public MDTParserException(final String message) {
        super(message);
    }

    public MDTParserException(final String message, Throwable cause) {
        super(message, cause);
    }
}
