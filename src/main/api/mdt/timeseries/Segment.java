package mdt.timeseries;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Segment {
	@Nullable public MultiLanguageProperty getName();
	@Nullable public MultiLanguageProperty getDescription();
	@Nullable public long getRecordCount();
	@Nullable public Instant getStartTime();
	@Nullable public Instant getEndTime();
	@Nullable public Duration getDuration();
	@Nullable public Long getSamplingInterval();
	@Nullable public Long getSamplingRate();
	@Nullable public String getState();
	@Nullable public Instant getLastUpdate();
}
