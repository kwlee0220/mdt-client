package mdt.test;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import utils.io.FileUtils;

import mdt.model.MDTModelSerDe;

/**
 * {@code $MDT_HOME/models} 아래의 모델 파일을 {@code MDTModelSerDe}의 JSON deserializer로 읽어
 * {@code Environment}로 파싱하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestEnvironment {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		JsonDeserializer deser = MDTModelSerDe.getJsonDeserializer();

		File modelFile = FileUtils.path(System.getenv("MDT_HOME"), "models", "innercase", "inspector", "model.json");
		try ( FileInputStream fis = new FileInputStream(modelFile) ) {
			Environment env = deser.read(fis, Environment.class);
			System.out.println(env);
		}
	}
}
