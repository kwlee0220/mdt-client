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
public class OperationStatusResponse<T> {
	private OperationStatus status;
	private @Nullable String operationLocation;
	private @Nullable T result;
	private @Nullable String message;
}
