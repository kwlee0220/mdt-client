package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface LinkedSegment extends Segment {
	public static final Reference SEMANTIC_ID = TimeSeriesSemanticIds.LINKED_SEGMENT_REFERENCE;
	
	/**
	 * Segment에 포함된 레코드들이 저장된 외부 저장소의 endpoint를 나타낸다.
	 *
	 * @return	외부 저장소의 endpoint
	 */
	public String getEndpoint();
	
	/**
	 * Segment에 포함된 레코드들을 조회하기 위한 질의문을 나타낸다.
	 *
	 * @return 질의문
	 */
	public String getQuery();
}
