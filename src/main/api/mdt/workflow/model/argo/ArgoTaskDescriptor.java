package mdt.workflow.model.argo;

import java.util.Set;

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
	private Set<String> dependencies;

	@JsonCreator
	public ArgoTaskDescriptor(@JsonProperty("name") @NonNull String name,
								@JsonProperty("value") @NonNull String template,
								@JsonProperty("dependencies") Set<String> dependencies) {
		this.name = name;
		this.template = template;
		this.dependencies = (dependencies.size() > 0) ? dependencies : null;
	}
}
