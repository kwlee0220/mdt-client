package mdt.model.sm.entity;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.func.FOption;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementCollectionEntity extends AbstractSMEContainerEntity<SubmodelElement>
												implements SubmodelElementEntity {
	@Override
	public SubmodelElementCollection newSubmodelElement() {
		DefaultSubmodelElementCollection smc = new DefaultSubmodelElementCollection.Builder()
													.idShort(getIdShort())
													.semanticId(getSemanticId())
													.value(Lists.newArrayList())
													.build();
		updateAasModel(smc);
		return smc;
	}

	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		Preconditions.checkArgument(sme != null);
		
		if ( sme instanceof SubmodelElementCollection smc ) {
			setIdShort(smc.getIdShort());
			setSemanticId(smc.getSemanticId());
			
			updateFields(smc.getValue());
		}
		else {
			throw new IllegalArgumentException("Not SubmodelElementCollection, but=" + sme);
		}
	}

	@Override
	public void updateAasModel(SubmodelElement sme) {
		Preconditions.checkArgument(sme != null);

		if ( sme instanceof SubmodelElementCollection smc ) {
			FOption.accept(getIdShort(), smc::setIdShort);
			FOption.accept(getSemanticId(), smc::setSemanticId);
			
			Map<String, SubmodelElement> elementMap = FStream.from(smc.getValue())
															.toMap(SubmodelElement::getIdShort);
			for ( SubmodelElement newElm: super.readSubmodelElementFromFields() ) {
				elementMap.put(newElm.getIdShort(), newElm);
			}
			smc.setValue(Lists.newArrayList(elementMap.values()));
		}
		else {
			throw new IllegalArgumentException("Not SubmodelElementCollection, but=" + sme);
		}
	}
}
