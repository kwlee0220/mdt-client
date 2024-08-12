package mdt.model.instance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultInstanceSubmodelDescriptor implements InstanceSubmodelDescriptor {
	private String id;
	private String idShort;
	private String semanticId;
}
