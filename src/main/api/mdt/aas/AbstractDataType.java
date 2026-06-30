package mdt.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.databind.JsonNode;


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
	 * 기본 구현은 노드의 텍스트 표현을 {@link #parseValueString(String)}으로 파싱한다.
	 */
	@Override
	public T fromJsonNode(JsonNode jnode) {
		if ( jnode != null && !jnode.isNull() ) {
			return parseValueString(jnode.asText());
		}
		else {
			return null;
		}
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
