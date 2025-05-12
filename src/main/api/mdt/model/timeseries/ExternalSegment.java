package mdt.model.timeseries;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ExternalSegment extends Segment {
	public static final Reference SEMANTIC_ID
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value("https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1")
									.build())
				.build();
	
	@Nullable public File getData();
}
