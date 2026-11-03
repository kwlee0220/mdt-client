package mdt.model.timeseries;

import java.time.Duration;
import java.util.List;

import mdt.model.sm.entity.SMCollectionField;


/**
 * {@link InternalSegment}의 기본 구현 클래스.
 * <p>
 * 레코드 집합은 {@code Records} idShort의 SubmodelElementCollection과 매핑된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultInternalSegment extends DefaultSegment implements InternalSegment {
	@SMCollectionField(idShort="Records") private DefaultRecords records;
	
	/**
	 * 빈 InternalSegment 객체를 생성한다.
	 * <p>
	 * semanticId는 {@link InternalSegment#SEMANTIC_ID}로 설정된다.
	 */
	public DefaultInternalSegment() {
		setSemanticId(InternalSegment.SEMANTIC_ID);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @return	Record들의 집합. 설정되지 않은 경우 {@code null}.
	 */
	public DefaultRecords getRecords() {
		return this.records;
	}
	
	/**
	 * Segment의 레코드 집합을 설정하고, 이로부터 segment 속성들을 갱신한다.
	 * <p>
	 * 레코드 개수(RecordCount)를 설정하고, 레코드가 있는 경우 첫번째/마지막 레코드의 타임스탬프로
	 * StartTime/EndTime/LastUpdate를 설정한다. 레코드가 2개 이상인 경우에는 Duration을 설정하고, Duration이
	 * 0보다 크면 SamplingInterval(밀리초 단위, {@code duration / (레코드 수 - 1)})과 SamplingRate
	 * (초당 레코드 수, {@code (레코드 수 - 1) * 1000 / duration}, 정수 나눗셈)도 설정한다.
	 * 상태(State)는 항상 {@link SegmentState#IN_PREGRESS}로 설정된다.
	 * 레코드들은 타임스탬프 순으로 정렬되어 있다고 가정한다.
	 *
	 * @param records	설정할 레코드 집합.
	 */
	public void setRecords(DefaultRecords records) {
		List<? extends DefaultRecord> recList = records.getRecordList();
		
		setRecordCount(recList.size());
		if ( recList.size() > 0 ) {
			DefaultRecord first = recList.get(0);
			DefaultRecord last = recList.get(recList.size() - 1);

			setStartTime(first.getTimestamp());
			setEndTime(last.getTimestamp());
			setLastUpdate(last.getTimestamp());
			
			if ( recList.size() > 1 ) {
				Duration dur = Duration.between(getStartTime(), getEndTime());
				setDuration(dur);
				
				// 레코드 n개 사이의 간격은 n-1개이다. 모든 타임스탬프가 같으면(duration 0) 간격·빈도를 정할 수 없다.
				long millis = dur.toMillis();
				long intervals = recList.size() - 1;
				if ( millis > 0 ) {
					setSamplingInterval(millis / intervals);
					setSamplingRate(intervals * 1000 / millis);
				}
			}
		}
		setState(SegmentState.IN_PREGRESS.name());
		
		this.records = records;
	}
}
