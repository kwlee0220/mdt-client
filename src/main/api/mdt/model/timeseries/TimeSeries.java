package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * IDTA TimeSeries Submodel로 표현되는 시계열 데이터를 정의한다.
 * <p>
 * 시계열 데이터는 메타데이터({@link Metadata})와 segment들의 집합({@link Segments})으로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TimeSeries {
	/** TimeSeries Submodel의 semanticId. */
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.TIME_SERIES;
	/** TimeSeries Submodel의 semanticId 참조. */
	public static final Reference SEMANTIC_ID_REFERENCE = TimeSeriesSemanticIds.TIME_SERIES_REFERENCE;
	
	/**
	 * 본 시계열 데이터의 메타데이터를 반환한다.
	 *
	 * @return	시계열 메타데이터.
	 */
	public Metadata getMetadata();
	
	/**
	 * 본 시계열 데이터에 포함된 segment (구간)들을 반환한다.
	 *
	 * @return	구간들.
	 */
	public Segments getSegments();
}
