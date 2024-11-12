package mdt.tree.sm;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.UtilityClass;
import mdt.model.DefaultInput;
import mdt.model.DefaultOutput;
import mdt.model.sm.info.DefaultComponentItem;
import mdt.model.sm.info.DefaultCompositionDependency;
import mdt.tree.CustomNodeTransform;
import mdt.tree.TextNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public final class CustomNodeTransforms {
	public static class InputTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultInput input = new DefaultInput();
			input.updateFromAasModel(sme);
			
			return new TextNode(String.format("%s%s (%s): %s",
											prefix, input.getInputID(), input.getInputType(), input.getInputValue()));
		}
	}

	public static class OutputTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultOutput output = new DefaultOutput();
			output.updateFromAasModel(sme);
			
			return new TextNode(String.format("%s%s (%s): %s",
									prefix, output.getOutputID(), output.getOutputType(), output.getOutputValue()));
		}
	}
	
	public static class CompositionDependencyTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultCompositionDependency dep = new DefaultCompositionDependency();
			dep.updateFromAasModel(sme);
			return new TextNode(String.format("%s%s: %s -> %s",
												prefix, dep.getDependencyType(), dep.getSource(), dep.getTarget()));
		}
	}
	
	public static class ComponentItemTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultComponentItem item = new DefaultComponentItem();
			item.updateFromAasModel(sme);
			return new TextNode(String.format("%s%s (%s)", prefix, item.getID(), item.getReference()));
		}
	}
}

