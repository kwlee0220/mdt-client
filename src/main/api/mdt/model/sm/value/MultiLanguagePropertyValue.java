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
 * AAS {@link MultiLanguageProperty} SubmodelElement의 값(언어별 텍스트 목록)을 표현하는 클래스.
 * <p>
 * 값 객체({@link #toValueObject()})와 value-only JSON은 언어 코드를 키로 하는 단일 필드 객체의 배열
 * (예: {@code [{"ko": "온도"}, {"en": "temperature"}]}) 형태이며, polymorphic JSON의 타입 식별자는
 * {@value #SERIALIZATION_TYPE}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MultiLanguagePropertyValue extends AbstractElementValue implements DataElementValue {
	/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
	public static final String SERIALIZATION_TYPE = "mdt:value:mlprop";
	
	private final List<LangStringTextType> m_langTexts;
	
	/**
	 * 주어진 언어별 텍스트 목록으로 {@code MultiLanguagePropertyValue} 객체를 생성한다.
	 * <p>
	 * 목록은 복사되지 않고 그대로 보관된다.
	 *
	 * @param langTexts	언어별 텍스트 목록.
	 * @throws IllegalArgumentException	{@code langTexts}가 {@code null}인 경우.
	 */
	public MultiLanguagePropertyValue(List<LangStringTextType> langTexts) {
		Preconditions.checkNotNullArgument(langTexts, "langTexts is null");

		m_langTexts = langTexts;
	}
	
	/**
	 * 하나의 언어 텍스트로 구성된 {@code MultiLanguagePropertyValue} 객체를 생성한다.
	 *
	 * @param language	언어 코드(예: {@code ko}, {@code en}).
	 * @param text		텍스트.
	 */
	public MultiLanguagePropertyValue(String language, String text) {
		this(List.of(buildLangStringTextType(language, text)));
	}
	
	/**
	 * 언어별 텍스트 목록을 반환한다.
	 *
	 * @return	언어별 텍스트 목록(내부 목록 그대로).
	 */
	public List<LangStringTextType> getLangTextAll() {
		return m_langTexts;
	}
	
	/**
	 * 주어진 {@link MultiLanguageProperty}의 값을 이 객체의 언어별 텍스트 목록으로 설정한다.
	 *
	 * @param mlprop	갱신할 MultiLanguageProperty 객체.
	 */
	public void update(MultiLanguageProperty mlprop) {
		mlprop.setValue(m_langTexts);
	}

	/**
	 * 언어별 텍스트 목록을 {@code {언어: 텍스트}} 형태의 단일 항목 {@link Map}의 리스트로 반환한다.
	 *
	 * @return	값 객체.
	 * @throws IllegalStateException	언어나 텍스트가 {@code null}인 항목이 있는 경우.
	 */
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
	
	/**
	 * AAS {@link MultiLanguageProperty} 객체의 값으로 {@code MultiLanguagePropertyValue} 객체를 생성한다.
	 *
	 * @param mlp	값을 추출할 MultiLanguageProperty 객체.
	 * @return	생성된 {@code MultiLanguagePropertyValue} 객체.
	 * @throws IllegalArgumentException	{@code mlp}의 값 목록이 {@code null}인 경우.
	 */
	public static MultiLanguagePropertyValue from(MultiLanguageProperty mlp) {
		return new MultiLanguagePropertyValue(mlp.getValue());
	}
	
	/**
	 * value-only JSON 노드({@code [{"<lang>": "<text>"}, ...]})로부터 {@code MultiLanguagePropertyValue}를
	 * 생성한다.
	 * <p>
	 * 각 원소 객체의 첫 번째 필드만 사용하며, 필드 값은 {@link JsonNode#asText()}로 읽는다.
	 *
	 * @param vnode	JSON 배열 노드.
	 * @return	생성된 {@code MultiLanguagePropertyValue} 객체.
	 * @throws IOException	{@code vnode}가 JSON 배열이 아니거나, 필드가 없는 원소가 있는 경우.
	 */
	public static MultiLanguagePropertyValue parseValueJsonNode(JsonNode vnode) throws IOException {
		if ( !vnode.isArray() ) {
			throw new IOException("MultiLanguagePropertyValue expects an 'Array' node: JsonNode=" + vnode);
		}
		
		List<LangStringTextType> textList
				= FStream.from(vnode.elements())
							.mapOrThrow(elm -> {
								Map.Entry<String,JsonNode> ent = Funcs.getFirst(elm.properties());
								if ( ent == null ) throw new IOException("No language field: element=" + elm);
								return buildLangStringTextType(ent.getKey(), ent.getValue().asText());
							})
							.toList();
		return new MultiLanguagePropertyValue(textList);
	}
	
	/**
	 * 값 객체({@code {언어: 텍스트}} 형태 {@link Map}의 {@link List})로부터 {@code MultiLanguagePropertyValue}를
	 * 생성한다.
	 * <p>
	 * 각 {@link Map}의 첫 번째 항목만 사용하며, 키와 값은 문자열로 변환된다.
	 *
	 * @param value	값 객체.
	 * @return	생성된 {@code MultiLanguagePropertyValue} 객체.
	 * @throws IOException	{@code value}가 {@link List}가 아니거나, 원소가 {@link Map}이 아니거나 비어 있는 경우.
	 */
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
	
	/**
	 * polymorphic JSON의 {@code value} 하위 노드로부터 {@code MultiLanguagePropertyValue}를 생성한다.
	 * <p>
	 * {@link #parseValueJsonNode(JsonNode)}와 동일하며, {@link ElementValues}의 역직렬화 등록부에서 사용된다.
	 *
	 * @param vnode	JSON 배열 노드.
	 * @return	생성된 {@code MultiLanguagePropertyValue} 객체.
	 * @throws IOException	{@code vnode}가 JSON 배열이 아닌 경우.
	 */
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
	
	/**
	 * 주어진 언어 코드와 텍스트로 {@link LangStringTextType} 객체를 생성한다.
	 *
	 * @param language	언어 코드.
	 * @param text		텍스트.
	 * @return	생성된 {@link LangStringTextType} 객체.
	 */
	public static LangStringTextType buildLangStringTextType(String language, String text) {
		return new DefaultLangStringTextType.Builder()
											.language(language)
											.text(text)
											.build();
	}
}
