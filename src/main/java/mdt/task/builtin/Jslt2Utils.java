package mdt.task.builtin;

import com.fasterxml.jackson.databind.JsonNode;

import jslt2.Jslt2Exception;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Jslt2Utils {
	public static final boolean getBoolean(JsonNode src) {
		return getBoolean(src, false);
	}
	public static final boolean getBoolean(JsonNode src, boolean strict) {
		if ( src.isBoolean() || src.isTextual() || !strict ) {
			return src.asBoolean();
		}
		
        throw new Jslt2Exception("Invalid BooleanNode: " + src);
		
	}
}
