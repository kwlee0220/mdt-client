package mdt.model.resource.value;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;

import com.google.common.collect.Lists;

import utils.func.Funcs;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MultiLanguagePropertyValue implements SubmodelElementValue,
															Supplier<List<LangStringTextType>> {
	private String m_idShort;
	private List<LangStringTextType> m_langTexts;
	
	public MultiLanguagePropertyValue() {
		m_langTexts = Lists.newArrayList();
	}
	
	public MultiLanguagePropertyValue(String language, String text) {
		DefaultLangStringTextType langText = new DefaultLangStringTextType();
		langText.setLanguage(language);
		langText.setText(text);
		m_langTexts = List.of(langText);
	}
	
	public String getIdShort() {
		return m_idShort;
	}
	
	public void setIdShort(String id) {
		m_idShort = id;
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
	public Map<String,Object> toJsonObject() {
		return FStream.from(m_langTexts)
						.toMap(LangStringTextType::getLanguage, LangStringTextType::getText);
	}
}
