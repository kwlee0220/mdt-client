package mdt.test;

import java.io.File;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManager;

/**
 * 로컬 MDT 매니저에 모델 디렉토리를 지정하여 MDTInstance를 추가하는 기능을 시험하는 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAddInstanceBundle {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		String instId = "test";
		File instanceDir = new File("/home/kwlee/mdt/models/test");
		HttpMDTInstanceClient inst = manager.addInstance(instId, -1, instanceDir);
		System.out.println("instance: aas-id=" + inst.getAasId());
	}
}
