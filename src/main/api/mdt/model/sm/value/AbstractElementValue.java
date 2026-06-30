package mdt.model.sm.value;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.MDTModelSerDe;


/**
 * {@link ElementValue} 구현을 위한 추상 기반 클래스.
 * <p>
 * {@link MDTModelSerDe}를 이용해 JSON 문자열({@link #toJsonString()}/{@link #toValueJsonString()})과
 * 사람이 읽는 표시 문자열({@link #toDisplayString()})의 기본 구현을 제공한다. polymorphic JSON
 * 직렬화/역직렬화는 {@link ElementValues.Serializer}/{@link ElementValues.Deserializer}가 담당하며,
 * 클래스에 {@link JsonSerialize}/{@link JsonDeserialize}로 지정되어 있다.
 * <p>
 * 하위 클래스는 직렬화 식별자({@link #getSerializationType()})와 값 직렬화 방식
 * ({@link #serializeValue(JsonGenerator)})을 구현한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonDeserialize(using = ElementValues.Deserializer.class)
@JsonSerialize(using = ElementValues.Serializer.class)
public abstract class AbstractElementValue implements ElementValue {
	/**
	 * 이 ElementValue 종류를 식별하는 직렬화 타입 문자열을 반환한다.
	 * <p>
	 * polymorphic JSON 직렬화 시 타입 구분자로 사용된다.
	 *
	 * @return 직렬화 타입 식별자.
	 */
	abstract public String getSerializationType();

	/**
	 * 값에 해당하는 부분을 주어진 {@link JsonGenerator}로 직렬화한다.
	 *
	 * @param gen	직렬화에 사용할 JsonGenerator.
	 * @throws IOException	직렬화가 실패한 경우.
	 */
	abstract public void serializeValue(JsonGenerator gen) throws IOException;

	@Override
	public String toJsonString() {
		try {
			return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
		}
		catch ( JsonProcessingException e ) {
			throw new UncheckedIOException("Failed to get JSON string of ElementValue: cause=" + e, e);
		}
	}
	
	@Override
	public JsonNode toJsonNode() {
		return MDTModelSerDe.getJsonMapper().valueToTree(this);
	}

	@Override
	public String toValueJsonString() {
		try {
			return MDTModelSerDe.getJsonMapper().writeValueAsString(toValueObject());
		}
		catch ( JsonProcessingException e ) {
			throw new UncheckedIOException("Failed to get valueString of ElementValue: cause=" + e, e);
		}
	}

	@Override
	public JsonNode toValueJsonNode() {
		return MDTModelSerDe.getJsonMapper().valueToTree(toValueObject());
	}

	@Override
	public String toDisplayString() {
		// PropertyValue인 경우는 재정의됨.
		return toValueJsonString();
	}
	
	@Override
	public String toString() {
		return toDisplayString();
	}
}
