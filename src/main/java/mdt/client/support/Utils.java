package mdt.client.support;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;

import utils.CSV;
import utils.KeyValue;
import utils.stream.FStream;

/**
 * mdt-client 내부에서 사용하는 문자열 처리 유틸리티.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Utils {
	private Utils() {
		throw new AssertionError("Should not be called: class=" + Utils.class);
	}
	
	/**
	 * {@code key1=value1;key2=value2} 형식의 문자열을 파싱하여 Map으로 반환한다.
	 * <p>
	 * 각 항목은 {@code ';'}로 구분하고({@link CSV#parseCsv(String, char)}), 첫 {@code '='}를 기준으로 키와 값으로
	 * 나눈 뒤 앞뒤 공백을 제거한다.
	 *
	 * @param paramsString	파싱할 문자열.
	 * @return	키-값 Map.
	 * @throws StringIndexOutOfBoundsException	{@code '='}가 없는 항목이 있는 경우.
	 */
	public static Map<String,String> parseParameters(String paramsString) {
		return CSV.parseCsv(paramsString, ';')
					.mapToKeyValue(part -> {
						int idx = part.indexOf('=');
						return KeyValue.of(part.substring(0, idx).trim(),
											part.substring(idx+1).trim());
					})
					.toMap();
	}

	/**
	 * 다국어 이름 목록의 text들을 {@code ". "}로 연결한 문자열을 반환한다. 언어 정보는 무시된다.
	 *
	 * @param names	다국어 이름 목록.
	 * @return	연결된 문자열. {@code names}가 {@code null}이면 빈 문자열.
	 */
	public static String concatLangStringNameTypes(List<LangStringNameType> names) {
		if ( names != null ) {
			return FStream.from(names)
							.map(name -> name.getText())
							.join(". ");
		}
		else {
			return "";
		}
	}

	/**
	 * 다국어 텍스트 목록의 text들을 {@code ". "}로 연결한 문자열을 반환한다. 언어 정보는 무시된다.
	 *
	 * @param texts	다국어 텍스트 목록.
	 * @return	연결된 문자열. {@code texts}가 {@code null}이면 빈 문자열.
	 */
	public static String concatLangStringTextTypes(List<LangStringTextType> texts) {
		if ( texts != null ) {
			return FStream.from(texts)
							.map(name -> name.getText())
							.join(". ");
		}
		else {
			return "";
		}
	}
}
