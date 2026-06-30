package mdt.model.timeseries;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.Throwables;
import utils.stream.FStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.sm.data.Data;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;
import mdt.model.sm.value.PropertyValue;

import ch.qos.logback.core.util.Duration;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultTimeSeries extends SubmodelEntity implements TimeSeries {
	private static final String IDSHORT = "TimeSeries";
	
	@SMCollectionField(idShort="Metadata", adaptorClass=DefaultMetadata.class) private Metadata metadata;
	@SMCollectionField(idShort="Segments", adaptorClass=DefaultSegments.class) private Segments segments;
	
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
	
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	
	public Segments getSegments() {
		return this.segments;
	}
	
	public void setSegments(Segments segments) {
		this.segments = segments;
	}
}
