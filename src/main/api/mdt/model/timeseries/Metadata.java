package mdt.model.timeseries;

import mdt.model.sm.value.MultiLanguagePropertyValue;

/**
 * 시계열 데이터의 메타데이터(Metadata)를 정의한다.
 * <p>
 * IDTA TimeSeries Submodel의 {@code Metadata}에 해당하며, 시계열의 이름과 설명, 그리고
 * 레코드의 구조({@link RecordMetadata})로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Metadata {
	/** Metadata의 semanticId. */
	public static final String SEMANTIC_ID = TimeSeriesSemanticIds.METADATA;
	
	/**
	 * Metadata의 이름을 반환한다.
	 *
	 * @return	Metadata의 이름.
	 */
	public MultiLanguagePropertyValue getName();
	
	/**
	 * Metadata의 설명을 반환한다.
	 *
	 * @return	Metadata의 설명.
	 */
	public MultiLanguagePropertyValue getDescription();
	
	/**
	 * Record의 metadata를 반환한다.
	 * <p>
	 * RecordMetadata는 Record의 metadata를 정의하는 객체로, Record를 구성하는
	 * 필드의 이름과 타입으로 구성된다.
	 *
	 * @return	Record의 metadata.
	 */
	public RecordMetadata getRecord();
}
