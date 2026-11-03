package mdt.model;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import utils.stream.KeyValueFStream;

import lombok.experimental.UtilityClass;


/**
 * 문자열 내 {@code ${name}} 형태의 변수를 프로세스 환경 변수 값 등으로 치환하는 유틸리티 클래스이다.
 * <p>
 * 클래스 초기화 시점의 환경 변수({@link System#getenv()})를 치환 테이블로 사용하며, 값에 포함된
 * {@code '\'}는 {@code '/'}로 바뀌어 저장된다 (Windows 경로가 JSON escape 문자로 해석되는 것을 막기 위함).
 * {@link #addMapping(String, String)}으로 치환 항목을 추가할 수 있다.
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
		MDT_ENV_VARS = KeyValueFStream.from(System.getenv())
								.mapValue(v -> v.replaceAll("\\\\", "/"))
								.toMap();
		SUBSTITUTOR = new StringSubstitutor(MDT_ENV_VARS);
	}
	
	/**
	 * 주어진 템플릿 문자열의 변수들을 치환한다.
	 * <p>
	 * 치환 테이블에 없는 변수는 그대로 남는다.
	 *
	 * @param template	치환할 템플릿 문자열.
	 * @return	치환된 문자열.
	 */
	public static String substibute(String template) {
		return SUBSTITUTOR.replace(template);
	}
	
	/**
	 * 주어진 객체를 JSON 문자열로 직렬화하여 변수를 치환한 뒤, 같은 클래스의 객체로 다시 역직렬화한다.
	 * <p>
	 * 직렬화/역직렬화에는 {@link MDTModelSerDe}를 사용한다.
	 *
	 * @param <T>	대상 객체 타입.
	 * @param obj	치환할 객체.
	 * @return	변수가 치환된 새 객체.
	 * @throws IOException	JSON 직렬화 또는 역직렬화에 실패한 경우.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T substituteJsonObject(T obj) throws IOException {
		String template = MDTModelSerDe.toJsonString(obj);
		String substituted = substibute(template);
		return MDTModelSerDe.readValue(substituted, (Class<T>)obj.getClass());
	}
	
	/**
	 * 치환 테이블에 항목을 추가한다.
	 * <p>
	 * 같은 이름의 항목이 있으면 대체된다. 이후의 치환에 반영된다.
	 *
	 * @param name	변수 이름.
	 * @param value	치환될 값.
	 */
	public static void addMapping(String name, String value) {
		MDT_ENV_VARS.put(name, value);
		SUBSTITUTOR = new StringSubstitutor(MDT_ENV_VARS);
	}
}
