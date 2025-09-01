package mdt.model.expr;

import mdt.model.expr.TerminalExpr.IntegerExpr;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTParameterReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.ref.OperationVariableReference;
import mdt.model.sm.ref.OperationVariableReference.Kind;
import mdt.model.sm.value.IdShortPath;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTElementReferenceExpr implements MDTExpression {
	public abstract MDTElementReference evaluate();
	
	public static class DefaultElementReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTSubmodelReference m_smRef;
		private final IdShortPath m_idShortPath;

		public DefaultElementReferenceExpr(MDTSubmodelReference smRef, IdShortPath path) {
			m_smRef = smRef;
			m_idShortPath = path;
		}

		@Override
		public DefaultElementReference evaluate() {
			return DefaultElementReference.newInstance(m_smRef, m_idShortPath.toString());
		}
	}
	
	public static class ParameterReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final StringExpr m_instanceIdExpr;
		private final MDTExpression m_paramPath;
		
		public ParameterReferenceExpr(StringExpr instanceIdExpr, MDTExpression paramPath) {
			m_instanceIdExpr = instanceIdExpr;
			m_paramPath = paramPath;
		}

		@Override
		public MDTParameterReference evaluate() {
			return MDTParameterReference.newInstance(m_instanceIdExpr.evaluate(),
													m_paramPath.evaluate().toString());
		}
	}
	
	public static class ArgumentReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTSubmodelExpr m_smExpr;
		private final MDTArgumentKind m_kind;
		private final MDTExpression m_argNameExpr;
		
		public ArgumentReferenceExpr(MDTSubmodelExpr smExpr, MDTArgumentKind kind, MDTExpression argNameExpr) {
			m_smExpr = smExpr;
			m_kind = kind;
			m_argNameExpr = argNameExpr;
		}

		@Override
		public MDTArgumentReference evaluate() {
			return MDTArgumentReference.newInstance((DefaultSubmodelReference)m_smExpr.evaluate(),
													m_kind, "" + m_argNameExpr.evaluate());
		}
	}
	
	public static class OperationVariableReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTElementReferenceExpr m_opVarRef;
		private final Kind m_kind;
		private final IntegerExpr m_opVarIdx;
		
		public OperationVariableReferenceExpr(MDTElementReferenceExpr opVarRef, Kind kind,
												IntegerExpr opVarIdx) {
			m_opVarRef = opVarRef;
			m_kind = kind;
			m_opVarIdx = opVarIdx;
		}

		@Override
		public OperationVariableReference evaluate() {
			MDTElementReference opRef = m_opVarRef.evaluate();
			int idx = m_opVarIdx.evaluate();
			return OperationVariableReference.newInstance(opRef, m_kind, idx);
		}
	}
}
