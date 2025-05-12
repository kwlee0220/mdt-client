package mdt.model.timeseries;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.data.Data;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultTimeSeries extends SubmodelEntity implements TimeSeries {
	private static final String IDSHORT = "TimeSeries";
	
	@SMCollectionField(idShort="Metadata") private Metadata metadata;
	@SMCollectionField(idShort="Segments") private Segments segments;
	
	public DefaultTimeSeries() {
		setIdShort(IDSHORT);
		setSemanticId(Data.SEMANTIC_ID_REFERENCE);
	}
}
