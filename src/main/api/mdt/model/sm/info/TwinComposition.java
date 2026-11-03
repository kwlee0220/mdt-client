package mdt.model.sm.info;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 * InformationModel Submodel의 {@code TwinComposition} SubmodelElementCollection 인터페이스.
 * <p>
 * 트윈을 구성하는 항목({@link CompositionItem})들과 항목 간 의존 관계({@link CompositionDependency})를 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TwinComposition {
	/**
	 * TwinComposition의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.TWIN_COMPOSITION;
	/**
	 * TwinComposition의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * composition 식별자를 반환한다.
	 *
	 * @return	composition 식별자.
	 */
	public String getCompositionID();
	/**
	 * composition 식별자를 설정한다.
	 *
	 * @param compositionID	composition 식별자.
	 */
	public void setCompositionID(String compositionID);
	
	/**
	 * 이 composition의 키를 반환한다. {@link #getCompositionID()}와 동일하다.
	 *
	 * @return	composition 식별자.
	 */
	public default String getKey() {
		return getCompositionID();
	}

	/**
	 * composition 타입을 반환한다.
	 *
	 * @return	composition 타입.
	 */
	public String getCompositionType();
	/**
	 * composition 타입을 설정한다.
	 *
	 * @param compositionType	composition 타입.
	 */
	public void setCompositionType(String compositionType);

	/**
	 * 구성 항목 리스트를 반환한다.
	 *
	 * @return	구성 항목 리스트.
	 */
	public List<CompositionItem> getCompositionItems();
	/**
	 * 구성 항목 리스트를 설정한다.
	 *
	 * @param compositionItems	구성 항목 리스트.
	 */
	public void setCompositionItems(List<CompositionItem> compositionItems);

	/**
	 * 구성 항목 간 의존 관계 리스트를 반환한다.
	 *
	 * @return	구성 항목 간 의존 관계 리스트.
	 */
	public List<CompositionDependency> getCompositionDependencies();
	/**
	 * 구성 항목 간 의존 관계 리스트를 설정한다.
	 *
	 * @param compositionDependencies	구성 항목 간 의존 관계 리스트.
	 */
	public void setCompositionDependencies(List<CompositionDependency> compositionDependencies);
}
