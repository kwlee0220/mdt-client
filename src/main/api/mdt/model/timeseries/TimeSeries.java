package mdt.model.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TimeSeries {
	public static final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
											= new DefaultReference.Builder()
																.type(ReferenceTypes.MODEL_REFERENCE)
																.keys(new DefaultKey.Builder()
																					.type(KeyTypes.SUBMODEL)
																					.value(SEMANTIC_ID)
																					.build())
																.build();
	
	/**
	 * 본 시계열 데이터에 포함된 레코드의 메타데이터를 반환한다.
	 *
	 * @return	레코드의 메타데이터
	 */
	public Metadata getMetadata();
	
	/**
	 * 본 시계열 데이터에 포함된 segment (구간)들을 반환한다.
	 *
	 * @return	구간들
	 */
	public Segments getSegments();
}
