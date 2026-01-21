package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InternalSegment extends Segment {
	public static final Reference SEMANTIC_ID = TimeSeriesSemanticIds.INTERNAL_SEGMENT_REFERENCE;
	
	/**
	 * Segment를 구성하는 Record들의 집합을 반환한다.
	 *
	 * @return	 Records
	 */
	public Records getRecords();
}
