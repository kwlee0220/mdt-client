package mdt.model.expr;

/**
 * MDT 표현식({@code MdtExpr} 문법)을 파싱하여 생성된 표현식 노드의 공통 인터페이스이다.
 * <p>
 * 표현식은 {@link MDTExpressionParser}로 생성되며, {@link #evaluate()}로 표현식이 나타내는 값
 * (리터럴 값, Submodel/SubmodelElement 참조 등)을 얻는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTExpression {
	/**
	 * 표현식을 평가하여 그 결과를 반환한다.
	 *
	 * @return	평가 결과. 결과의 타입은 표현식 종류에 따라 다르다.
	 */
	public Object evaluate();
}
