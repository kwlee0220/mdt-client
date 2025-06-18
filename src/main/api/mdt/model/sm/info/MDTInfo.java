package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInfo {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/InformationModel/MDTInfo/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
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
