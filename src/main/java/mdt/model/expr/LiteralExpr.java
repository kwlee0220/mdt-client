package mdt.model.expr;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;

import mdt.aas.DataType;
import mdt.model.expr.TerminalExpr.DoubleExpr;
import mdt.model.expr.TerminalExpr.IntegerExpr;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.FileValue;
import mdt.model.sm.value.MultiLanguagePropertyValue;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.value.RangeValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class LiteralExpr implements MDTExpression {
	public abstract ElementValue evaluate();
	
	public static class PropertyValueSpec extends LiteralExpr {
		private final TerminalExpr m_terminal;
	
		public PropertyValueSpec(TerminalExpr terminal) {
			m_terminal = terminal;
		}
	
		@Override
		public PropertyValue evaluate() {
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
			else {
				throw new IllegalArgumentException("Unsupported terminal type: " + m_terminal);
			}
		}
		
		@Override
		public String toString() {
			return String.format("'%s'", m_terminal.evaluate());
		}
	}

	public static class FileValueSpec extends LiteralExpr {
		private final FileValue m_value;
		
		public FileValueSpec(String mimeType, String path) {
			m_value = new FileValue(mimeType, path);
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

	public static class MLPropertyValueSpec extends LiteralExpr {
		private final MultiLanguagePropertyValue m_value;
	
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

	public static class RangeValueSpec extends LiteralExpr {
		private final RangeValue<?> m_value;
	
		public RangeValueSpec(DataType<?> vtype, Object min, Object max) {
			m_value = new RangeValue(vtype, min, max);
		}
	
		@Override
		public RangeValue evaluate() {
			return m_value;
		}

		@Override
		public String toString() {
			return "" + m_value;
		}
	}
}
