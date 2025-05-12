package mdt.model.timeseries;

import java.time.Duration;
import java.util.List;

import mdt.model.sm.entity.SMCollectionField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultInternalSegment extends DefaultSegment implements InternalSegment {
	@SMCollectionField(idShort="Records") private DefaultRecords records;
	
	public DefaultInternalSegment() {
		setSemanticId(InternalSegment.SEMANTIC_ID);
	}
	
	public DefaultRecords getRecords() {
		return this.records;
	}
	
	public void setRecords(DefaultRecords records) {
		List<? extends DefaultRecord> recList = records.getRecordList();
		
		setRecordCount(recList.size());
		if ( recList.size() > 0 ) {
			DefaultRecord first = recList.getFirst();
			DefaultRecord last = recList.getLast();

			setStartTime(first.getTimestamp());
			setEndTime(last.getTimestamp());
			setLastUpdate(last.getTimestamp());
			
			if ( recList.size() > 1 ) {
				Duration dur = Duration.between(getStartTime(), getEndTime());
				setDuration(dur);
				setSamplingInterval(dur.toMillis() / recList.size());
				setSamplingRate(recList.size() * 1000 / dur.toMillis());
			}
		}
		setState(SegmentState.IN_PREGRESS.name());
		
		this.records = records;
	}
}
