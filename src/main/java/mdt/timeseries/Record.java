package mdt.timeseries;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.base.Preconditions;

import utils.Indexed;
import utils.stream.FStream;

import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.DataType;
import mdt.model.DataTypes;
import mdt.model.SubmodelUtils;
import mdt.timeseries.RecordSchema.Field;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Record extends AbstractMDTSubmodelElementCollection {
	private static final Reference SEMANTIC_ID
					= new DefaultReference.Builder()
							.type(ReferenceTypes.EXTERNAL_REFERENCE)
							.keys(new DefaultKey.Builder()
												.type(KeyTypes.GLOBAL_REFERENCE)
												.value("https://admin-shell.io/idta/TimeSeries/Record/1/1")
												.build())
							.build();
	
	private RecordSchema m_schema;
	private List<Object> m_values;
	
	public Record(RecordSchema schema) {
		m_schema = schema;
		m_values = Arrays.asList(new Object[schema.getFieldCount()]); 
	}
	
	public Record(RecordSchema schema, List<Object> values) {
		m_schema = schema;
		m_values = values;
	}
	
	public Instant getTimestamp() {
		return (Instant)m_values.get(0);
	}
	
	public Object getFieldValue(String fieldName) {
		Preconditions.checkArgument(fieldName != null);
		
		Indexed<Field> found = m_schema.getIndexField(fieldName);
		Preconditions.checkArgument(found != null, "Invalid field name: name=%s", fieldName);
		
		return m_values.get(found.index());
	}
	
	public Object getFieldValue(int fieldIndex) {
		Preconditions.checkArgument(fieldIndex >= 0 && fieldIndex < m_schema.getFieldCount(),
									"Invalid Record field index: index=%d, field_count=%d",
									fieldIndex, m_schema.getFieldCount());
		return m_values.get(fieldIndex);
	}
	
	public void setFieldValue(int fieldIndex, Object value) {
		Preconditions.checkArgument(fieldIndex >= 0 && fieldIndex < m_schema.getFieldCount(),
									"Invalid Record field index: index=%d, field_count=%d",
									fieldIndex, m_schema.getFieldCount());
		m_values.set(fieldIndex, value);
	}
	
	public List<Object> getFieldValueAll() {
		return m_values;
	}

	@Override
	public SubmodelElementCollection toAasModel() {
		List<SubmodelElement> elements = FStream.from(this.m_schema.getFieldAll())
												.zipWith(FStream.from(this.m_values))
												.map(t -> this.toProperty(t._1, t._2))
												.cast(SubmodelElement.class)
												.toList();
		return new DefaultSubmodelElementCollection.Builder()
						.idShort("Record")
						.value(elements)
						.build();
	}

	@Override
	public void fromAasModel(SubmodelElement model) {
		Preconditions.checkArgument(model instanceof SubmodelElementCollection);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		m_values = FStream.from(smc.getValue())
						.castSafely(Property.class)
						.map(this::getFieldValue)
						.toList();
	}
	
	public static Record newRecord(SubmodelElementCollection model) {
		RecordSchema schema = newRecordSchema(model);
		Record record = new Record(schema);
		record.fromAasModel(model);
		
		return record;
	}
	
	public static RecordSchema newRecordSchema(SubmodelElementCollection model) {
		List<Field> fields = FStream.from(model.getValue())
									.castSafely(Property.class)
									.map(prop -> getField(prop))
									.toList();
		return new RecordSchema(fields);
	}
	
	private Property toProperty(Field field, Object value) {
		 return SubmodelUtils.newProperty(field.getName(), field.getType().getTypeDefXsd(),
				 							field.getType().toValueString(value));
	}
	
	private Object getFieldValue(Property prop) {
		Indexed<Field> indexedField = m_schema.getIndexField(prop.getIdShort());
		return indexedField.value().getType().parseValueString(prop.getValue());
	}
	
	private static Field getField(Property prop) {
		DataType type = DataTypes.fromAas4jDatatype(prop.getValueType());
		return new Field(prop.getIdShort(), type);
	}
}
