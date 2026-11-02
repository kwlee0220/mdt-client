package mdt.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonDeserializationException;


/**
 * {@link DataType} 구현의 공통 기반 클래스이다.
 * <p>
 * 식별자({@code xs:} id) · AAS4J {@link DataTypeDefXsd} · Java 클래스의 보관과 접근자를 제공하고,
 * Json/JDBC 변환의 기본 구현을 값 문자열 변환({@link #toValueString(Object)} /
 * {@link #parseValueString(String)})에 위임하여 제공한다. 따라서 구현체는 최소한
 * {@code toValueString} / {@code parseValueString}만 정의하면 동작하며, 타입별로 더 효율적이거나
 * 정확한 변환이 필요한 경우 Json/JDBC 메소드를 재정의(override)한다.
 *
 * @param <T>	이 데이터 타입에 대응하는 Java 값의 타입.
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractDataType<T> implements DataType<T> {
	private final String m_id;
	private final DataTypeDefXsd m_xsdType;
	private final Class<T> m_javaClass;

	/**
	 * 데이터 타입의 식별 정보를 받아 인스턴스를 생성한다.
	 *
	 * @param id		{@code xs:} 식별자(예: {@code "xs:int"}).
	 * @param xsdType	대응하는 AAS4J {@link DataTypeDefXsd}.
	 * @param javaClass	대응하는 Java 값 클래스.
	 */
	protected AbstractDataType(String id, DataTypeDefXsd xsdType, Class<T> javaClass) {
		m_id = id;
		m_xsdType = xsdType;
		m_javaClass = javaClass;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public DataTypeDefXsd getTypeDefXsd() {
		return m_xsdType;
	}

	@Override
	public Class<T> getJavaClass() {
		return m_javaClass;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 기본 구현은 {@link #toValueString(Object)} 결과(값 문자열)를 그대로 반환한다.
	 */
	@Override
	public Object toJsonObject(T value) {
		return toValueString(value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@code null}·JSON {@code null}을 처리한 뒤 실제 변환을 {@link #convertJsonNode(JsonNode)}에 맡기고, 변환 중 난
	 * 예외({@code NumberFormatException}, {@code DateTimeParseException} 등)를 모두 같은 예외로 바꿔 알린다.
	 * 메시지에는 데이터 타입과 JSON 노드 종류만 넣고, 값은 원인 예외에 남긴다.
	 */
	@Override
	public final T fromJsonNode(JsonNode jnode) throws JacksonDeserializationException {
		if ( jnode == null || jnode.isNull() ) {
			return null;
		}
		try {
			return convertJsonNode(jnode);
		}
		catch ( JacksonDeserializationException e ) {
			// 수치·불리언 변환(JsonScalars)의 메시지("int expected, but string" 등)에 데이터 타입을 붙인다.
			throw new JacksonDeserializationException(String.format("invalid %s value: %s", getId(), e.getMessage()), e);
		}
		catch ( RuntimeException e ) {
			throw new JacksonDeserializationException(String.format("invalid %s value: %s", getId(),
																	jnode.getNodeType().name().toLowerCase()), e);
		}
	}

	/**
	 * {@code null}이 아닌 JSON 노드를 값으로 변환한다. 변환할 수 없으면 비검사 예외를 던진다.
	 * <p>
	 * 기본 구현은 스칼라 노드의 텍스트를 {@link #parseValueString(String)}으로 해석하고, 객체·배열은 거부한다.
	 * 숫자·불리언처럼 JSON 자체의 타입을 그대로 쓸 수 있는 데이터 타입은 재정의한다.
	 *
	 * @param jnode	변환할 JSON 노드({@code null}·JSON {@code null}이 아님).
	 * @return	변환된 값.
	 * @throws JacksonDeserializationException	변환할 수 없는 경우(비검사 예외로 알려도 된다).
	 */
	protected T convertJsonNode(JsonNode jnode) throws JacksonDeserializationException {
		if ( jnode.isContainerNode() ) {
			throw new IllegalArgumentException("scalar expected");
		}
		return parseValueString(jnode.asText());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 기본 구현은 {@link #toValueString(Object)} 결과(값 문자열)를 그대로 반환한다.
	 */
	@Override
	public Object toJdbcObject(T value) {
		return toValueString(value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 기본 구현은 객체의 {@code toString()} 표현을 {@link #parseValueString(String)}으로 파싱한다.
	 */
	@Override
	public T fromJdbcObject(Object jdbcObj) {
		return (jdbcObj != null) ? parseValueString(jdbcObj.toString()) : null;
	}
}
