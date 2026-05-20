package mdt.model.timeseries;

import java.time.Instant;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.model.sm.PropertyUtils;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.model.timeseries.RecordMetadata.Field;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DefaultRecord extends SubmodelElementCollectionEntity implements Record {
	// 경우에 따라서는 null이 될 수도 있으므로 주의
	// 이 경우에는 AAS 모델에서 레코드의 필드 정보를 가져와야 한다. 
	private RecordMetadata m_metadata;
	private List<FieldValue> m_values;
	
	/**
	 * Empty 레코드를 생성한다.
	 * <p>
	 * 레코드의 메타데이터와 값은 null로 제대로 초기화되지 않은 상태로 생성된다.
	 * 이 경우에는 반드시 {@link #updateFromAasModel(SubmodelElement)} 메소드를 통해
	 * AAS 모델에서 레코드의 필드 정보를 가져와야 한다.
	 */
	public DefaultRecord() {
		setSemanticId(Record.SEMANTIC_ID_REFERENCE);
		
		m_metadata = null;
		m_values = null;
	}
	
	/**
	 * 주어진 스키마를 사용한 empty 레코드를 생성한다.
	 * 
	 * @param metadata 레코드 메타데이터
	 */
	public DefaultRecord(RecordMetadata metadata) {
		Preconditions.checkArgument(metadata != null, "'metadata' should not be null");
		
		setSemanticId(Record.SEMANTIC_ID_REFERENCE);
		
		m_metadata = metadata;
		m_values = FStream.from(metadata.getFieldAll())
							.map(field -> new FieldValue(field, null))
							.toList();
	}
	
	/**
	 * 주어진 스키마와 값을 사용한 레코드를 생성한다.
	 * 
	 * @param idShort	레코드에 부여할 idShort 값.
	 * @param metadata	레코드 메타데이터
	 * @param values	레코드 값
	 */
	public DefaultRecord(String idShort, RecordMetadata metadata, List<Object> values) {
		Preconditions.checkArgument(metadata != null, "'metadata' should not be null");
		Preconditions.checkArgument(values != null, "'values' should not be null");
		Preconditions.checkArgument(values.size() == metadata.getFieldCount(),
									"The size of 'values'(%s) should be equal to the field count (%s) of 'metadata'",
									values, metadata);
		
		setIdShort(idShort);
		setSemanticId(Record.SEMANTIC_ID_REFERENCE);
		
		m_metadata = metadata;
		m_values = Lists.newArrayListWithExpectedSize(metadata.getFieldCount());
		for ( int i = 0; i < metadata.getFieldCount(); ++i ) {
			Field field = metadata.getField(i);
			Object value = values.get(i);

			m_values.add(new FieldValue(field, value));
		}
	}

	@Override
	public RecordMetadata getMetadata() {
		return m_metadata;
	}

	@Override
	public Instant getTimestamp() {
		return (Instant)m_values.get(0).getValue();
	}

	@Override
	public List<FieldValue> getFieldValues() {
		return m_values;
	}
	
	public FieldValue getFieldValue(int index) {
		return m_values.get(index);
	}

	@Override
	public void updateAasModel(SubmodelElement model) {
		Preconditions.checkArgument(m_metadata != null, "'metadata' should not be null");
		
		super.updateAasModel(model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		smc.setIdShort(getIdShort());
		smc.setSemanticId(Record.SEMANTIC_ID_REFERENCE);
		
		List<SubmodelElement> fieldProps = FStream.from(m_values)
												.map(fv -> (SubmodelElement)toProperty(fv))
												.toList();
		smc.setValue(fieldProps);
	}

	@Override
	public void updateFromAasModel(SubmodelElement model) {
		super.updateFromAasModel(model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		if ( m_metadata == null ) {
			List<Field> fields = Lists.newArrayList();
			m_values = FStream.from(smc.getValue())
								.castSafely(Property.class)
								.map(prop -> {
									Field field = new Field(prop.getIdShort(), prop.getValueType());
									fields.add(field);
									Object value = field.getType().parseValueString(prop.getValue());
									return new FieldValue(field, value);
								})
								.toList();
			m_metadata = new RecordMetadata(fields);
		}
		else {
			m_values = FStream.from(smc.getValue())
								.castSafely(Property.class)
								.zipWith(FStream.from(m_metadata.getFieldAll()))
								.map(tup -> new FieldValue(tup._2, tup._2.getType().parseValueString(tup._1.getValue())))
								.toList();
		}
		
	}
	
	@Override
	public String toString() {
		String prefix = String.format("%s: { ", getIdShort());
		return FStream.from(m_values)
							.map(FieldValue::toString)
							.join(", ", prefix, " }");
	}
	
	@SuppressWarnings("unchecked")
	private Property toProperty(FieldValue fieldValue) {
		Field field = fieldValue.getField();
	    String valStr = field.getType().toValueString(fieldValue.getValue());
	    return PropertyUtils.newProperty(field.getName(), field.getType().getTypeDefXsd(), valStr);
	}
}
