package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;


/**
 * {@code Data}는 MDT의 Data 서브모델을 정의한다.
 * <p>
 * Data 서브모델은 서브모델 메타 정보 ({@link SubModelInfo})와 설비/공정 등의 데이터 엔티티를 담는
 * {@link DataInfo}로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Data {
	/**
	 * Data 서브모델의 semantic id 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_DATA;
	/**
	 * {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}.
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 서브모델 메타 정보 ({@code SubModelInfo})를 반환한다.
	 *
	 * @return	{@link SubModelInfo} 객체.
	 */
	public SubModelInfo getSubModelInfo();
	/**
	 * 데이터 엔티티들을 담은 {@code DataInfo}를 반환한다.
	 *
	 * @return	{@link DataInfo} 객체.
	 */
	public DataInfo getDataInfo();
}
