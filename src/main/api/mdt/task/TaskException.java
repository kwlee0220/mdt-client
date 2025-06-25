package mdt.task;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@SuppressWarnings("serial")
public class TaskException extends Exception {
	public TaskException(String details) {
		super(details);
	}
	
	public TaskException(Throwable cause) {
		super(cause);
	}
	
	public TaskException(String details, Throwable cause) {
		super(details, cause);
	}
	
	@Override
	public String getMessage() {
		if ( getCause() != null ) {
			return String.format("%s, cause=%s", super.getMessage(), getCause());
		}
		else {
			return super.getMessage();
		}
	}
}
