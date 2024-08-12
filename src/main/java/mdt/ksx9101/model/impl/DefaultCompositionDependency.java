package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.CompositionDependency;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionDependency extends AbstractMDTSubmodelElementCollection
											implements CompositionDependency {
	@PropertyField(idShort="Source") private String source;
	@PropertyField(idShort="Target") private String target;
	@PropertyField(idShort="DependencyType") private String dependencyType;
	@PropertyField(idShort="Description") private String description;
	
	public DefaultCompositionDependency() {
		super(null, null);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s->%s]", getDependencyType(), getSource(), getTarget());
	}
}
