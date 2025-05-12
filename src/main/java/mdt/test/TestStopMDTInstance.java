package mdt.test;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.client.instance.StopMDTInstances;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestStopMDTInstance {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		Logger logger = (Logger)LoggerFactory.getLogger("mdt");
		logger.setLevel(Level.INFO);
		
		StopMDTInstances starter = new StopMDTInstances.Builder()
											.mdtInstanceManager(manager)
											.instanceIds("innercase", "test")
											.recursive(true)
											.nthreads(5)
											.build();
		starter.run();
	}
}
