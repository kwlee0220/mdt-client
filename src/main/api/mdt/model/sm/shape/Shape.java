package mdt.model.sm.shape;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.AASModelEntity;


/**
 * 자산의 형상(shape) 정보를 기술하는 Shape Submodel 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Shape extends AASModelEntity<Submodel> {
	/**
	 * Shape Submodel의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_SHAPE;
	/**
	 * Shape Submodel의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * Submodel 정보({@code SubModelInfo} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	Submodel 정보.
	 */
	public SubModelInfo getSubModelInfo();
	/**
	 * Shape 정보를 반환한다.
	 *
	 * @return	Shape 정보.
	 */
	public ShapeInfo getShapeInfo();
}
