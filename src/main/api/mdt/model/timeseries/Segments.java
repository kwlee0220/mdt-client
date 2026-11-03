package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

/**
 * 시계열 데이터를 구성하는 segment({@link Segment})들의 집합(Segments)을 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Segments {
	/** Segments의 semanticId. */
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.SEGMENTS;
	/** Segments의 semanticId 참조. */
	public static final Reference SEMANTIC_ID_REFERENCE = TimeSeriesSemanticIds.SEGMENTS_REFERENCE;
	
	/**
	 * Segment 리스트를 반환한다.
	 *
	 * @return	Segment 리스트.
	 */
	public List<? extends Segment> getSegments();
}
