package mdt.model.timeseries;

import javax.annotation.Nullable;

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
