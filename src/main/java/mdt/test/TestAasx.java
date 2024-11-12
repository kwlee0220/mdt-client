package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAasx {
	public static final void main(String... args) throws Exception {
		File file = new File("/home/kwlee/Downloads/01_Festo.aasx");
		try ( FileInputStream fis = new FileInputStream(file) ) {
			AASXDeserializer deser = new AASXDeserializer(fis);
			Environment env = deser.read();
			
			System.out.println(env);
		}
	}
}
