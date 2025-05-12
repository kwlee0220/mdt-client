package mdt.model.timeseries;

import java.time.Instant;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import mdt.model.timeseries.RecordMetadata.Field;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Record {
	public static final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Record/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
											= new DefaultReference.Builder()
													.type(ReferenceTypes.EXTERNAL_REFERENCE)
													.keys(new DefaultKey.Builder()
																		.type(KeyTypes.GLOBAL_REFERENCE)
																		.value(SEMANTIC_ID)
																		.build())
													.build();
	
	public static final class FieldValue {
		private final Field m_field;
		private final Object m_value;
        
        public FieldValue(Field field, Object value) {
            m_field = field;
            m_value = value;
        }
        
        public Field getField() {
            return m_field;
        }
        
        public Object getValue() {
            return m_value;
        }
        
        public String toString() {
            return String.format("%s:%s", m_field.getName(), m_value);
        }
	}
	
	/**
	 * 레코드의 메타데이터를 반환한다.
	 *
	 * @return	레코드의 메타데이터
	 */
	public RecordMetadata getMetadata();
	
	/**
	 * 본 시계열 레코드의 타임스탬프를 반환한다.
	 *
	 * @return	본 시계열 레코드의 타임스탬프
	 */
	public Instant getTimestamp();
	
	/**
	 * 레코드를 구성하는 필드 값 리스트를 반환한다.
	 *
	 * @return	레코드 필드 값 리스트
	 */
	public List<FieldValue> getFieldValues();
}
