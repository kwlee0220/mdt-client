package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 * MDT 인스턴스의 기본 정보를 담는 InformationModel Submodel 인터페이스.
 * <p>
 * Submodel은 {@code MDTInfo}와 {@code TwinComposition} SubmodelElementCollection으로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InformationModel {
	/**
	 * InformationModel Submodel의 기본 idShort.
	 */
	public static final String IDSHORT = "InformationModel";
	/**
	 * InformationModel Submodel의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_INFO;
	/**
	 * InformationModel Submodel의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * MDT 정보({@code MDTInfo} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	MDT 정보({@code MDTInfo} SubmodelElementCollection).
	 */
	public MDTInfo getMDTInfo();
	/**
	 * MDT 정보({@code MDTInfo} SubmodelElementCollection)를 설정한다.
	 *
	 * @param info	MDT 정보({@code MDTInfo} SubmodelElementCollection).
	 */
	public void setMDTInfo(MDTInfo info);
	
	/**
	 * Twin composition 정보({@code TwinComposition} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	Twin composition 정보({@code TwinComposition} SubmodelElementCollection).
	 */
	public TwinComposition getTwinComposition();
	/**
	 * Twin composition 정보({@code TwinComposition} SubmodelElementCollection)를 설정한다.
	 *
	 * @param composition	Twin composition 정보({@code TwinComposition} SubmodelElementCollection).
	 */
	public void setTwinComposition(TwinComposition composition);
}
