package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.func.Funcs;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MultiLanguagePropertyValue extends AbstractElementValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:mlprop";
	
	private final List<LangStringTextType> m_langTexts;
	
	public MultiLanguagePropertyValue(List<LangStringTextType> langTexts) {
		Preconditions.checkNotNullArgument(langTexts, "langTexts is null");

		m_langTexts = langTexts;
	}
	
	public MultiLanguagePropertyValue(String language, String text) {
		this(List.of(buildLangStringTextType(language, text)));
	}
	
	public List<LangStringTextType> getLangTextAll() {
		return m_langTexts;
	}
	
	public void update(MultiLanguageProperty mlprop) {
		mlprop.setValue(m_langTexts);
	}

	@Override
	public List<Map<String,String>> toValueObject()  {
		return FStream.from(m_langTexts)
						.map(langText -> {
							if ( langText.getLanguage() == null || langText.getText() == null ) {
								String msg = String.format("Invalid language-text: language=%s, text=%s",
															langText.getLanguage(), langText.getText());
								throw new IllegalStateException(msg);
							}
							return Map.of(langText.getLanguage(), langText.getText());
						})
						.toList();
	}
	
	public static MultiLanguagePropertyValue from(MultiLanguageProperty mlp) {
		return new MultiLanguagePropertyValue(mlp.getValue());
	}
	
	public static MultiLanguagePropertyValue parseValueJsonNode(JsonNode vnode) throws IOException {
		if ( !vnode.isArray() ) {
			throw new IOException("MultiLanguagePropertyValue expects an 'Array' node: JsonNode=" + vnode);
		}
		
		List<LangStringTextType> textList
				= FStream.from(vnode.elements())
							.mapOrThrow(elm -> {
								Map.Entry<String,JsonNode> ent = Funcs.getFirst(elm.properties());
								if ( ent == null ) throw new IllegalStateException("No language field");
								return buildLangStringTextType(ent.getKey(), ent.getValue().asText());
							})
							.toList();
		return new MultiLanguagePropertyValue(textList);
	}
	
	public static MultiLanguagePropertyValue fromValueObject(Object value)
		throws IOException {
		if ( value instanceof List langTextObjs ) {
			List<LangStringTextType> textList
							= FStream.<Object>from(langTextObjs)
									.mapOrThrow(langTextObj -> {
										if ( !(langTextObj instanceof Map langTextMap) ) {
											throw new IOException("Invalid language text for MultiLanguagePropertyValue: "
																	+ langTextObj);
										}
										Map.Entry<?,?> ent = Funcs.getFirst(((Map<?,?>)langTextMap).entrySet());
										if ( ent == null ) {
											throw new IOException("No language field: " + langTextObj);
										}
										return buildLangStringTextType("" + ent.getKey(), "" + ent.getValue());
									})
									.toList();
			return new MultiLanguagePropertyValue(textList);
		}
		else {
			throw new IOException("Invalid value object for MultiLanguagePropertyValue: " + value);
		}
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		for ( LangStringTextType langText: m_langTexts ) {
			if ( langText.getLanguage() == null || langText.getText() == null ) {
				throw new IOException(String.format("Invalid language-text: language=%s, text=%s",
													langText.getLanguage(), langText.getText()));
			}
			gen.writeStartObject();
			gen.writeStringField(langText.getLanguage(), langText.getText());
			gen.writeEndObject();
		}
		gen.writeEndArray();
	}
	
	public static MultiLanguagePropertyValue deserializeValue(JsonNode vnode) throws IOException {
		return parseValueJsonNode(vnode);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(m_langTexts);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || MultiLanguagePropertyValue.class != obj.getClass() ) {
			return false;
		}

		MultiLanguagePropertyValue other = (MultiLanguagePropertyValue)obj;
		return Objects.equals(m_langTexts, other.m_langTexts);
	}
	
	public static LangStringTextType buildLangStringTextType(String language, String text) {
		return new DefaultLangStringTextType.Builder()
											.language(language)
											.text(text)
											.build();
	}
}
