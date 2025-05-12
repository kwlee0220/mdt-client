package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MultiLanguagePropertyValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String VALUE_JSON
		= "[{\"en\":\"Hello\"},{\"ko\":\"안녕하세요\"}]";
	@Test
	public void serializeNamedProperty() throws JsonProcessingException {
		var texts = List.of(MultiLanguagePropertyValue.buildLangStringTextType("en", "Hello"),
				            MultiLanguagePropertyValue.buildLangStringTextType("ko", "안녕하세요"));
		MultiLanguagePropertyValue value = new MultiLanguagePropertyValue(texts);
		
		String json = m_mapper.writeValueAsString(value);
		Assert.assertEquals(VALUE_JSON, json);
	}

	@Test
	public void testParseJsonNode() throws IOException {
		MultiLanguagePropertyValue value = MultiLanguagePropertyValue.parseJsonNode(m_mapper.readTree(VALUE_JSON));
		
		List<LangStringTextType> texts = value.getLangTextAll();
		Assert.assertEquals(2, texts.size());
		Assert.assertEquals("en", texts.get(0).getLanguage());
		Assert.assertEquals("Hello", texts.get(0).getText());
		Assert.assertEquals("ko", texts.get(1).getLanguage());
		Assert.assertEquals("안녕하세요", texts.get(1).getText());
	}
}
