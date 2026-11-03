package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * 레코드들을 Submodel 내부에 직접 포함하는 시계열 segment(InternalSegment)를 정의한다.
 * <p>
 * IDTA TimeSeries Submodel의 {@code InternalSegment}에 해당한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InternalSegment extends Segment {
	/** InternalSegment의 semanticId 참조. */
	public static final Reference SEMANTIC_ID = TimeSeriesSemanticIds.INTERNAL_SEGMENT_REFERENCE;
	
	/**
	 * Segment를 구성하는 Record들의 집합을 반환한다.
	 *
	 * @return	Record들의 집합.
	 */
	public Records getRecords();
}
