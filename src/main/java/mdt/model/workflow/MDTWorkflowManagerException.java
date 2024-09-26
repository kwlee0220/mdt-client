package mdt.model.workflow;

import mdt.model.MDTException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTWorkflowManagerException extends MDTException {
	private static final long serialVersionUID = 1L;

	public MDTWorkflowManagerException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public MDTWorkflowManagerException(String details) {
		super(details);
	}
	
	public MDTWorkflowManagerException(Throwable cause) {
		super(cause);
	}
}
