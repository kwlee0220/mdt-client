package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(Include.NON_NULL)
public class StartResult {
	private MDTInstanceStatus status;
	private String serviceEndpoint;
}
