package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Repair;
import mdt.ksx9101.model.Repairs;
import mdt.model.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultRepairs extends SubmodelElementListEntity<Repair,DefaultRepair>
								implements Repairs {
	public DefaultRepairs() {
		super("Repairs", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultRepair newElementEntity() {
		return new DefaultRepair();
	}
}