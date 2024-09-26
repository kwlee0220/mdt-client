package mdt.ksx9101.model.impl;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.CompositionItem;
import mdt.ksx9101.model.CompositionItems;
import mdt.model.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionItems
									extends SubmodelElementListEntity<CompositionItem,DefaultCompositionItem>
									implements CompositionItems {
	public DefaultCompositionItems() {
		super("CompositionItems", null, false, AasSubmodelElements.REFERENCE_ELEMENT);
	}
	public DefaultCompositionItems(List<DefaultCompositionItem> items) {
		this();
		
		Preconditions.checkNotNull(items);
		setElementHandles(items);
	}

	@Override
	public DefaultCompositionItem newElementEntity() {
		return new DefaultCompositionItem();
	}
	
	@Override
	public String toString() {
		return String.format("CompositionItems(%d)", getClass().getSimpleName(),
								getElementHandles().size());
	}
}