package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Routing;
import mdt.ksx9101.model.Routings;
import mdt.model.SubmodelElementListHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultRoutings extends SubmodelElementListHandle<Routing,DefaultRouting>
								implements Routings {
	public DefaultRoutings() {
		super("Routings", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultRouting newElementHandle() {
		return new DefaultRouting();
	}
}