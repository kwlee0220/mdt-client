package mdt.model.instance;

import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMDTInstancePayload {
	@JsonProperty("id") private String id;
	@JsonProperty("environment") private Environment environment;
	@JsonProperty("arguments") private Object arguments;
}
