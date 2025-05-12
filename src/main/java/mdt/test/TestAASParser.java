package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAASParser {
	public static final void main(String... args) throws Exception {
		File file = new File("misc/aas/shell_test.json");
		try ( FileInputStream fis = new FileInputStream(file) ) {
			JsonDeserializer deser = new JsonDeserializer();
			AssetAdministrationShell shell = deser.read(fis, AssetAdministrationShell.class);
			
			System.out.println(shell);
		}
	}
}
