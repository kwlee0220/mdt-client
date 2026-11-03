package mdt.model.expr;

import java.io.IOException;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;

/**
 * SubmodelElement 참조에 값을 대입하는 대입 표현식({@code <fullElementSpec> = <value>})이다.
 * <p>
 * 좌변은 대상 SubmodelElement 참조이고, 우변은 다른 SubmodelElement 참조 또는 값 리터럴이다.
 * 평가 전에 {@link #activate(MDTInstanceManager)}로 참조 해석에 사용할 {@link MDTInstanceManager}를
 * 설정해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AssignmentExpr implements MDTExpression {
	private final MDTElementReferenceExpr m_lhs;
	private final MDTExpression m_rhs;
	private MDTInstanceManager m_instanceManager;

	/**
	 * 대입 표현식을 생성한다.
	 *
	 * @param lhs	값이 대입될 SubmodelElement 참조 표현식.
	 * @param rhs	대입할 값을 나타내는 표현식 ({@link MDTElementReferenceExpr} 또는 {@link LiteralExpr}).
	 */
	public AssignmentExpr(MDTElementReferenceExpr lhs, MDTExpression rhs) {
		m_lhs = lhs;
		m_rhs = rhs;
	}
	
	/**
	 * 평가 시 참조 해석에 사용할 {@link MDTInstanceManager}를 설정한다.
	 *
	 * @param instanceManager	MDTInstance 관리자.
	 */
	public void activate(MDTInstanceManager instanceManager) {
		m_instanceManager = instanceManager;
	}

	/**
	 * 대입을 수행하고 좌변 참조를 반환한다.
	 * <p>
	 * 좌변과 (참조인 경우) 우변 참조를 활성화한 뒤, 우변의 값으로 좌변 SubmodelElement의 값을 갱신한다.
	 * 우변이 참조이면 그 SubmodelElement의 현재 값을, 값 리터럴이면 리터럴을 평가한 값을 사용한다.
	 *
	 * @return	값이 갱신된 좌변 SubmodelElement 참조.
	 * @throws IllegalArgumentException	우변이 참조나 값 리터럴이 아닌 경우.
	 * @throws MDTEvaluationException	값 읽기 또는 갱신 중 입출력 오류가 발생한 경우.
	 */
	@Override
	public MDTElementReference evaluate() {
		try {
			MDTElementReference holder = m_lhs.evaluate();
			holder.activate(m_instanceManager);
			
			ElementValue value = null;
			if ( m_rhs instanceof MDTElementReferenceExpr ref ) {
				MDTElementReference rhsVar = ref.evaluate();
				rhsVar.activate(m_instanceManager);
				value = rhsVar.readValue();
			}
			else if ( m_rhs instanceof LiteralExpr literal ) {
				value = literal.evaluate();
			}
			else {
				throw new IllegalArgumentException("Invalid RHS in assignment: " + m_rhs);
			}
			holder.updateValue(value);
			
			return holder;
		}
		catch ( IOException e ) {
			throw new MDTEvaluationException("Failed to assignment", e);
		}
	}
}
