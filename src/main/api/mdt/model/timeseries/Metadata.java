package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import mdt.model.sm.value.MultiLanguagePropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Metadata {
	public static final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Metadata/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();

	public static final String SEMANTIC_ID_NAME = "ttps://admin-shell.io/idta/TimeSeries/Metadata/Name/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE_NAME
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID_NAME)
									.build())
				.build();

	public static final String SEMANTIC_ID_DESC = "ttps://admin-shell.io/idta/TimeSeries/Metadata/Description/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE_DESC
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID_DESC)
									.build())
				.build();
	
	/**
	 * Metadata의 이름을 반환한다.
	 *
	 * @return	Metadata의 이름
	 */
	public MultiLanguagePropertyValue getName();
	
	/**
	 * Metadata의 설명을 반환한다.
	 *
	 * @return Metadata의 설명
	 */
	public MultiLanguagePropertyValue getDescription();
	
	/**
	 * Record의 metadata를 반환한다.
	 * <p>
	 * RecordMetadata는 Record의 metadata를 정의하는 객체로, Record를 구성하는
	 * 필드의 이름과 타입으로 구성된다.
	 *
	 * @return	Record의 metadata
	 */
	public RecordMetadata getRecordMetadata();
}
