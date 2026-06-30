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

	public static SubmodelElementCollection readLastRecords(SubmodelReference submodelRef,
															@Nullable TimeSeriesRange range,
															@Nullable List<String> columns) throws IOException {
		AASOperationClient readLastRecords = new AASOperationClient(submodelRef.get(), "ReadLastRecords",
																	Duration.ofSeconds(1));
		if ( range != null ) {
			String rangeExpr = range.toString();
			readLastRecords.setInputVariable("Range", PropertyUtils.STRING("Range", rangeExpr));
		}
		if ( columns != null ) {
			String columnsExpr = FStream.from(columns).join(',');
			readLastRecords.setInputVariable("Columns", PropertyUtils.STRING("Columns", columnsExpr));
		}
		
		try {
			OperationResult result = readLastRecords.run();

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
