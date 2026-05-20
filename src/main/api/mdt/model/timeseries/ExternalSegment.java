package mdt.model.timeseries;

import org.jetbrains.annotations.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ExternalSegment extends Segment {
	public static final Reference SEMANTIC_ID = TimeSeriesSemanticIds.EXTERNAL_SEGMENT_REFERENCE;
	
	@Nullable public File getData();
}
