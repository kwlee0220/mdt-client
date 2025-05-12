package mdt.model.expr;

import java.io.IOException;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AssignmentExpr implements MDTExpr {
	private final MDTElementReferenceExpr m_lhs;
	private final MDTExpr m_rhs;
	private MDTInstanceManager m_instanceManager;

	public AssignmentExpr(MDTElementReferenceExpr lhs, MDTExpr rhs) {
		m_lhs = lhs;
		m_rhs = rhs;
	}
	
	public void activate(MDTInstanceManager instanceManager) {
		m_instanceManager = instanceManager;
	}

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
				literal.evaluate();
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
