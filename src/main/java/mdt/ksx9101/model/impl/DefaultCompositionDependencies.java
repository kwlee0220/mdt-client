package mdt.ksx9101.model.impl;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.CompositionDependencies;
import mdt.ksx9101.model.CompositionDependency;
import mdt.model.SubmodelElementListHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionDependencies
			extends SubmodelElementListHandle<CompositionDependency,DefaultCompositionDependency>
									implements CompositionDependencies {
	public DefaultCompositionDependencies() {
		super("CompositionDependencies", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}
	public DefaultCompositionDependencies(List<DefaultCompositionDependency> elms) {
		this();
		
		this.setElementHandles(elms);
	}

	@Override
	public DefaultCompositionDependency newElementHandle() {
		return new DefaultCompositionDependency();
	}
}