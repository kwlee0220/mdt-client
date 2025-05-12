package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.func.Funcs;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MultiLanguagePropertyValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:mlprop";
	
	private final List<LangStringTextType> m_langTexts;
	
	public MultiLanguagePropertyValue(List<LangStringTextType> langTexts) {
		m_langTexts = langTexts;
	}
	
	public MultiLanguagePropertyValue(String language, String text) {
		this(List.of(buildLangStringTextType(language, text)));
	}
	
	public List<LangStringTextType> getLangTextAll() {
		return m_langTexts;
	}
	
	public static MultiLanguagePropertyValue parseJsonNode(JsonNode jnode) throws IOException {
		List<LangStringTextType> textList
						= FStream.from(jnode.elements())
									.mapOrThrow(elm -> {
										Map.Entry<String,JsonNode> ent
											= Funcs.getFirst(elm.fields())
													.getOrThrow(() -> new IOException("No language field"));
										return buildLangStringTextType(ent.getKey(), ent.getValue().asText());
									})
									.toList();
		return new MultiLanguagePropertyValue(textList);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		for ( LangStringTextType langText: m_langTexts ) {
			gen.writeStartObject();
			gen.writeStringField(langText.getLanguage(), langText.getText());
			gen.writeEndObject();
		}
		gen.writeEndArray();
	}
	
	public static LangStringTextType buildLangStringTextType(String language, String text) {
		return new DefaultLangStringTextType.Builder()
											.language(language)
											.text(text)
											.build();
	}
}
