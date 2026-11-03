package mdt.model.timeseries;

import java.time.Instant;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.timeseries.RecordMetadata.Field;

/**
 * 시계열 데이터를 구성하는 레코드(Record) 하나를 정의한다.
 * <p>
 * 레코드는 {@link RecordMetadata}에 정의된 필드들의 값으로 구성되며, 첫번째 필드는
 * 레코드의 타임스탬프이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Record {
	/** Record의 semanticId. */
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.RECORD;
	/** Record의 semanticId 참조. */
	public static final Reference SEMANTIC_ID_REFERENCE = TimeSeriesSemanticIds.RECORD_REFERENCE;
	
	/**
	 * 레코드 필드와 그 값의 쌍.
	 */
	public static final class FieldValue {
		private final Field m_field;
		private final Object m_value;
        
        /**
         * 필드 값 객체를 생성한다.
         *
         * @param field	레코드 필드.
         * @param value	필드 값. {@code null}이 허용된다.
         */
        public FieldValue(Field field, Object value) {
            m_field = field;
            m_value = value;
        }
        
        /**
         * 레코드 필드를 반환한다.
         *
         * @return	레코드 필드.
         */
        public Field getField() {
            return m_field;
        }
        
        /**
         * 필드 값을 반환한다.
         *
         * @return	필드 값. 설정되지 않은 경우 {@code null}.
         */
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
	 * @return	레코드의 메타데이터.
	 */
	public RecordMetadata getMetadata();
	
	/**
	 * 본 시계열 레코드의 타임스탬프를 반환한다.
	 * <p>
	 * 타임스탬프는 레코드의 첫번째 필드 값이다.
	 *
	 * @return	본 시계열 레코드의 타임스탬프.
	 */
	public Instant getTimestamp();
	
	/**
	 * 레코드를 구성하는 필드 값 리스트를 반환한다.
	 *
	 * @return	레코드 필드 값 리스트.
	 */
	public List<FieldValue> getFieldValues();
}
