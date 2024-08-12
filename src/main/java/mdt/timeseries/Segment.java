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
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.SubmodelUtils;
import mdt.model.resource.value.MultiLanguagePropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class Segment extends AbstractMDTSubmodelElementCollection {
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
	private MultiLanguagePropertyValue description;
	private long recordCount;
	private Instant startTime;
	@Nullable private Instant endTime;
	@Nullable private Duration duration;
	@Nullable private Long samplingInterval;
	@Nullable private Long samplingRate;
	@Nullable private SegmentState state;
	@Nullable private Instant lastUpdate;

	@Override
	public void fromAasModel(SubmodelElement model) {
		Preconditions.checkArgument(model instanceof SubmodelElementCollection);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
	}

	@Override
	public SubmodelElementCollection toAasModel() {
		List<SubmodelElement> elements = Lists.newArrayList();
		elements.add(SubmodelUtils.newStringProperty("idShort", this.idShort));
		if ( this.name != null ) {
			elements.add(SubmodelUtils.newMultiLanguageProperty("Name", this.name));
		}
		if ( this.description != null ) {
			elements.add(SubmodelUtils.newMultiLanguageProperty("Description", this.description));
		}
		elements.add(SubmodelUtils.newLongProperty("RecordCount", this.recordCount));
		elements.add(SubmodelUtils.newDateTimeProperty("StartTime", this.startTime));
		if ( this.endTime != null ) {
			elements.add(SubmodelUtils.newDateTimeProperty("EndTime", this.endTime));
		}
		if ( this.duration != null ) {
			elements.add(SubmodelUtils.newDurationProperty("Duration", this.duration));
		}
		if ( this.samplingInterval != null ) {
			elements.add(SubmodelUtils.newLongProperty("SamplingInterval", this.samplingInterval));
		}
		if ( this.samplingRate != null ) {
			elements.add(SubmodelUtils.newLongProperty("SamplingRate", this.samplingRate));
		}
		if ( this.state != null ) {
			elements.add(SubmodelUtils.newStringProperty("State", this.state.name()));
		}
		if ( this.lastUpdate != null ) {
			elements.add(SubmodelUtils.newDateTimeProperty("LastUpdate", this.lastUpdate));
		}
		
		return new DefaultSubmodelElementCollection.Builder()
						.idShort(this.idShort)
						.semanticId(SEMANTIC_ID)
						.value(elements)
						.build();
	}
}
