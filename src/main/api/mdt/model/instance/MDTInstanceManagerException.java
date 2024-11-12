package mdt.model.instance;

import mdt.model.MDTException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceManagerException extends MDTException {
	private static final long serialVersionUID = 1L;

	public MDTInstanceManagerException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public MDTInstanceManagerException(String details) {
		super(details);
	}
	
	public MDTInstanceManagerException(Throwable cause) {
		super(cause);
	}
}
