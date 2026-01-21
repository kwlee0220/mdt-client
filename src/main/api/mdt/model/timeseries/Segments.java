package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Segments {
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.SEGMENTS;
	public static final Reference SEMANTIC_ID_REFERENCE = TimeSeriesSemanticIds.SEGMENTS_REFERENCE;
	
	public List<? extends Segment> getSegments();
}
