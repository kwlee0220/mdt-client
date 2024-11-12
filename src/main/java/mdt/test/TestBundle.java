package mdt.test;

import java.io.File;

import utils.io.FileUtils;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestBundle {
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		String instId = "welder";
		File model = FileUtils.path(instId, "model.json");
		File conf = FileUtils.path(instId, "config.json");
		String imageId = manager.bundleInstance(instId, null, model, conf);
		System.out.println("bundled image=" + imageId);
	}
}
