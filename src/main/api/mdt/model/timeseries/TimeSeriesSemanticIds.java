package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 * IDTA TimeSeries Submodel (버전 1.1)에서 사용되는 semanticId 상수들을 정의한다.
 * <p>
 * 각 semanticId에 대해 문자열 상수와, 이를 {@link ReferenceTypes#EXTERNAL_REFERENCE} 타입의
 * {@link KeyTypes#GLOBAL_REFERENCE} key 하나로 구성한 참조({@code *_REFERENCE}) 상수를 함께 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class TimeSeriesSemanticIds {
	private TimeSeriesSemanticIds() {
		throw new AssertionError("Should not be called: class=" + TimeSeriesSemanticIds.class.getName());
	}
	
	/** TimeSeries Submodel의 semanticId. */
	public static final String TIME_SERIES = "https://admin-shell.io/idta/TimeSeries/1/1";
	/** TimeSeries Submodel의 semanticId 참조. */
	public static final DefaultReference TIME_SERIES_REFERENCE = toExternalReference(TIME_SERIES);
	
	/** Metadata의 semanticId. */
	public static final String METADATA = "https://admin-shell.io/idta/TimeSeries/Metadata/1/1";
	/** Metadata의 semanticId 참조. */
	public static final DefaultReference METADATA_REFERENCE = toExternalReference(METADATA);
	
	/** Metadata Name 속성의 semanticId. */
	public static final String METADATA_NAME = "https://admin-shell.io/idta/TimeSeries/Metadata/Name/1/1";
	/** Metadata Name 속성의 semanticId 참조. */
	public static final DefaultReference METADATA_NAME_REFERENCE = toExternalReference(METADATA_NAME);
	
	/** Metadata Description 속성의 semanticId. */
	public static final String METADATA_DESC = "https://admin-shell.io/idta/TimeSeries/Metadata/Description/1/1";
	/** Metadata Description 속성의 semanticId 참조. */
	public static final DefaultReference METADATA_DESC_REFERENCE = toExternalReference(METADATA_DESC);
	
	/** Record의 semanticId. */
	public static final String RECORD = "https://admin-shell.io/idta/TimeSeries/Record/1/1";
	/** Record의 semanticId 참조. */
	public static final DefaultReference RECORD_REFERENCE = toExternalReference(RECORD);
	
	/** Records의 semanticId. */
	public static final String RECORDS = "https://admin-shell.io/idta/TimeSeries/Records/1/1";
	/** Records의 semanticId 참조. */
	public static final DefaultReference RECORDS_REFERENCE = toExternalReference(RECORDS);
	
	/** ExternalSegment의 semanticId. */
	public static final String EXTERNAL_SEGMENT = "https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1";
	/** ExternalSegment의 semanticId 참조. */
	public static final DefaultReference EXTERNAL_SEGMENT_REFERENCE = toExternalReference(EXTERNAL_SEGMENT);
	
	/** InternalSegment의 semanticId. */
	public static final String INTERNAL_SEGMENT = "https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1";
	/** InternalSegment의 semanticId 참조. */
	public static final DefaultReference INTERNAL_SEGMENT_REFERENCE = toExternalReference(INTERNAL_SEGMENT);
	
	/** LinkedSegment의 semanticId. */
	public static final String LINKED_SEGMENT = "https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1";
	/** LinkedSegment의 semanticId 참조. */
	public static final DefaultReference LINKED_SEGMENT_REFERENCE = toExternalReference(LINKED_SEGMENT);
	
	/** Segments의 semanticId. */
	public static final String SEGMENTS = "https://admin-shell.io/idta/TimeSeries/Segments/1/1";
	/** Segments의 semanticId 참조. */
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
