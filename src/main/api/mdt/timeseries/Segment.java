package mdt.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.PropertyUtils;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.model.sm.value.MultiLanguagePropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class Segment extends SubmodelElementCollectionEntity {
	private static final Reference SEMANTIC_ID
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
								.type(KeyTypes.GLOBAL_REFERENCE)
								.value("https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1")
								.build())
				.build();
	
	private String timeSeriesId;
	private String idShort;
	private MultiLanguagePropertyValue name;
//	private MultiLanguagePropertyValue description;
	private long recordCount;
	private Instant startTime;
	@Nullable private Instant endTime;
	@Nullable private Duration duration;
	@Nullable private Long samplingInterval;
	@Nullable private Long samplingRate;
	@Nullable private SegmentState state;
	@Nullable private Instant lastUpdate;
	
	public Segment() {
	}

	@Override
	public void updateFromAasModel(SubmodelElement model) {
		Preconditions.checkArgument(model instanceof SubmodelElementCollection);
		
//		SubmodelElementCollection smc = (SubmodelElementCollection)model;
	}

	@Override
	public SubmodelElementCollection newSubmodelElement() {
		List<SubmodelElement> elements = Lists.newArrayList();
		elements.add(PropertyUtils.STRING("idShort", this.idShort));
		if ( this.name != null ) {
			elements.add(PropertyUtils.MULTI_LANGUAGE("Name", this.name));
		}
//		if ( this.description != null ) {
//			elements.add(SubmodelUtils.newMultiLanguageProperty("Description", this.description));
//		}
		elements.add(PropertyUtils.LONG("RecordCount", this.recordCount));
		elements.add(PropertyUtils.DATE_TIME("StartTime", this.startTime));
		if ( this.endTime != null ) {
			elements.add(PropertyUtils.DATE_TIME("EndTime", this.endTime));
		}
		if ( this.duration != null ) {
			elements.add(PropertyUtils.DURATION("Duration", this.duration));
		}
		if ( this.samplingInterval != null ) {
			elements.add(PropertyUtils.LONG("SamplingInterval", this.samplingInterval));
		}
		if ( this.samplingRate != null ) {
			elements.add(PropertyUtils.LONG("SamplingRate", this.samplingRate));
		}
		if ( this.state != null ) {
			elements.add(PropertyUtils.STRING("State", this.state.name()));
		}
		if ( this.lastUpdate != null ) {
			elements.add(PropertyUtils.DATE_TIME("LastUpdate", this.lastUpdate));
		}
		
		return new DefaultSubmodelElementCollection.Builder()
						.idShort(this.idShort)
						.semanticId(SEMANTIC_ID)
						.value(elements)
						.build();
	}
}
