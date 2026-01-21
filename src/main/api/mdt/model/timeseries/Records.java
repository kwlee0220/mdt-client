package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Records {
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.RECORDS;
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
	 * @return 레코드 리스트
	 */
	public List<? extends Record> getRecordList();
}
