package mdt.model.timeseries;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

import mdt.model.sm.value.MultiLanguagePropertyValue;


/**
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
	
	@Nullable public MultiLanguagePropertyValue getName();
	@Nullable public MultiLanguagePropertyValue getDescription();
	public long getRecordCount();
	@Nullable public Instant getStartTime();
	@Nullable public Instant getEndTime();
	@Nullable public Duration getDuration();
	@Nullable public Long getSamplingInterval();
	@Nullable public Long getSamplingRate();
	public String getState();
	@Nullable public Instant getLastUpdate();
}
