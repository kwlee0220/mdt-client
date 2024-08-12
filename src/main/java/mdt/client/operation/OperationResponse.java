package mdt.client.operation;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class OperationResponse<T> {
	private OperationStatus status;
	@Nullable private T result;
	@Nullable private String message;
	
	@Override
	public String toString() {
		String resultStr = (this.result != null) ? String.format(", result=%s", this.result) : "";
		String msgStr = (this.message != null) ? String.format(", message=%s", this.message) : "";
		return String.format("status=%s%s%s", this.status, resultStr, msgStr);
	}
	
	public static <T> OperationResponse<T> completed(T result) {
		return OperationResponse.<T>builder()
									.status(OperationStatus.COMPLETED)
									.result(result)
									.build();
	}
	
	public static <T> OperationResponse<T> failed(String msg) {
		return OperationResponse.<T>builder()
									.status(OperationStatus.FAILED)
									.message(msg)
									.build();
	}
}
