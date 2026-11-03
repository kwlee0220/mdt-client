package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

/**
 * 파일 {@code misc/aas/shell_test.json}을 AAS4J {@code JsonDeserializer}로 읽어
 * {@code AssetAdministrationShell}로 파싱되는지 확인하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAASParser {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		File file = new File("misc/aas/shell_test.json");
		try ( FileInputStream fis = new FileInputStream(file) ) {
			JsonDeserializer deser = new JsonDeserializer();
			AssetAdministrationShell shell = deser.read(fis, AssetAdministrationShell.class);
			
			System.out.println(shell);
		}
	}
}
