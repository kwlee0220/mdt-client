package mdt.client;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;

import utils.CSV;
import utils.func.KeyValue;
import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Utils {
	private Utils() {
		throw new AssertionError("Should not be called: class=" + Utils.class);
	}
	
	public static Map<String,String> parseParameters(String paramsString) {
		return CSV.parseCsv(paramsString, ';')
					.mapToKeyValue(part -> {
						int idx = part.indexOf('=');
						return KeyValue.of(part.substring(0, idx).trim(),
											part.substring(idx+1).trim());
					})
					.toMap();
	}

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
