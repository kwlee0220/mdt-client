package mdt.tree.sm;

import java.util.Map;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.collect.Maps;

import mdt.model.Input;
import mdt.model.Output;
import mdt.model.ReferenceUtils;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.info.CompositionDependency;
import mdt.model.sm.info.CompositionItem;
import mdt.model.sm.value.ElementValues;
import mdt.model.timeseries.Record;
import mdt.tree.CustomNodeTransform;
import mdt.tree.TextNode;
import mdt.tree.sm.CustomNodeTransforms.CompositionDependencyTransform;
import mdt.tree.sm.CustomNodeTransforms.CompositionItemTransform;
import mdt.tree.sm.CustomNodeTransforms.InputTransform;
import mdt.tree.sm.CustomNodeTransforms.OutputTransform;
import mdt.tree.sm.CustomNodeTransforms.RecordTransform;
import mdt.tree.sm.data.EquipmentNode;
import mdt.tree.sm.data.ParameterValueNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementNodeFactory {
	private static final Map<String,CustomNodeTransform> TRANSFORMS = Maps.newHashMap();
	static {
		TRANSFORMS.put(ParameterValue.SEMANTIC_ID, new ParameterValueNode.Transform());
		TRANSFORMS.put(Equipment.SEMANTIC_ID, new EquipmentNode.Transform());
		TRANSFORMS.put(CompositionItem.SEMANTIC_ID, new CompositionItemTransform());
		TRANSFORMS.put(CompositionDependency.SEMANTIC_ID, new CompositionDependencyTransform());
		TRANSFORMS.put(Input.SEMANTIC_ID, new InputTransform());
		TRANSFORMS.put(Output.SEMANTIC_ID, new OutputTransform());
		TRANSFORMS.put(Record.SEMANTIC_ID, new RecordTransform());
	}
	
	public static Node toNode(String prefix, SubmodelElement smElm) {
		return toNode(prefix, smElm.getIdShort(), smElm);
	}
	
	public static Node toNode(String prefix, String id, SubmodelElement smElm) {
		if ( smElm instanceof Property p ) {
			return DataElementNodes.fromProperty(prefix, id, p);
		}
		else if ( smElm instanceof File file ) {
			return DataElementNodes.fromFile(prefix, id, file);
		}
		else if ( smElm instanceof SubmodelElementCollection smc) {
			String semanticIdStr = ReferenceUtils.getSemanticIdStringOrNull(smc.getSemanticId());
			CustomNodeTransform trans = TRANSFORMS.get(semanticIdStr);
			if ( trans != null ) {
				return trans.toNode(prefix, smc);
			}
			else {
				return new SubmodelElementCollectionNode(prefix, smc);
			}
		}
		else if ( smElm instanceof SubmodelElementList smel) {
			return new SubmodelElementListNode(prefix, smel);
		}
		else if ( smElm instanceof Operation op) {
			return new AASOperationNode(op);
		}
		else if ( smElm instanceof MultiLanguageProperty mlp ) {
			return DataElementNodes.fromMLProperty(prefix, id, mlp);
		}
		else {
			return new TextNode(ElementValues.toRawString(smElm));
		}
	}
}
