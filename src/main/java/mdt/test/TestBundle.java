package mdt.test;

import java.io.File;

import utils.io.FileUtils;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;

/**
 * MDTInstance 모델/설정 파일을 이미지로 번들링하는 {@code bundleInstance} 기능을 시험하는 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestBundle {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		String instId = "welder";
		File model = FileUtils.path(instId, "model.json");
		File conf = FileUtils.path(instId, "config.json");
		String imageId = manager.bundleInstance(instId, null, model, conf);
		System.out.println("bundled image=" + imageId);
	}
}
