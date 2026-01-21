package mdt.workflow;

import mdt.model.MDTException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTWorkflowInstanceManagerException extends MDTException {
	private static final long serialVersionUID = 1L;

	public MDTWorkflowInstanceManagerException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public MDTWorkflowInstanceManagerException(String details) {
		super(details);
	}
	
	public MDTWorkflowInstanceManagerException(Throwable cause) {
		super(cause);
	}
}
