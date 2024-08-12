package mdt.client;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MDTClientException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public MDTClientException(String details) {
		super(details);
	}
	
	public MDTClientException(Throwable cause) {
		super(cause);
	}
}
