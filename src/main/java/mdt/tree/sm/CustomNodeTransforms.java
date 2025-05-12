package mdt.tree.sm;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.UtilityClass;

import utils.stream.FStream;

import mdt.model.DefaultInput;
import mdt.model.DefaultOutput;
import mdt.model.sm.info.DefaultCompositionDependency;
import mdt.model.sm.info.DefaultCompositionItem;
import mdt.model.timeseries.DefaultRecord;
import mdt.model.timeseries.Record.FieldValue;
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
			
			return SubmodelElementNodeFactory.toNode(prefix, input.getInputID(), input.getInputValue());
		}
	}

	public static class OutputTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultOutput output = new DefaultOutput();
			output.updateFromAasModel(sme);
			
			return SubmodelElementNodeFactory.toNode(prefix, output.getOutputID(), output.getOutputValue());
		}
	}
	
	public static class CompositionDependencyTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultCompositionDependency dep = new DefaultCompositionDependency();
			dep.updateFromAasModel(sme);
			return new TextNode(String.format("%s%s: %s -> %s",
												prefix, dep.getDependencyType(), dep.getSourceId(), dep.getTargetId()));
		}
	}
	
	public static class CompositionItemTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultCompositionItem item = new DefaultCompositionItem();
			item.updateFromAasModel(sme);
			return new TextNode(String.format("%s%s (%s)", prefix, item.getID(), item.getReference()));
		}
	}

	public static class RecordTransform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultRecord rec = new DefaultRecord();
			rec.updateFromAasModel(sme);
			
			String csv = FStream.from(rec.getFieldValues())
								.map(this::toString)
								.join(", ");
			return new TextNode(String.format("%s%s (REC): %s", prefix, rec.getIdShort(), csv));
		}
		
		private String toString(FieldValue fv) {
			return String.format("%s:%s", fv.getField().getName(), fv.getValue());
		}
	}
}

