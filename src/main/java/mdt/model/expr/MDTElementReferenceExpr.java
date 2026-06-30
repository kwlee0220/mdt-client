package mdt.model.expr;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import mdt.model.expr.TerminalExpr.IntegerExpr;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTParameterCollectionReference;
import mdt.model.sm.ref.MDTParameterReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.ref.OperationVariableReference;
import mdt.model.sm.ref.OperationVariableReference.Kind;
import mdt.model.sm.ref.SubmodelBasedElementReference;
import mdt.model.sm.ref.timeseries.ReadLastRecordsReference;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;
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
		public SubmodelBasedElementReference evaluate() {
			String paramPathStr = m_paramPath.evaluate().toString();
			if ( paramPathStr.equals("*") ) {
				return MDTParameterCollectionReference.newInstance(m_instanceIdExpr.evaluate());
			}
			else {
				return MDTParameterReference.newInstance(m_instanceIdExpr.evaluate(), paramPathStr);
			}
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
		private final DefaultElementReferenceExpr m_opElmExpr;
		private final Kind m_kind;
		private final IntegerExpr m_opVarIdx;

		public OperationVariableReferenceExpr(DefaultElementReferenceExpr opElmExpr, Kind kind,
												IntegerExpr opVarIdx) {
			m_opElmExpr = opElmExpr;
			m_kind = kind;
			m_opVarIdx = opVarIdx;
		}

		@Override
		public OperationVariableReference evaluate() {
			DefaultElementReference opRef = m_opElmExpr.evaluate();
			int idx = m_opVarIdx.evaluate();
			return OperationVariableReference.newInstance(opRef, m_kind, idx);
		}
	}

	public static class TimeseriesReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTSubmodelExpr m_smExpr;
		@Nullable private final TimeSeriesRange m_range;
		@Nullable private final List<String> m_columns;

		public TimeseriesReferenceExpr(MDTSubmodelExpr smExpr, @Nullable TimeSeriesRange range,
										@Nullable List<String> columns) {
			m_smExpr = smExpr;
			m_range = range;
			m_columns = columns;
		}

		@Override
		public ReadLastRecordsReference evaluate() {
			DefaultSubmodelReference smRef = m_smExpr.evaluate();

			ReadLastRecordsReference.Builder builder = ReadLastRecordsReference.builder()
																					.submodelReference(smRef);
			if ( m_range != null ) {
				builder.range(m_range);
			}
			if ( m_columns != null ) {
				builder.columns(m_columns);
			}
			return builder.build();
		}
	}
}
