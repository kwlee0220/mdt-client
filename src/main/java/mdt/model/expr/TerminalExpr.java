package mdt.model.expr;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class TerminalExpr implements MDTExpr {
	public abstract Object evaluate();
	
	public static class StringExpr extends TerminalExpr implements MDTExpr {
		private final String m_str;

		public StringExpr(String str) {
			m_str = str;
		}

		@Override
		public String evaluate() {
			return m_str;
		}
		
	}
	
	public static class SymbolExpr extends StringExpr implements MDTExpr {
		public SymbolExpr(String str) {
			super(str);
		}
	}
	
	public static class IntegerExpr extends TerminalExpr implements MDTExpr {
		private final Integer m_id;

		public IntegerExpr(Integer id) {
			m_id = id;
		}

		@Override
		public Integer evaluate() {
			return m_id;
		}
	}
	
	public static class DoubleExpr extends TerminalExpr implements MDTExpr {
		private final Double m_value;

		public DoubleExpr(Double value) {
			m_value = value;
		}

		@Override
		public Double evaluate() {
			return m_value;
		}
	}
}
