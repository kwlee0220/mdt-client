package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.func.Funcs;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MultiLanguagePropertyValue extends AbstractElementValue implements DataElementValue {
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
	
	public void update(MultiLanguageProperty mlprop) {
		mlprop.setValue(m_langTexts);
	}

	@Override
	public List<Map<String,String>> toValueObject() {
		return FStream.from(m_langTexts)
						.map(langText -> Map.of(langText.getLanguage(), langText.getText()))
						.toList();
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}
	
	public static MultiLanguagePropertyValue from(MultiLanguageProperty mlp) {
		return new MultiLanguagePropertyValue(mlp.getValue());
	}
	
	public static MultiLanguagePropertyValue parseValueJsonNode(JsonNode vnode, MultiLanguageProperty mlprop)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("MultiLanguagePropertyValue expects an 'Object' node: JsonNode=" + vnode);
		}
		
		List<LangStringTextType> textList
				= FStream.from(vnode.elements())
							.mapOrThrow(elm -> {
								Map.Entry<String,JsonNode> ent
									= Funcs.getFirst(elm.properties())
											.orElseThrow(() -> new IllegalStateException("No language field"));
								return buildLangStringTextType(ent.getKey(), ent.getValue().asText());
							})
							.toList();
		return new MultiLanguagePropertyValue(textList);
	}
	
	public static MultiLanguagePropertyValue fromValueObject(Object value, MultiLanguageProperty mlprop)
		throws IOException {
		if ( value instanceof Map vmap ) {
			@SuppressWarnings("unchecked")
			List<LangStringTextType> textList
							= KeyValueFStream.<String,String>from(vmap)
											.map((k,v) -> buildLangStringTextType(""+k, ""+v))
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
			gen.writeStartObject();
			gen.writeStringField(langText.getLanguage(), langText.getText());
			gen.writeEndObject();
		}
		gen.writeEndArray();
	}
	
	public static MultiLanguagePropertyValue deserializeValue(JsonNode vnode) {
		List<LangStringTextType> textList
				= FStream.from(vnode.elements())
							.mapOrThrow(elm -> {
								Map.Entry<String,JsonNode> ent
									= Funcs.getFirst(elm.properties())
											.orElseThrow(() -> new IllegalStateException("No language field"));
								return buildLangStringTextType(ent.getKey(), ent.getValue().asText());
							})
							.toList();
		return new MultiLanguagePropertyValue(textList);
	}
	
	public static LangStringTextType buildLangStringTextType(String language, String text) {
		return new DefaultLangStringTextType.Builder()
											.language(language)
											.text(text)
											.build();
	}
}
