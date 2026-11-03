package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

/**
 * AASX 패키지 파일을 {@code AASXDeserializer}로 읽어 {@code Environment}로 변환되는지 확인하는
 * 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestAasx {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		File file = new File("/home/kwlee/Downloads/01_Festo.aasx");
		try ( FileInputStream fis = new FileInputStream(file) ) {
			AASXDeserializer deser = new AASXDeserializer(fis);
			Environment env = deser.read();
			
			System.out.println(env);
		}
	}
}
