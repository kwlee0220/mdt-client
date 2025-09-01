package mdt.model.expr;

import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTSubmodelExpr implements MDTExpression {
	public abstract DefaultSubmodelReference evaluate();
	
	public static class SubmodelByIdShortExpr extends MDTSubmodelExpr implements MDTExpression {
		private final StringExpr m_instanceIdExpr;
		private final StringExpr m_idShortExpr;

		public SubmodelByIdShortExpr(StringExpr instanceIdExpr, StringExpr idShortExpr) {
			m_instanceIdExpr = instanceIdExpr;
			m_idShortExpr = idShortExpr;
		}

		@Override
		public ByIdShortSubmodelReference evaluate() {
			return DefaultSubmodelReference.ofIdShort(m_instanceIdExpr.evaluate(), m_idShortExpr.evaluate());
		}
	}
	
	public static class SubmodelByIdExpr extends MDTSubmodelExpr implements MDTExpression {
		private final StringExpr m_smIdExpr;

		public SubmodelByIdExpr(StringExpr smIdExpr) {
			m_smIdExpr = smIdExpr;
		}

		@Override
		public DefaultSubmodelReference evaluate() {
			return DefaultSubmodelReference.ofId(m_smIdExpr.evaluate());
		}
	}
}
