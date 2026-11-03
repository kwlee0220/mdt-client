package mdt.client.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.Nullable;


/**
 * 연산(시뮬레이션) 서버가 반환하는 연산 상태 응답 메시지.
 * <p>
 * 연산 상태({@code status}), 이후 상태 조회/취소에 사용할 연산 위치({@code operationLocation}),
 * 연산 결과({@code result}), 부가 메시지({@code message})로 구성되며,
 * 접근자와 빌더는 Lombok으로 생성된다. JSON 직렬화 시 {@code null}인 필드는 생략된다.
 *
 * @param <T>	연산 결과 타입.
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
