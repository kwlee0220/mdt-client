package mdt.model.expr;

/**
 * 문자열, 정수, 실수, 불리언 등 문법의 단말 토큰(terminal)에 해당하는 표현식들의 기반 클래스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class TerminalExpr implements MDTExpression {
	/**
	 * 단말 토큰의 값을 반환한다.
	 *
	 * @return	토큰 값.
	 */
	public abstract Object evaluate();
	
	/** 문자열 단말 표현식. 따옴표 문자열(따옴표 제거 후)이나 식별자({@code ID}) 토큰에 해당한다. */
	public static class StringExpr extends TerminalExpr implements MDTExpression {
		private final String m_str;

		/**
		 * 주어진 문자열로 표현식을 생성한다.
		 *
		 * @param str	문자열 값.
		 */
		public StringExpr(String str) {
			m_str = str;
		}

		@Override
		public String evaluate() {
			return m_str;
		}
		
	}
	
	/** 문법 키워드나 구분 기호 등 그 밖의 토큰을 토큰 텍스트 그대로 담는 문자열 표현식. */
	public static class SymbolExpr extends StringExpr implements MDTExpression {
		/**
		 * 주어진 토큰 텍스트로 표현식을 생성한다.
		 *
		 * @param str	토큰 텍스트.
		 */
		public SymbolExpr(String str) {
			super(str);
		}
	}
	
	/** 정수 단말 표현식. */
	public static class IntegerExpr extends TerminalExpr implements MDTExpression {
		private final Integer m_id;

		/**
		 * 주어진 정수 값으로 표현식을 생성한다.
		 *
		 * @param id	정수 값.
		 */
		public IntegerExpr(Integer id) {
			m_id = id;
		}

		@Override
		public Integer evaluate() {
			return m_id;
		}
	}
	
	/** 실수 단말 표현식. */
	public static class DoubleExpr extends TerminalExpr implements MDTExpression {
		private final Double m_value;

		/**
		 * 주어진 실수 값으로 표현식을 생성한다.
		 *
		 * @param value	실수 값.
		 */
		public DoubleExpr(Double value) {
			m_value = value;
		}

		@Override
		public Double evaluate() {
			return m_value;
		}
	}

	/** 불리언 단말 표현식. */
	public static class BooleanExpr extends TerminalExpr implements MDTExpression {
		private final Boolean m_value;

		/**
		 * 주어진 불리언 값으로 표현식을 생성한다.
		 *
		 * @param value	불리언 값.
		 */
		public BooleanExpr(Boolean value) {
			m_value = value;
		}

		@Override
		public Boolean evaluate() {
			return m_value;
		}
	}
}
