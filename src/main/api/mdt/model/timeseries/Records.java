package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Records {
	public static final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Records/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
//	/**
//	 * 레코드의 메타데이터를 반환한다.
//	 *
//	 * @return	레코드의 메타데이터
//	 */
//	public RecordMetadata getRecordMetadata();
	
	/**
	 * 레코드 리스트를 반환한다.
	 *
	 * @return 레코드 리스트
	 */
	public List<? extends Record> getRecordList();
}
