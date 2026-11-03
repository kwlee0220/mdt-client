package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * 시계열 segment에 포함된 레코드({@link Record})들의 집합(Records)을 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Records {
	/** Records의 semanticId. */
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.RECORDS;
	/** Records의 semanticId 참조. */
	public static final Reference SEMANTIC_ID_REFERENCE = TimeSeriesSemanticIds.RECORDS_REFERENCE;
	
//	/**
//	 * 레코드의 메타데이터를 반환한다.
//	 *
//	 * @return	레코드의 메타데이터
//	 */
//	public RecordMetadata getRecordMetadata();
	
	/**
	 * 레코드 리스트를 반환한다.
	 *
	 * @return	레코드 리스트.
	 */
	public List<? extends Record> getRecordList();
}
