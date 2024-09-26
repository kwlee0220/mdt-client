package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestEnvironment {
	public static final void main(String... args) throws Exception {
		JsonDeserializer deser = AASUtils.getJsonDeserializer();

		try ( FileInputStream fis = new FileInputStream(new File("C:\\Temp\\mdt\\mdt-instances\\내함_성형_\\model.json")) ) {
			Environment env = deser.read(fis, Environment.class);
			System.out.println(env);
		}
	}
}
