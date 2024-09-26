package mdt.task;

import lombok.experimental.UtilityClass;
import utils.func.FOption;
import utils.func.Funcs;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Tasks {
	public static FOption<Port> findElapsedTimePort(Iterable<Port> outputPorts) {
		return Funcs.findFirst(outputPorts, p -> p.getName().equals(MDTTask.ELAPSED_TIME_PORT_NAME));
	}
}
