package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInfo {
	public static final String SEMANTIC_ID = MDTSemanticIds.MDT_INFO;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 트윈화 대상 자산 ID를 반환한다.
	 *
	 * @return 자산 ID.
	 */
	public String getIdShort();
	
	/**
	 * 트윈화 대상 자산 ID를 설정한다.
	 *
	 * @param idShort 설정할 자산 ID.
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
