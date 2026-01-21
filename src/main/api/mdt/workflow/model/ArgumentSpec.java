package mdt.workflow.model;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.expr.LiteralExpr;
import mdt.model.expr.MDTElementReferenceExpr;
import mdt.model.expr.MDTExpression;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerAware;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.PropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class ArgumentSpec {
	public static ReferenceArgumentSpec reference(MDTElementReference ref) {
		return new ReferenceArgumentSpec(ref);
	}
	public static ReferenceArgumentSpec reference(String refString) {
		return new ReferenceArgumentSpec(ElementReferences.parseExpr(refString));
	}
	public static TaskOutputArgumentSpec taskOutput(String argId, String taskId, String outVarId) {
		return new TaskOutputArgumentSpec(argId, taskId, outVarId);
	}
	public static LiteralArgumentSpec literal(ElementValue value) {
		return new LiteralArgumentSpec(value);
	}
	public static LiteralArgumentSpec literal(Integer value) {
		return literal(PropertyValue.INTEGER(value));
	}
	public static LiteralArgumentSpec literal(String value) {
		return literal(PropertyValue.STRING(value));
	}
	
	abstract public ElementValue readValue() throws IOException;
	
	public static ArgumentSpec parseArgumentSpec(String argSpecExpr) {
		MDTExpression expr = MDTExpressionParser.parseExpr(argSpecExpr);
		if ( expr instanceof MDTElementReferenceExpr refExpr ) {
			return ArgumentSpec.reference(refExpr.evaluate());	
		}
		else if ( expr instanceof LiteralExpr lit ) {
			return ArgumentSpec.literal(lit.evaluate());
		}
		else {
			throw new IllegalArgumentException("Unexpected variable expression: expr=" + argSpecExpr);
		}
	}
	
	public static class ReferenceArgumentSpec extends ArgumentSpec implements MDTInstanceManagerAware {
		private final MDTElementReference m_ref;
		
		public ReferenceArgumentSpec(MDTElementReference ref) {
			m_ref = ref;
		}
		
		public String getReferenceString() {
			return m_ref.toStringExpr();
		}
		
		public MDTElementReference getElementReference() {
			return m_ref;
		}
		
		@Override
		public void activate(MDTInstanceManager manager) {
			m_ref.activate(manager);
		}
		
		public SubmodelElement read() throws IOException {
			return m_ref.read();
		}
		
		@Override
		public ElementValue readValue() throws IOException {
			return m_ref.readValue();
		}
		
		public void updateValue(ElementValue value) throws IOException {
			m_ref.updateValue(value);
		}
		
		public void updateWithJsonString(String jsonString) throws IOException {
			m_ref.updateValue(jsonString);
		}
		
		@Override
		public String toString() {
			return String.format("reference(%s)", m_ref.toStringExpr());
		}
	}
	
	public static class TaskOutputArgumentSpec extends ArgumentSpec {
		private final String m_taskId;
		private final String m_outVarId;

		public TaskOutputArgumentSpec(String argId, String taskId, String outVarId) {
			m_taskId = taskId;
			m_outVarId = outVarId;
		}

		public String getTaskId() {
			return m_taskId;
		}

		public String getOutputVarName() {
			return m_outVarId;
		}

		@Override
		public ElementValue readValue() throws IOException {
			throw new UnsupportedOperationException(
                    "Cannot get value of TaskOutputArgumentSpec directly: taskId=" + m_taskId
                    + ", outVarId=" + m_outVarId);
		}
		
		@Override
		public String toString() {
			return String.format("task_output(%s,%s)", m_taskId, m_outVarId);
		}
	}
	
	public static class LiteralArgumentSpec extends ArgumentSpec {
		private final ElementValue m_value;
		
		public LiteralArgumentSpec(ElementValue value) {
			m_value = value;
		}
		
		@Override
		public ElementValue readValue() {
			return m_value;
		}
		
		@Override
		public String toString() {
			return String.format("literal(%s)", m_value.toValueJsonString());
		}
	}
}
