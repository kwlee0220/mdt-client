package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.sm.data.Data;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultTimeSeries extends SubmodelEntity implements TimeSeries {
	private static final String IDSHORT = "TimeSeries";
	
	@SMCollectionField(idShort="Metadata") private Metadata metadata;
	@SMCollectionField(idShort="Segments") private Segments segments;
	
	public static DefaultTimeSeries from(Submodel submodel) {
		DefaultTimeSeries timeseries = new DefaultTimeSeries();
		timeseries.updateFromAasModel(submodel);
		
		return timeseries;
	}
	
	public DefaultTimeSeries() {
		setIdShort(IDSHORT);
		setSemanticId(Data.SEMANTIC_ID_REFERENCE);
	}
	
	public Metadata getMetadata() {
		return this.metadata;
	}
	
	public Segments getSegments() {
		return this.segments;
	}
}
