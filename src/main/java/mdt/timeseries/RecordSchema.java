package mdt.timeseries;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import utils.Indexed;
import utils.stream.FStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mdt.model.DataType;
import mdt.model.DataTypes;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RecordSchema {
	private List<Field> m_fields;
	private Map<String,Indexed<Field>> m_fieldsMap;

	@Getter
	@AllArgsConstructor
	public static class Field {
		private String name;
		private DataType type;
	}
	
	public RecordSchema(List<Field> fields) {
		Preconditions.checkArgument(fields != null, "'fields' should not be null");
		Preconditions.checkArgument(fields.get(0).getType() == DataTypes.DATE_TIME,
									"The first field should be datatype of 'DATE_TIME', but %s",
									fields.get(0).getType().getName());
		this.m_fields = fields;
		this.m_fieldsMap = FStream.from(fields)
									.zipWithIndex()
									.map(t -> Indexed.with(t.value(), t.index()))
									.toMap(idxed -> idxed.value().name);
	}
	
	public int getFieldCount() {
		return m_fields.size();
	}
	
	public List<Field> getFieldAll() {
		return Collections.unmodifiableList(m_fields);
	}
	
	public Field getField(int index) {
		Preconditions.checkArgument(index >= 0 && index < getFieldCount(),
									"Invalid Record field index: index=%d, field_count=%d",
									index, getFieldCount());
		return m_fields.get(index);
	}
	
	public Indexed<Field> getField(String fieldName) {
		Preconditions.checkArgument(fieldName != null);
		
		Indexed<Field> found = getIndexField(fieldName);
		Preconditions.checkArgument(found != null, "Invalid field name: name=%s", fieldName);
		
		return found;
	}
	
	public Indexed<Field> getIndexField(String name) {
		return this.m_fieldsMap.get(name);
	}
}
