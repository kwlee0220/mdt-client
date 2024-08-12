package mdt.model;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MDTException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public MDTException(String details) {
		super(details);
	}
	
	public MDTException(Throwable cause) {
		super(cause);
	}
}
