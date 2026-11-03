package mdt.model.expr;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;

import mdt.aas.DataType;
import mdt.model.expr.TerminalExpr.BooleanExpr;
import mdt.model.expr.TerminalExpr.DoubleExpr;
import mdt.model.expr.TerminalExpr.IntegerExpr;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.FileValue;
import mdt.model.sm.value.MultiLanguagePropertyValue;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.value.RangeValue;

/**
 * SubmodelElement 값 리터럴({@code valueLiteralSpec})을 나타내는 표현식의 기반 클래스이다.
 * <p>
 * 평가 결과는 리터럴 종류에 따른 {@link ElementValue}이다.
 * <ul>
 *   <li>{@link PropertyValueSpec}: 문자열/정수/실수/불리언 Property 값
 *       (예: {@code 'abc'}, {@code -3}, {@code 1.5}, {@code true})</li>
 *   <li>{@link MLPropertyValueSpec}: 다국어 Property 값 (예: {@code "text"@ko})</li>
 *   <li>{@link FileValueSpec}: File 값 (예: {@code file:"image/png"("a.png")})</li>
 *   <li>{@link RangeValueSpec}: Range 값 (예: {@code [0, 10]})</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class LiteralExpr implements MDTExpression {
	/**
	 * 리터럴이 나타내는 SubmodelElement 값을 반환한다.
	 *
	 * @return	SubmodelElement 값.
	 */
	public abstract ElementValue evaluate();
	
	/**
	 * 단말 토큰 하나로 표현되는 Property 값 리터럴.
	 * <p>
	 * 단말 토큰 종류에 따라 문자열, 정수, 실수, 불리언 타입의 {@link PropertyValue}로 평가된다.
	 */
	public static class PropertyValueSpec extends LiteralExpr {
		private final TerminalExpr m_terminal;
	
		/**
		 * 리터럴을 생성한다.
		 *
		 * @param terminal	값을 나타내는 단말 표현식.
		 */
		public PropertyValueSpec(TerminalExpr terminal) {
			m_terminal = terminal;
		}
	
		/**
		 * 단말 토큰 값을 타입에 맞는 {@link PropertyValue}로 변환하여 반환한다.
		 *
		 * @return	Property 값.
		 * @throws IllegalArgumentException	지원하지 않는 단말 표현식인 경우.
		 */
		@Override
		public PropertyValue<?> evaluate() {
			if ( m_terminal instanceof StringExpr ) {
				String str = (String) m_terminal.evaluate();
				return PropertyValue.STRING(str);
			}
			else if ( m_terminal instanceof IntegerExpr ) {
				Integer id = (Integer)m_terminal.evaluate();
				return PropertyValue.INTEGER(id.intValue());
			}
			else if ( m_terminal instanceof DoubleExpr ) {
				double value = (Double) m_terminal.evaluate();
				return PropertyValue.DOUBLE(value);
			}
			else if ( m_terminal instanceof BooleanExpr ) {
				Boolean value = (Boolean) m_terminal.evaluate();
				return PropertyValue.BOOLEAN(value);
			}
			else {
				throw new IllegalArgumentException("Unsupported terminal type: " + m_terminal);
			}
		}
		
		@Override
		public String toString() {
			return String.format("'%s'", m_terminal.evaluate());
		}
	}

	/** File 값 리터럴 ({@code file:<mimeType>(<path>)}). */
	public static class FileValueSpec extends LiteralExpr {
		private final FileValue m_value;
		
		/**
		 * 리터럴을 생성한다.
		 *
		 * @param mimeType	파일의 MIME 타입.
		 * @param path	파일 경로.
		 */
		public FileValueSpec(String mimeType, String path) {
			m_value = new FileValue(path, mimeType);
		}

		@Override
		public FileValue evaluate() {
			return m_value;
		}

		@Override
		public String toString() {
			return "" + m_value;
		}
	}

	/** 단일 언어 텍스트로 구성된 MultiLanguageProperty 값 리터럴 ({@code <text>@<lang>}). */
	public static class MLPropertyValueSpec extends LiteralExpr {
		private final MultiLanguagePropertyValue m_value;
	
		/**
		 * 리터럴을 생성한다.
		 *
		 * @param lang	언어 코드.
		 * @param text	텍스트.
		 */
		public MLPropertyValueSpec(String lang, String text) {
			m_value = new MultiLanguagePropertyValue(lang, text);
		}
	
		@Override
		public MultiLanguagePropertyValue evaluate() {
			return m_value;
		}
		
		@Override
		public String toString() {
			LangStringTextType langText = m_value.getLangTextAll().get(0);
			return String.format("'%s'@%s", langText.getText(), langText.getLanguage());
		}
	}

	/** Range 값 리터럴 ({@code [<min>, <max>]}). */
	public static class RangeValueSpec extends LiteralExpr {
		private final RangeValue<?> m_value;
	
		/**
		 * 리터럴을 생성한다.
		 *
		 * @param vtype	Range 값의 데이터 타입.
		 * @param min	최소값.
		 * @param max	최대값.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public RangeValueSpec(DataType<?> vtype, Object min, Object max) {
			m_value = new RangeValue(vtype, min, max);
		}
	
		@Override
		public RangeValue<?> evaluate() {
			return m_value;
		}

		@Override
		public String toString() {
			return "" + m_value;
		}
	}
}
