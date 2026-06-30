package mdt.model.sm.ref.timeseries;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.jetbrains.annotations.Nullable;

import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.timeseries.Metadata;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
abstract class SegmentRecordsReader {
	protected final Metadata m_tsMetadata;
	@Nullable protected final TimeSeriesRange m_range;
	@Nullable protected final List<String> m_columns;
	
	abstract protected ElementCollectionValue readValue() throws IOException;
	abstract protected SubmodelElementCollection read() throws IOException;
	
	protected SegmentRecordsReader(Metadata tsMetadata, @Nullable TimeSeriesRange range, @Nullable List<String> columns) {
		m_tsMetadata = tsMetadata;
		m_range = range;
		m_columns = columns;
	}
}
