package mdt.client.operation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

import mdt.model.MDTModelSerDe;
import mdt.task.Parameter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonInclude(Include.NON_NULL)
public class OperationRequestBody {
	@Nullable private String submodelEndpoint;
	@Nullable private List<Parameter> parameters;
	@Nullable private Set<String> outputNames;
	
	public static OperationRequestBody newEmpty() {
		return new OperationRequestBody();
	}
	
	public static OperationRequestBody parseJsonString(String jsonStr) throws IOException {
		return MDTModelSerDe.readValue(jsonStr, OperationRequestBody.class);
	}
	
	public String toString() {
		return MDTModelSerDe.toJsonString(this);
	}
}
