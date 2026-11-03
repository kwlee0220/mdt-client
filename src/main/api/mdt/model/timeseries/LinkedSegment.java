package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * 레코드들이 외부 저장소(예: 데이터베이스)에 저장되어, endpoint와 질의문을 통해 조회되는
 * 시계열 segment(LinkedSegment)를 정의한다.
 * <p>
 * IDTA TimeSeries Submodel의 {@code LinkedSegment}에 해당한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface LinkedSegment extends Segment {
	/** LinkedSegment의 semanticId 참조. */
	public static final Reference SEMANTIC_ID = TimeSeriesSemanticIds.LINKED_SEGMENT_REFERENCE;
	
	/**
	 * Segment에 포함된 레코드들이 저장된 외부 저장소의 endpoint를 반환한다.
	 *
	 * @return	외부 저장소의 endpoint.
	 */
	public String getEndpoint();
	
	/**
	 * Segment에 포함된 레코드들을 외부 저장소에서 조회하기 위한 질의문을 반환한다.
	 *
	 * @return	질의문.
	 */
	public String getQuery();
}
