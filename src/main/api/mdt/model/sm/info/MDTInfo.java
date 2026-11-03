package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 * InformationModel Submodel의 {@code MDTInfo} SubmodelElementCollection 인터페이스.
 * <p>
 * 트윈화 대상 자산의 타입, 이름과 MDT 모델의 상태를 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInfo {
	/**
	 * MDTInfo의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.MDT_INFO;
	/**
	 * MDTInfo의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * {@code MDTInfo} SubmodelElementCollection의 idShort를 반환한다.
	 *
	 * @return idShort.
	 */
	public String getIdShort();
	
	/**
	 * {@code MDTInfo} SubmodelElementCollection의 idShort를 설정한다.
	 *
	 * @param idShort 설정할 idShort.
	 */
	public void setIdShort(String idShort);
	
	/**
	 * 트윈화 대상 자산 타입을 반환한다.
	 *
	 * @return 자산 타입.
	 */
	public MDTAssetType getAssetType();
	
	/**
	 * 트윈화 대상 자산 타입을 설정한다.
	 *
	 * @param type 설정할 자산 타입.
	 */
	public void setAssetType(MDTAssetType type);
	
	/**
	 * 트윈화 대상 자산 이름을 반환한다.
	 *
	 * @return 자산 이름.
	 */
	public String getAssetName();
	
	/**
	 * 트윈화 대상 자산 이름을 설정한다.
	 *
	 * @param name 설정할 자산 이름.
	 */
	public void setAssetName(String name);

	/**
	 * MDT 모델의 현재 상태를 반환한다.
	 *
	 * @return	현재 상태.
	 */
	public MDTAssetStatus getStatus();
	
	/**
	 * MDT 모델의 현재 상태를 설정한다.
	 *
	 * @param status 설정할 상태.
	 */
	public void setStatus(MDTAssetStatus status);
}
