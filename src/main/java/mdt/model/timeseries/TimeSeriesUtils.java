package mdt.model.timeseries;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.jetbrains.annotations.Nullable;

import utils.Throwables;
import utils.stream.FStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.sm.PropertyUtils;
import mdt.model.sm.ref.SubmodelReference;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class TimeSeriesUtils {
	private TimeSeriesUtils() {
		throw new AssertionError("Should not be called: class=" + getClass().getName());
	}

	/**
	 * 주어진 시계열 Submodel에서 지정된 범위와 컬럼에 해당하는 레코드들을 읽어 반환한다.
	 * <p>
	 * 대상 Submodel의 {@code ReadRecordsByRange} 연산을 호출하며, {@code range}와 {@code columns}는
	 * 각각 연산의 {@code Range}와 {@code Columns} 입력 인자로 전달된다. {@code range}는
	 * {@link TimeSeriesRange}의 {@code toString()}이 생성하는 MdtExpr 표기(예: {@code last=10},
	 * {@code from~to}, {@code from+N})로, {@code columns}는 쉼표로 구분된 컬럼명 목록으로 인코딩된다.
	 *
	 * @param submodelRef	대상 시계열 Submodel에 대한 참조.
	 * @param range			읽어올 레코드의 범위. {@code null}이면 범위를 지정하지 않은 것으로 간주되어
	 * 						전체 레코드를 읽는다.
	 * @param columns		읽어올 컬럼명 목록. {@code null}이면 전체 컬럼을 읽는다.
	 * @return	조회된 레코드들을 담은 {@link SubmodelElementCollection}.
	 * @throws IOException	연산 호출이 실패하거나 레코드를 읽는 과정에서 오류가 발생한 경우.
	 */
	public static SubmodelElementCollection readRecords(SubmodelReference submodelRef,
														@Nullable TimeSeriesRange range,
														@Nullable List<String> columns) throws IOException {
		AASOperationClient readRecordsByRange = new AASOperationClient(submodelRef.get(), "ReadRecordsByRange",
																	Duration.ofSeconds(1));
		if ( range != null ) {
			String rangeExpr = range.toString();
			readRecordsByRange.setInputVariable("Range", PropertyUtils.STRING("Range", rangeExpr));
		}
		if ( columns != null ) {
			String columnsExpr = FStream.from(columns).join(',');
			readRecordsByRange.setInputVariable("Columns", PropertyUtils.STRING("Columns", columnsExpr));
		}
		
		try {
			OperationResult result = readRecordsByRange.run();

			List<OperationVariable> outputs = result.getOutputArguments();
			return (SubmodelElementCollection)outputs.get(0).getValue();
		}
		catch ( CancellationException | InterruptedException | ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			if ( cause instanceof IOException ) {
				throw (IOException)cause;
			}
			else {
				String rangeStr = (range != null) ? "#" + range : "";
				String colStr = (columns != null) ? ":" + FStream.from(columns).join(',') : "";
				String str = String.format("%s%s%s", submodelRef, rangeStr, colStr);
				throw new IOException("Failed to read TimeSeries records: " + str);
			}
		}
	}
}
