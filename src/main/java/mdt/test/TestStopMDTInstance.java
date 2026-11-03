package mdt.test;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.client.instance.StopMDTInstances;

/**
 * {@code StopMDTInstances}로 지정된 MDTInstance들을 (의존 인스턴스까지 재귀적으로) 병렬 종료하는
 * 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestStopMDTInstance {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
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
