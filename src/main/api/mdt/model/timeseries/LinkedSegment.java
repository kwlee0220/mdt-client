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
public interface LinkedSegment extends Segment {
	public static final Reference SEMANTIC_ID
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value("https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1")
									.build())
				.build();
	
	/**
	 * Segment에 포함된 레코드들이 저장된 외부 저장소의 endpoint를 나타낸다.
	 *
	 * @return	외부 저장소의 endpoint
	 */
	public String getEndpoint();
	
	/**
	 * Segment에 포함된 레코드들을 조회하기 위한 질의문을 나타낸다.
	 *
	 * @return 질의문
	 */
	public String getQuery();
}
