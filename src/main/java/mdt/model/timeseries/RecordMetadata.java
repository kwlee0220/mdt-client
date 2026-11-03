package mdt.model.timeseries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import utils.Keyed;
import utils.KeyedValueList;
import utils.jdbc.JdbcUtils;
import utils.stream.FStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;

/**
 * 시계열 레코드의 구조(스키마)를 정의한다.
 * <p>
 * 레코드 메타데이터는 이름으로 구분되는 필드({@link Field})들의 순서있는 리스트이며,
 * 첫번째 필드는 레코드의 타임스탬프로 사용되는 {@code DATE_TIME} 타입 필드이어야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RecordMetadata {
	private final KeyedValueList<String,Field> m_fields;
	
	/**
	 * 주어진 레코드 필드로 구성된 메타데이터를 생성한다.
	 * <p>
	 * 첫번째 필드는 반드시 {@link DataTypes#DATE_TIME} 타입이어야 한다.
	 * 
	 * @param fields	레코드 필드 리스트.
	 * @throws IllegalArgumentException	{@code fields}가 {@code null}이거나 비어 있거나, 첫번째 필드가
	 * 									{@code DATE_TIME} 타입이 아닌 경우.
	 */
	public RecordMetadata(List<Field> fields) {
		Preconditions.checkArgument(fields != null, "'fields' should not be null");
		Preconditions.checkArgument(!fields.isEmpty(), "'fields' should not be empty");
		Preconditions.checkArgument(fields.get(0).getType() == DataTypes.DATE_TIME,
									"The first field should be datatype of 'DATE_TIME', but %s",
									fields.get(0).getType());
		m_fields = KeyedValueList.from(fields, Field::getName);
	}
	
	/**
	 * 레코드의 필드 개수를 반환한다.
	 *
	 * @return	레코드의 필드 개수.
	 */
	public int getFieldCount() {
		return m_fields.size();
	}
	
	/**
	 * 레코드의 필드 목록을 반환한다.
	 *
	 * @return	레코드의 필드 목록 (수정 불가).
	 */
	public List<Field> getFieldAll() {
		return Collections.unmodifiableList(m_fields);
	}
	
	/**
	 * 필드 이름을 key로 하는 필드 map을 필드 순서대로 반환한다.
	 *
	 * @return	필드 이름과 필드의 map.
	 */
	public LinkedHashMap<String,Field> getFieldMap() {
		return m_fields.toMap();
	}
	
	/**
	 * 주어진 인덱스에 해당하는 레코드 필드를 반환한다.
	 *
	 * @param index	레코드 필드 인덱스 (0-based).
	 * @return	레코드 필드.
	 * @throws IllegalArgumentException	{@code index}가 유효 범위를 벗어난 경우.
	 */
	public Field getField(int index) {
		Preconditions.checkArgument(index >= 0 && index < getFieldCount(),
									"Invalid Record field index: index=%d, field_count=%d",
									index, getFieldCount());
		return m_fields.get(index);
	}
	
	/**
	 * 주어진 필드 이름에 해당하는 레코드 필드를 반환한다.
	 *
	 * @param fieldName	레코드 필드 이름.
	 * @return	레코드 필드. 해당 이름의 필드가 없는 경우 {@code null}.
	 */
	public Field getField(String fieldName) {
		return m_fields.getOfKey(fieldName);
	}
	
	/**
	 * 주어진 필드 이름에 해당하는 레코드 필드 인덱스를 반환한다.
	 *
	 * @param fieldName	레코드 필드 이름.
	 * @return	레코드 필드 인덱스. 해당 이름의 필드가 없는 경우 {@code -1}.
	 */
	public int getFieldIndex(String fieldName) {
		return m_fields.indexOfKey(fieldName);
	}
	
	/**
	 * {@link ResultSet}의 현재 행에서 레코드 필드 값을 읽어 반환한다.
	 * <p>
	 * 각 컬럼 값은 같은 위치의 필드 타입에 맞게 변환된다. 컬럼 수와 필드 수가 다른 경우에는
	 * 둘 중 적은 수만큼만 읽는다.
	 *
	 * @param rset	레코드 필드 값을 읽을 {@link ResultSet}.
	 * @return	레코드 필드 값 목록.
	 * @throws IllegalArgumentException	{@code rset}이 {@code null}인 경우.
	 * @throws SQLException	필드 값을 읽는 중 발생한 예외.
	 */
	public List<Object> read(ResultSet rset) throws SQLException {
		Preconditions.checkArgument(rset != null, "'ResultSet' should not be null");
		
		return FStream.from(m_fields)
						.zipWith(FStream.from(JdbcUtils.toColumnObjectList(rset)))
						.map(tup -> tup._1.getType().fromJdbcObject(tup._2))
						.toList();
	}
	
	@Override
	public String toString() {
		return FStream.from(m_fields)
						.map(field -> String.format("%s:%s", field.getName(), field.getType().getName()))
						.join(", ", "{", "}");
	}

	/**
	 * 레코드를 구성하는 필드 하나를 정의한다. 필드는 이름과 데이터 타입으로 구성된다.
	 */
	public static class Field implements Keyed<String> {
		private final String m_name;
		private final DataType<?> m_type;
		
		/**
		 * 레코드 필드를 생성한다.
		 *
		 * @param name			필드 이름.
		 * @param typeDefXsd	필드의 AAS 데이터 타입.
		 * @throws IllegalArgumentException	{@code name} 또는 {@code typeDefXsd}가 {@code null}인 경우.
		 */
		public Field(@JsonProperty("name") String name, @JsonProperty("type") DataTypeDefXsd typeDefXsd) {
			Preconditions.checkArgument(name != null, "'name' should not be null");
			Preconditions.checkArgument(typeDefXsd != null, "'type' should not be null");

			m_name = name;
			m_type = DataTypes.fromAas4jDatatype(typeDefXsd);
		}

		/**
		 * 필드 이름을 key로 반환한다.
		 *
		 * @return	필드 이름.
		 */
		@Override
		public String key() {
			return m_name;
		}
		
		/**
		 * 필드 이름을 반환한다.
		 *
		 * @return	필드 이름.
		 */
		public String getName() {
			return m_name;
		}

		/**
		 * 필드의 데이터 타입을 반환한다.
		 *
		 * @return	데이터 타입.
		 */
		@SuppressWarnings("rawtypes")
		public DataType getType() {
			return m_type;
		}

		/**
		 * 필드의 AAS 데이터 타입({@link DataTypeDefXsd})을 반환한다.
		 *
		 * @return	AAS 데이터 타입.
		 */
		public DataTypeDefXsd getTypeDefXsd() {
			return m_type.getTypeDefXsd();
		}
		
		@Override
		public String toString() {
			return String.format("Field[%s]: %s", m_name, m_type.getName());
		}
	}
}
