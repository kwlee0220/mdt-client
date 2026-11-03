package mdt.model.expr;

import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;

/**
 * Submodel 참조를 나타내는 표현식({@code submodelSpec})의 기반 클래스이다.
 * <p>
 * {@code <instance>:[idShort=]<idShort>} 형태는 {@link SubmodelByIdShortExpr}로,
 * {@code submodel:id=<id>} 형태는 {@link SubmodelByIdExpr}로 표현된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTSubmodelExpr implements MDTExpression {
	/**
	 * 표현식이 가리키는 Submodel 참조를 생성한다.
	 *
	 * @return	Submodel 참조.
	 */
	public abstract DefaultSubmodelReference evaluate();
	
	/** MDTInstance 식별자와 Submodel idShort로 Submodel을 지칭하는 표현식 ({@code <instance>:[idShort=]<idShort>}). */
	public static class SubmodelByIdShortExpr extends MDTSubmodelExpr implements MDTExpression {
		private final StringExpr m_instanceIdExpr;
		private final StringExpr m_idShortExpr;

		/**
		 * 표현식을 생성한다.
		 *
		 * @param instanceIdExpr	MDTInstance 식별자 표현식.
		 * @param idShortExpr	Submodel idShort 표현식.
		 */
		public SubmodelByIdShortExpr(StringExpr instanceIdExpr, StringExpr idShortExpr) {
			m_instanceIdExpr = instanceIdExpr;
			m_idShortExpr = idShortExpr;
		}

		@Override
		public ByIdShortSubmodelReference evaluate() {
			return DefaultSubmodelReference.ofIdShort(m_instanceIdExpr.evaluate(), m_idShortExpr.evaluate());
		}
	}
	
	/** Submodel 식별자로 Submodel을 지칭하는 표현식 ({@code submodel:id=<id>}). */
	public static class SubmodelByIdExpr extends MDTSubmodelExpr implements MDTExpression {
		private final StringExpr m_smIdExpr;

		/**
		 * 표현식을 생성한다.
		 *
		 * @param smIdExpr	Submodel 식별자 표현식.
		 */
		public SubmodelByIdExpr(StringExpr smIdExpr) {
			m_smIdExpr = smIdExpr;
		}

		@Override
		public DefaultSubmodelReference evaluate() {
			return DefaultSubmodelReference.ofId(m_smIdExpr.evaluate());
		}
	}
}
