package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import lombok.experimental.UtilityClass;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class TimeSeriesSemanticIds {
	public static final String TIME_SERIES = "https://admin-shell.io/idta/TimeSeries/1/1";
	public static final DefaultReference TIME_SERIES_REFERENCE = toExternalReference(TIME_SERIES);
	
	public static final String METADATA = "https://admin-shell.io/idta/TimeSeries/Metadata/1/1";
	public static final DefaultReference METADATA_REFERENCE = toExternalReference(METADATA);
	
	public static final String METADATA_NAME = "https://admin-shell.io/idta/TimeSeries/Metadata/Name/1/1";
	public static final DefaultReference METADATA_NAME_REFERENCE = toExternalReference(METADATA_NAME);
	
	public static final String METADATA_DESC = "https://admin-shell.io/idta/TimeSeries/Metadata/Description/1/1";
	public static final DefaultReference METADATA_DESC_REFERENCE = toExternalReference(METADATA_DESC);
	
	public static final String RECORD = "https://admin-shell.io/idta/TimeSeries/Record/1/1";
	public static final DefaultReference RECORD_REFERENCE = toExternalReference(RECORD);
	
	public static final String RECORDS = "https://admin-shell.io/idta/TimeSeries/Records/1/1";
	public static final DefaultReference RECORDS_REFERENCE = toExternalReference(RECORDS);
	
	public static final String EXTERNAL_SEGMENT = "https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1";
	public static final DefaultReference EXTERNAL_SEGMENT_REFERENCE = toExternalReference(EXTERNAL_SEGMENT);
	
	public static final String INTERNAL_SEGMENT = "https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1";
	public static final DefaultReference INTERNAL_SEGMENT_REFERENCE = toExternalReference(INTERNAL_SEGMENT);
	
	public static final String LINKED_SEGMENT = "https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1";
	public static final DefaultReference LINKED_SEGMENT_REFERENCE = toExternalReference(LINKED_SEGMENT);
	
	public static final String SEGMENTS = "https://admin-shell.io/idta/TimeSeries/Segments/1/1";
	public static final DefaultReference SEGMENTS_REFERENCE = toExternalReference(SEGMENTS);
	
	private static final DefaultReference toExternalReference(String value) {
		return new DefaultReference.Builder()
					.type(ReferenceTypes.EXTERNAL_REFERENCE)
					.keys(new DefaultKey.Builder()
										.type(KeyTypes.GLOBAL_REFERENCE)
										.value(value)
										.build())
					.build();
	}
}
