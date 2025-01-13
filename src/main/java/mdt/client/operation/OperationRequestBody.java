package mdt.client.operation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;

import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentReference;
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
	@Nullable private Set<String> outputNames = Sets.newHashSet();
	
	public static OperationRequestBody newEmpty() {
		return new OperationRequestBody();
	}
	
	public static OperationRequestBody parseJsonString(String jsonStr) throws IOException {
		return MDTModelSerDe.readValue(jsonStr, OperationRequestBody.class);
	}
	
	public void setOutputNames(Collection<String> outNames) {
		this.outputNames = new HashSet<>(outNames);
	}
	
	public String toString() {
		String paramNameCsv = FStream.from(this.parameters)
									.map(Parameter::getName)
									.join(", ");
		String outNameCsv = FStream.from(this.outputNames).join(", ");
		
		if ( this.submodelEndpoint == null ) {
			return String.format("(%s) -> (%s)", paramNameCsv, outNameCsv);
		}
		else {
			return String.format("%s: {%s} -> {%s}", this.submodelEndpoint, paramNameCsv, outNameCsv);
		}
	}
	
	public static final void main(String... args) throws Exception {
		MDTSubmodelReference smRef = DefaultSubmodelReference.newInstance("inspector", "ThicknessInspection");
		MDTArgumentReference opArgRef = MDTArgumentReference.newInstance(smRef, "input", "UpperImage");
		Parameter param = Parameter.of("Defect", opArgRef);
		
		OperationRequestBody body = new OperationRequestBody();
		body.setParameters(List.of(param));
		
		JsonMapper mapper = JsonMapper.builder().build();
		String json = mapper.writeValueAsString(body);
		
		OperationRequestBody body2 = mapper.readValue(json, OperationRequestBody.class);
		System.out.println(body2);
	}
}
