package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.BOM;
import mdt.ksx9101.model.BOMs;
import mdt.model.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultBOMs extends SubmodelElementListEntity<BOM,DefaultBOM> implements BOMs {
	public DefaultBOMs() {
		super("BOMs", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultBOM newElementEntity() {
		return new DefaultBOM();
	}
}