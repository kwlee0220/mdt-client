package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import utils.Keyed;

import mdt.model.MDTSemanticIds;

/**
 * Twin composition을 구성하는 항목(하위 트윈 등)을 표현하는 SubmodelElementCollection 인터페이스.
 * <p>
 * {@link Keyed}의 키는 구성 항목 식별자({@link #getID()})이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionItem extends Keyed<String> {
	/**
	 * CompositionItem의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.COMPOSITION_ITEM;
	/**
	 * CompositionItem의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 이 구성 항목의 키를 반환한다. {@link #getID()}와 동일하다.
	 *
	 * @return	구성 항목 식별자.
	 */
	public default String key() {
		return getID();
	}
	
	/**
	 * 구성 항목 식별자를 반환한다.
	 *
	 * @return	구성 항목 식별자.
	 */
	public String getID();
	/**
	 * 구성 항목 식별자를 설정한다.
	 *
	 * @param id	구성 항목 식별자.
	 */
	public void setID(String id);
	
	/**
	 * 구성 항목이 가리키는 대상 참조 문자열을 반환한다.
	 *
	 * @return	구성 항목이 가리키는 대상 참조 문자열.
	 */
	public String getReference();
	/**
	 * 구성 항목이 가리키는 대상 참조 문자열을 설정한다.
	 *
	 * @param ref	구성 항목이 가리키는 대상 참조 문자열.
	 */
	public void setReference(String ref);
	
	/**
	 * 구성 항목 설명을 반환한다.
	 *
	 * @return	구성 항목 설명.
	 */
	public String getDescription();
	/**
	 * 구성 항목 설명을 설정한다.
	 *
	 * @param desc	구성 항목 설명.
	 */
	public void setDescription(String desc);
}
