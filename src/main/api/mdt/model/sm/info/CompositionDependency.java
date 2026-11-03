package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 * Twin composition에서 두 구성 항목({@link CompositionItem}) 사이의 의존 관계를 표현하는
 * SubmodelElementCollection 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionDependency {
	/**
	 * CompositionDependency의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.COMPOSITION_DEPENDENCY;
	/**
	 * CompositionDependency의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 의존 관계의 출발 구성 항목 식별자를 반환한다.
	 *
	 * @return	의존 관계의 출발 구성 항목 식별자.
	 */
	public String getSourceId();
	/**
	 * 의존 관계의 출발 구성 항목 식별자를 설정한다.
	 *
	 * @param src	의존 관계의 출발 구성 항목 식별자.
	 */
	public void setSourceId(String src);
	
	/**
	 * 의존 관계의 도착 구성 항목 식별자를 반환한다.
	 *
	 * @return	의존 관계의 도착 구성 항목 식별자.
	 */
	public String getTargetId();
	/**
	 * 의존 관계의 도착 구성 항목 식별자를 설정한다.
	 *
	 * @param tar	의존 관계의 도착 구성 항목 식별자.
	 */
	public void setTargetId(String tar);
	
	/**
	 * 의존 관계 타입을 반환한다.
	 *
	 * @return	의존 관계 타입.
	 */
	public String getDependencyType();
	/**
	 * 의존 관계 타입을 설정한다.
	 *
	 * @param type	의존 관계 타입.
	 */
	public void setDependencyType(String type);
}
