package mdt.model.timeseries;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.MultiLanguagePropertyField;
import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.model.sm.value.MultiLanguagePropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSegment extends SubmodelElementCollectionEntity implements Segment {
	@MultiLanguagePropertyField(idShort="Name") private MultiLanguagePropertyValue name;
	@MultiLanguagePropertyField(idShort="Description") private MultiLanguagePropertyValue description;
	@PropertyField(idShort="RecordCount") private long recordCount;
	@PropertyField(idShort="StartTime") private Instant startTime;
	@PropertyField(idShort="EndTime") private Instant endTime;
	@PropertyField(idShort="Duration") private Duration duration;
	@PropertyField(idShort="SamplingInterval") private Long samplingInterval;
	@PropertyField(idShort="SamplingRate") private Long samplingRate;
	@PropertyField(idShort="State") private String state;
	@PropertyField(idShort="LastUpdate") private Instant lastUpdate;
}
