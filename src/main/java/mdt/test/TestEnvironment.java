package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import utils.io.FileUtils;

import mdt.model.MDTModelSerDe;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestEnvironment {
	public static final void main(String... args) throws Exception {
		JsonDeserializer deser = MDTModelSerDe.getJsonDeserializer();

		File modelFile = FileUtils.path(System.getenv("MDT_HOME"), "models", "innercase", "inspector", "model.json");
		try ( FileInputStream fis = new FileInputStream(modelFile) ) {
			Environment env = deser.read(fis, Environment.class);
			System.out.println(env);
		}
	}
}
