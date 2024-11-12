package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Lists;

import utils.func.Funcs;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MultiLanguagePropertyValue implements SubmodelElementValue,
															Supplier<List<LangStringTextType>> {
	private List<LangStringTextType> m_langTexts;
	
	public MultiLanguagePropertyValue(List<LangStringTextType> langTexts) {
		m_langTexts = langTexts;
	}
	
	public MultiLanguagePropertyValue() {
		m_langTexts = Lists.newArrayList();
	}
	
	public MultiLanguagePropertyValue(String language, String text) {
		DefaultLangStringTextType langText = new DefaultLangStringTextType();
		langText.setLanguage(language);
		langText.setText(text);
		m_langTexts = List.of(langText);
	}
	
	@Override
	public List<LangStringTextType> get() {
		return m_langTexts;
	}
	
	public void setValue(List<LangStringTextType> values) {
		m_langTexts = values;
	}
	
	public void addLangStringText(final LangStringTextType langText) {
		LangStringTextType replaced = Funcs.replaceFirst(m_langTexts,
														t -> t.getLanguage().equals(langText.getLanguage()),
														langText);
		if ( replaced == null ) {
			m_langTexts.add(langText);
		}
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		
		for ( LangStringTextType langText: m_langTexts ) {
			serializeLangStringTextType(gen, langText);
		}
		
		gen.writeEndArray();
	}
	
	private void serializeLangStringTextType(JsonGenerator gen, LangStringTextType langText) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("language", langText.getLanguage());
		gen.writeStringField("text", langText.getText());
		gen.writeEndObject();
	}
}
