package mdt.ksx9101.model.impl;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.CompositionDependency;
import mdt.ksx9101.model.TwinComposition;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;
import mdt.model.SMLField;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultTwinComposition extends SubmodelElementCollectionEntity
									implements TwinComposition {
	@PropertyField(idShort="CompositionID") private String compositionID;
	@PropertyField(idShort="CompositionType") private String compositionType;
	
	@SMLField(idShort="CompositionItems")
	private List<ReferenceElement> compositionItems = Lists.newArrayList();
	
	@SMLField(idShort="CompositionDependencies", elementClass=DefaultCompositionDependency.class)
	private List<CompositionDependency> compositionDependencies = Lists.newArrayList();
}
