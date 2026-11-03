package mdt.model.sm.entity;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * SubmodelElement와 상호 변환되는 엔티티 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelElementEntity extends AASModelEntity<SubmodelElement> {
	/**
	 * 대응되는 SubmodelElement의 idShort를 반환한다.
	 *
	 * @return	idShort.
	 */
	public String getIdShort();
	
	/**
	 * 이 엔티티의 내용을 담은 새 SubmodelElement를 생성한다.
	 *
	 * @return	생성된 SubmodelElement.
	 */
	public SubmodelElement newSubmodelElement();
}
