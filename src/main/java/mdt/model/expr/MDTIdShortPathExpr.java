package mdt.model.expr;

import mdt.model.sm.value.IdShortPath;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTIdShortPathExpr implements MDTExpr {
	private final IdShortPath m_path;
	
	public MDTIdShortPathExpr(IdShortPath path) {
		m_path = path;
	}

	@Override
	public IdShortPath evaluate() {
		return m_path;
	}
}
