package mdt.model;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import lombok.experimental.UtilityClass;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class MDTSubstitutor {
	private static StringSubstitutor SUBSTITUTOR;
	private static Map<String,String> MDT_ENV_VARS;
	
	static {
		// Windows OS의 경우 환경 변수 내 파일 경로명에 포함된 file separator가 '\'이기 때문에
		// 이 값을 이용하여 string substitution을 사용하여 JSON 파일 구성하면
		// 이 '\'가 escape character로 간주되어 문제를 유발한다.
		// 이를 해결하기 위해 '\'를 '/'로 대체시킨다.
		MDT_ENV_VARS = FStream.from(System.getenv())
								.mapValue(v -> v.replaceAll("\\\\", "/"))
								.toMap();
		SUBSTITUTOR = new StringSubstitutor(MDT_ENV_VARS);
	}
	
	public static String substibute(String template) {
		return SUBSTITUTOR.replace(template);
	}
	
	public static <T> T substituteJsonObject(T obj) throws IOException {
		String template = MDTModelSerDe.toJsonString(obj);
		String substituted = substibute(template);
		return MDTModelSerDe.readValue(substituted, (Class<T>)obj.getClass());
	}
	
	public static void addMapping(String name, String value) {
		MDT_ENV_VARS.put(name, value);
		SUBSTITUTOR = new StringSubstitutor(MDT_ENV_VARS);
	}
}
