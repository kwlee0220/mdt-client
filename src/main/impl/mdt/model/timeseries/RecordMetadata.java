package mdt.model.timeseries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RecordMetadata {
	private final KeyedValueList<String,Field> m_fields;
	
	/**
	 * 주어지 레코드 필드로 구성된 메타데이터를 생성한다.
	 * <p>
	 * 첫번째 필드는 반드시 'DATE_TIME' 타입이어야 한다.
	 * 
	 * @param fields	레코드 필드
	 */
	public RecordMetadata(List<Field> fields) {
		Preconditions.checkArgument(fields != null, "'fields' should not be null");
		Preconditions.checkArgument(fields.get(0).getType() == DataTypes.DATE_TIME,
									"The first field should be datatype of 'DATE_TIME', but %s",
									fields.get(0).getType());
		m_fields = KeyedValueList.from(fields, Field::getName);
	}
	
	/**
	 * 레코드의 필드 개수를 반환한다.
	 *
	 * @return	레코드의 필드 개수
	 */
	public int getFieldCount() {
		return m_fields.size();
	}
	
	/**
	 * 레코드의 필드 목록을 반환한다.
	 *
	 * @return 레코드의 필드 목록
	 */
	public List<Field> getFieldAll() {
		return Collections.unmodifiableList(m_fields);
	}
	
	/**
	 * 주어진 인덱스에 해당하는 레코드 필드를 반환한다.
	 *
	 * @param index 레코드 필드 인덱스
	 * @return 레코드 필드
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
	 * @param fieldName 레코드 필드 이름
	 * @return 레코드 필드
	 */
	public Field getField(String fieldName) {
		return m_fields.getOfKey(fieldName);
	}
	
	/**
	 * 주어진 필드 이름에 해당하는 레코드 필드 인덱스를 반환한다.
	 *
	 * @param fieldName 레코드 필드 이름
	 * @return 레코드 필드 인덱스
	 */
	public int getFieldIndex(String fieldName) {
		return m_fields.indexOfKey(fieldName);
	}
	
	/**
	 * {@link ResultSet}에서 레코드 필드 값을 읽어 반환한다.
	 *
	 * @param rset	레코드 필드 값을 읽을 {@link ResultSet}
	 * @return	레코드 필드 값 목록
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

	public static class Field implements Keyed<String> {
		private final String m_name;
		private final DataType<?> m_type;
		
		public Field(@JsonProperty("name") String name, @JsonProperty("type") DataTypeDefXsd typeDefXsd) {
			Preconditions.checkArgument(name != null, "'name' should not be null");
			Preconditions.checkArgument(typeDefXsd != null, "'type' should not be null");

			m_name = name;
			m_type = DataTypes.fromAas4jDatatype(typeDefXsd);
		}

		@Override
		public String key() {
			return m_name;
		}
		
		public String getName() {
			return m_name;
		}

		@SuppressWarnings("rawtypes")
		public DataType getType() {
			return m_type;
		}

		public DataTypeDefXsd getTypeDefXsd() {
			return m_type.getTypeDefXsd();
		}
		
		@Override
		public String toString() {
			return String.format("Field[%s]: %s", m_name, m_type.getName());
		}
	}
}
