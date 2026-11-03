package mdt.model.timeseries;

import java.time.Duration;
import java.time.Instant;

import org.jetbrains.annotations.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.sm.value.MultiLanguagePropertyValue;


/**
 * 시계열 데이터의 한 구간(segment)을 정의한다.
 * <p>
 * IDTA TimeSeries Submodel의 segment 공통 속성들을 정의하며, 레코드의 저장 방식에 따라
 * {@link InternalSegment}, {@link LinkedSegment}, {@link ExternalSegment}로 세분화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Segment {
	
//	/**
//	 * Segment에 저장된 레코드들의 메타데이터를 반환한다.
//	 *
//	 * @return 레코드 메타데이터
//	 */
//	public RecordMetadata getRecordMetadata();
	
	/**
	 * Segment의 이름을 반환한다.
	 *
	 * @return	Segment 이름. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public MultiLanguagePropertyValue getName();

	/**
	 * Segment의 설명을 반환한다.
	 *
	 * @return	Segment 설명. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public MultiLanguagePropertyValue getDescription();

	/**
	 * Segment에 포함된 레코드의 개수를 반환한다.
	 *
	 * @return	레코드 개수.
	 */
	public long getRecordCount();

	/**
	 * Segment에 포함된 첫번째 레코드의 시각을 반환한다.
	 *
	 * @return	시작 시각. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public Instant getStartTime();

	/**
	 * Segment에 포함된 마지막 레코드의 시각을 반환한다.
	 *
	 * @return	종료 시각. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public Instant getEndTime();

	/**
	 * Segment가 포함하는 시간 구간의 길이를 반환한다.
	 *
	 * @return	시간 구간의 길이. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public Duration getDuration();

	/**
	 * Segment에 포함된 레코드들의 샘플링 간격을 반환한다.
	 *
	 * @return	샘플링 간격. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public Long getSamplingInterval();

	/**
	 * Segment에 포함된 레코드들의 샘플링 비율(단위 시간당 레코드 수)을 반환한다.
	 *
	 * @return	샘플링 비율. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public Long getSamplingRate();

	/**
	 * Segment의 상태를 반환한다.
	 * <p>
	 * 일반적으로 {@link SegmentState}의 상수 이름이 사용된다.
	 *
	 * @return	Segment 상태 문자열.
	 */
	public String getState();

	/**
	 * Segment가 마지막으로 갱신된 시각을 반환한다.
	 *
	 * @return	마지막 갱신 시각. 지정되지 않은 경우 {@code null}.
	 */
	@Nullable public Instant getLastUpdate();
}
