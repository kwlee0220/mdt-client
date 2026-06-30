package mdt.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * AAS({@code xs:*}) 데이터 타입을 나타내며, 해당 타입 값의 표현(representation) 간 상호 변환을 담당하는 인터페이스이다.
 * <p>
 * 각 데이터 타입은 다음 세 가지 외부 표현과 Java 값({@code T}) 사이의 변환을 정의한다.
 * <ul>
 *   <li>값 문자열: AAS {@code xs:*} 표기의 문자열({@link #toValueString(Object)} / {@link #parseValueString(String)})</li>
 *   <li>Json: Jackson {@link JsonNode} 표현({@link #toJsonObject(Object)} / {@link #fromJsonNode(JsonNode)})</li>
 *   <li>JDBC: 데이터베이스 컬럼 값 객체({@link #toJdbcObject(Object)} / {@link #fromJdbcObject(Object)})</li>
 * </ul>
 * 구현체는 보통 불변(immutable) 싱글턴이며 {@link DataTypes}에 상수로 등록되어 있다.
 *
 * @param <T>	이 데이터 타입에 대응하는 Java 값의 타입.
 * @author Kang-Woo Lee (ETRI)
 */
public interface DataType<T> {
	/**
	 * 이 데이터 타입의 {@code xs:} 식별자를 반환한다(예: {@code "xs:int"}).
	 *
	 * @return {@code xs:} 식별자 문자열.
	 */
	public String getId();

	/**
	 * 이 데이터 타입에 대응하는 AAS4J {@link DataTypeDefXsd}를 반환한다.
	 *
	 * @return AAS4J XSD 데이터 타입.
	 */
	public DataTypeDefXsd getTypeDefXsd();

	/**
	 * 이 데이터 타입에 대응하는 Java 클래스를 반환한다(예: {@code Integer.class}).
	 *
	 * @return Java 값 클래스.
	 */
	public Class<T> getJavaClass();

	/**
	 * 이 데이터 타입의 이름을 반환한다.
	 * <p>
	 * 기본 구현은 {@link DataTypeDefXsd#name()}을 반환한다(예: {@code "INT"}).
	 *
	 * @return 데이터 타입 이름.
	 */
	public default String getName() {
		return getTypeDefXsd().name();
	}

	/**
	 * 주어진 값을 이 데이터 타입의 값 문자열({@code xs:*} 표기)로 변환한다.
	 *
	 * @param value	변환할 값. {@code null}이면 {@code null}을 반환한다.
	 * @return 값 문자열, 또는 {@code value}가 {@code null}이면 {@code null}.
	 */
	public String toValueString(Object value);

	/**
	 * 값 문자열을 이 데이터 타입의 Java 값으로 파싱한다.
	 *
	 * @param str	파싱할 값 문자열. {@code null}(또는 빈 문자열)이면 {@code null}을 반환한다.
	 * @return 파싱된 값, 또는 {@code str}이 {@code null}이면 {@code null}.
	 * @throws IllegalArgumentException	문자열이 이 데이터 타입의 형식에 맞지 않는 경우.
	 */
	public T parseValueString(String str);

	/**
	 * 값을 Json 직렬화에 적합한 객체로 변환한다.
	 *
	 * @param value	변환할 값. {@code null}이면 {@code null}을 반환한다.
	 * @return Json 직렬화용 객체, 또는 {@code null}.
	 */
	public Object toJsonObject(T value);

	/**
	 * Json 노드를 이 데이터 타입의 Java 값으로 변환한다.
	 *
	 * @param jnode	변환할 Json 노드. {@code null}이거나 Json {@code null}이면 {@code null}을 반환한다.
	 * @return 변환된 값, 또는 {@code null}.
	 */
	public T fromJsonNode(JsonNode jnode);

	/**
	 * 값을 JDBC 저장에 적합한 객체로 변환한다.
	 *
	 * @param value	변환할 값. {@code null}이면 {@code null}을 반환한다.
	 * @return JDBC 저장용 객체, 또는 {@code null}.
	 */
	public Object toJdbcObject(T value);

	/**
	 * JDBC 조회 결과 객체를 이 데이터 타입의 Java 값으로 변환한다.
	 *
	 * @param jdbcObj	변환할 JDBC 객체. {@code null}이면 보통 {@code null}을 반환한다.
	 * @return 변환된 값.
	 * @throws IllegalArgumentException	객체가 이 데이터 타입으로 변환될 수 없는 경우.
	 */
	public T fromJdbcObject(Object jdbcObj);
}
