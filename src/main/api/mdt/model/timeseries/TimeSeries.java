package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TimeSeries {
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.TIME_SERIES;
	public static final Reference SEMANTIC_ID_REFERENCE = TimeSeriesSemanticIds.TIME_SERIES_REFERENCE;
	
	/**
	 * 본 시계열 데이터에 포함된 레코드의 메타데이터를 반환한다.
	 *
	 * @return	레코드의 메타데이터
	 */
	public Metadata getMetadata();
	
	/**
	 * 본 시계열 데이터에 포함된 segment (구간)들을 반환한다.
	 *
	 * @return	구간들
	 */
	public Segments getSegments();
}
