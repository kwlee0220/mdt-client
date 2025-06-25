package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.junit.Assert;
import org.junit.Test;

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

		Assert.assertEquals(JSON, value.toJsonString());
		Assert.assertEquals(VALUE_JSON, value.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assert.assertTrue(value instanceof MultiLanguagePropertyValue);
		MultiLanguagePropertyValue mlpValue = (MultiLanguagePropertyValue)value;
		
		List<LangStringTextType> texts = mlpValue.getLangTextAll();
		Assert.assertEquals(2, texts.size());
		Assert.assertEquals("en", texts.get(0).getLanguage());
		Assert.assertEquals("Hello", texts.get(0).getText());
		Assert.assertEquals("ko", texts.get(1).getLanguage());
		Assert.assertEquals("안녕하세요", texts.get(1).getText());
	}
}
