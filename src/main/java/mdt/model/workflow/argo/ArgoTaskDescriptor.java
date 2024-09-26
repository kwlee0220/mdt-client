package mdt.model.workflow.argo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonInclude(Include.NON_NULL)
public class ArgoTaskDescriptor {
	@NonNull private final String name;
	@NonNull private final String template;
	private List<String> dependencies;
	private ArgoArgumentsDescriptor arguments;

	@JsonCreator
	public ArgoTaskDescriptor(@JsonProperty("name") @NonNull String name,
								@JsonProperty("value") @NonNull String template,
								@JsonProperty("dependencies") List<String> dependencies,
								@JsonProperty("arguments") ArgoArgumentsDescriptor arguments) {
		this.name = name;
		this.template = template;
		this.dependencies = (dependencies.size() > 0) ? dependencies : null;
		this.arguments = (arguments.size() > 0) ? arguments : null;
	}
}
