package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MultiLanguagePropertyValueTest {
	private static final String JSON
		= "{\"@type\":\"mdt:value:mlprop\",\"value\":[{\"en\":\"Hello\"},{\"ko\":\"안녕하세요\"}]}";
	private static final String VALUE_JSON
		= "[{\"en\":\"Hello\"},{\"ko\":\"안녕하세요\"}]";
	
	@Test
	public void serializeNamedProperty() throws IOException {
		var texts = List.of(MultiLanguagePropertyValue.buildLangStringTextType("en", "Hello"),
				            MultiLanguagePropertyValue.buildLangStringTextType("ko", "안녕하세요"));
		MultiLanguagePropertyValue value = new MultiLanguagePropertyValue(texts);

		Assertions.assertEquals(JSON, value.toJsonString());
		Assertions.assertEquals(VALUE_JSON, value.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assertions.assertTrue(value instanceof MultiLanguagePropertyValue);
		MultiLanguagePropertyValue mlpValue = (MultiLanguagePropertyValue)value;
		
		List<LangStringTextType> texts = mlpValue.getLangTextAll();
		Assertions.assertEquals(2, texts.size());
		Assertions.assertEquals("en", texts.get(0).getLanguage());
		Assertions.assertEquals("Hello", texts.get(0).getText());
		Assertions.assertEquals("ko", texts.get(1).getLanguage());
		Assertions.assertEquals("안녕하세요", texts.get(1).getText());
	}
}
