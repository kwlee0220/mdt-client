package mdt.sample;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import utils.async.AsyncResult;

import mdt.client.operation.ProcessBasedMDTOperation;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestProcessBasedMDTOperation {
	private static final String COMMAND = "C://Users//kwlee//AppData//Local//Programs//Microsoft VS Code//Code";
	
	public static final void main(String... args) throws Exception {
		ProcessBasedMDTOperation opClient
					= ProcessBasedMDTOperation.builder()
								.setCommand(COMMAND)
								.setWorkingDirectory(new File("C:\\Temp\\mdt\\simulator\\workspace"))
								.addFileArgument("arg1", "111", false)
								.addFileArgument("arg2", "222", false)
								.addFileArgument("arg3", null, true)
								.setTimeout(Duration.ofSeconds(7))
								.build();
//		Map<String,String> result = opClient.run();
		opClient.start();
		AsyncResult<Map<String,String>> result = opClient.waitForFinished(5, TimeUnit.SECONDS);
		if ( result.isRunning() ) {
			System.out.println("Cancelling...");
			opClient.cancel(true);
			result = opClient.poll();
		}
		System.out.println(result);
	}
}
