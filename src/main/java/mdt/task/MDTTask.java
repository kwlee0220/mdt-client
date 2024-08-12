package mdt.task;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import mdt.model.instance.MDTInstanceManager;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTTask {
	public void setMDTInstanceManager(MDTInstanceManager manager);
	
	public void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
					Map<String,Port> outputPorts, Map<String,String> options)
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException;
}
