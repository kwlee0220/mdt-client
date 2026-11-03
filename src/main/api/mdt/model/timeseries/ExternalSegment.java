package mdt.model.timeseries;

import org.jetbrains.annotations.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * 레코드들이 외부 파일에 저장된 시계열 segment(ExternalSegment)를 정의한다.
 * <p>
 * IDTA TimeSeries Submodel의 {@code ExternalSegment}에 해당한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ExternalSegment extends Segment {
	/** ExternalSegment의 semanticId 참조. */
	public static final Reference SEMANTIC_ID = TimeSeriesSemanticIds.EXTERNAL_SEGMENT_REFERENCE;
	
	/**
	 * Segment의 레코드들이 저장된 외부 파일을 반환한다.
	 *
	 * @return	레코드 데이터 파일을 나타내는 {@link File} SubmodelElement. 없는 경우 {@code null}.
	 */
	@Nullable public File getData();
}
