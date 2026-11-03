package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 * {@link TimeSeries}의 기본 구현 클래스.
 * <p>
 * idShort는 {@code "TimeSeries"}이며, 메타데이터와 segment 집합은 각각 {@code Metadata},
 * {@code Segments} idShort의 SubmodelElementCollection과 매핑된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultTimeSeries extends SubmodelEntity implements TimeSeries {
	private static final String IDSHORT = "TimeSeries";
	
	@SMCollectionField(idShort="Metadata", adaptorClass=DefaultMetadata.class) private Metadata metadata;
	@SMCollectionField(idShort="Segments", adaptorClass=DefaultSegments.class) private Segments segments;
	
	/**
	 * 주어진 Submodel로부터 {@link DefaultTimeSeries} 객체를 생성한다.
	 *
	 * @param submodel	시계열 Submodel.
	 * @return	생성된 {@link DefaultTimeSeries} 객체.
	 */
	public static DefaultTimeSeries from(Submodel submodel) {
		DefaultTimeSeries timeseries = new DefaultTimeSeries();
		timeseries.updateFromAasModel(submodel);
		
		return timeseries;
	}
	
	/**
	 * idShort와 semanticId({@link TimeSeries#SEMANTIC_ID_REFERENCE})가 설정된 빈 TimeSeries 객체를 생성한다.
	 */
	public DefaultTimeSeries() {
		setIdShort(IDSHORT);
		setSemanticId(TimeSeries.SEMANTIC_ID_REFERENCE);
	}
	
	public Metadata getMetadata() {
		return this.metadata;
	}
	
	/**
	 * 시계열 메타데이터를 설정한다.
	 *
	 * @param metadata	시계열 메타데이터.
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	
	public Segments getSegments() {
		return this.segments;
	}
	
	/**
	 * 시계열 segment 집합을 설정한다.
	 *
	 * @param segments	segment 집합.
	 */
	public void setSegments(Segments segments) {
		this.segments = segments;
	}
}
