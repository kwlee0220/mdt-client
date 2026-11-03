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
 * {@link Segment}의 공통 속성을 구현하는 기본 클래스.
 * <p>
 * 각 속성은 같은 이름(첫 글자 대문자)의 idShort를 갖는 SubmodelElement와 매핑되며,
 * getter/setter는 Lombok으로 생성된다.
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
