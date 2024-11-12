package mdt.task;

import utils.func.FOption;


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
		String causeStr = FOption.mapOrElse(getCause(), c -> ", cause=" + c, "");
		return String.format("%s%s", super.getMessage(), causeStr);
	}
}
