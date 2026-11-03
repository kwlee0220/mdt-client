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
import mdt.model.sm.ref.timeseries.TimeSeriesRecordsReference;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;
import mdt.model.sm.value.IdShortPath;

/**
 * SubmodelElement 참조({@code fullElementSpec})를 나타내는 표현식의 기반 클래스이다.
 * <p>
 * 평가 결과는 참조 종류에 따른 {@link MDTElementReference}이다.
 * <ul>
 *   <li>{@link DefaultElementReferenceExpr}: {@code <submodelSpec>:<idShortPath>}</li>
 *   <li>{@link ParameterReferenceExpr}: {@code param:<instance>:(<idShortPath>|<index>|*)}</li>
 *   <li>{@link ArgumentReferenceExpr}: {@code oparg:<submodelSpec>:(in|out):(<name>|<index>|*)}</li>
 *   <li>{@link OperationVariableReferenceExpr}: {@code opvar:<elementSpec>:(in|out|inout):<index>}</li>
 *   <li>{@link TimeseriesReferenceExpr}: {@code timeseries:<submodelSpec>[#<range>][|<columns>]}</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTElementReferenceExpr implements MDTExpression {
	/**
	 * 표현식이 가리키는 SubmodelElement 참조를 생성한다.
	 *
	 * @return	SubmodelElement 참조.
	 */
	public abstract MDTElementReference evaluate();
	
	/** Submodel 참조와 idShortPath로 SubmodelElement를 지칭하는 표현식 ({@code <submodelSpec>:<idShortPath>}). */
	public static class DefaultElementReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTSubmodelReference m_smRef;
		private final IdShortPath m_idShortPath;

		/**
		 * 표현식을 생성한다.
		 *
		 * @param smRef	대상 Submodel 참조.
		 * @param path	Submodel 내 idShortPath.
		 */
		public DefaultElementReferenceExpr(MDTSubmodelReference smRef, IdShortPath path) {
			m_smRef = smRef;
			m_idShortPath = path;
		}

		@Override
		public DefaultElementReference evaluate() {
			return DefaultElementReference.newInstance(m_smRef, m_idShortPath.toString());
		}
	}
	
	/**
	 * MDTInstance의 Data 서브모델 파라미터를 지칭하는 표현식 ({@code param:<instance>:<paramPath>}).
	 * <p>
	 * 파라미터 경로가 {@code *}이면 전체 파라미터 컬렉션 참조로, 그 밖에는 개별 파라미터 참조로 평가된다.
	 */
	public static class ParameterReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final StringExpr m_instanceIdExpr;
		private final MDTExpression m_paramPath;
		
		/**
		 * 표현식을 생성한다.
		 *
		 * @param instanceIdExpr	MDTInstance 식별자 표현식.
		 * @param paramPath	파라미터 경로(idShortPath, 인덱스 또는 {@code *}) 표현식.
		 */
		public ParameterReferenceExpr(StringExpr instanceIdExpr, MDTExpression paramPath) {
			m_instanceIdExpr = instanceIdExpr;
			m_paramPath = paramPath;
		}

		/**
		 * 파라미터 참조를 생성한다.
		 *
		 * @return	파라미터 경로가 {@code *}이면 {@link MDTParameterCollectionReference},
		 * 		그렇지 않으면 {@link MDTParameterReference}.
		 */
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
	
	/** AI/Simulation 서브모델 연산의 입력 또는 출력 인자를 지칭하는 표현식 ({@code oparg:<submodelSpec>:(in|out):<arg>}). */
	public static class ArgumentReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTSubmodelExpr m_smExpr;
		private final MDTArgumentKind m_kind;
		private final MDTExpression m_argNameExpr;
		
		/**
		 * 표현식을 생성한다.
		 *
		 * @param smExpr	대상 Submodel 표현식.
		 * @param kind	인자 종류 (입력/출력).
		 * @param argNameExpr	인자 이름, 인덱스 또는 {@code *}를 나타내는 표현식.
		 */
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
	
	/**
	 * AAS Operation SubmodelElement의 연산 변수를 지칭하는 표현식
	 * ({@code opvar:<elementSpec>:(in|out|inout):<index>}).
	 */
	public static class OperationVariableReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final DefaultElementReferenceExpr m_opElmExpr;
		private final Kind m_kind;
		private final IntegerExpr m_opVarIdx;

		/**
		 * 표현식을 생성한다.
		 *
		 * @param opElmExpr	대상 Operation SubmodelElement 참조 표현식.
		 * @param kind	연산 변수 종류 (입력/출력/입출력).
		 * @param opVarIdx	연산 변수 인덱스 표현식.
		 */
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

	/**
	 * TimeSeries 서브모델의 레코드 조회를 지칭하는 표현식 ({@code timeseries:<submodelSpec>[#<range>][|<columns>]}).
	 * <p>
	 * 구간이나 컬럼 목록이 생략된 경우 해당 설정 없이 참조가 생성된다 (컬럼 생략 시 전체 컬럼).
	 */
	public static class TimeseriesReferenceExpr extends MDTElementReferenceExpr implements MDTExpression {
		private final MDTSubmodelExpr m_smExpr;
		@Nullable private final TimeSeriesRange m_range;
		@Nullable private final List<String> m_columns;

		/**
		 * 표현식을 생성한다.
		 *
		 * @param smExpr	대상 TimeSeries Submodel 표현식.
		 * @param range	조회 구간. 생략된 경우는 {@code null}.
		 * @param columns	조회할 컬럼 이름 목록. 생략된 경우는 {@code null}.
		 */
		public TimeseriesReferenceExpr(MDTSubmodelExpr smExpr, @Nullable TimeSeriesRange range,
										@Nullable List<String> columns) {
			m_smExpr = smExpr;
			m_range = range;
			m_columns = columns;
		}

		@Override
		public TimeSeriesRecordsReference evaluate() {
			DefaultSubmodelReference smRef = m_smExpr.evaluate();

			TimeSeriesRecordsReference.Builder builder = TimeSeriesRecordsReference.builder()
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
