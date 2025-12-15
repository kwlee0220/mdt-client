package mdt.tree.node.data;

import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.UtilityClass;

import mdt.aas.DataTypes;
import mdt.model.sm.SubmodelUtils;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.DefaultNodeFactories;
import mdt.tree.node.DefaultNodeFactory;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public final class ParameterValueNode {
	public static ParameterValueNodeFactory FACTORY = new ParameterValueNodeFactory();
	public static class ParameterValueNodeFactory implements DefaultNodeFactory {
		@Override
		public DefaultNode create(SubmodelElement sme) {
			SubmodelElement value = SubmodelUtils.traverse(sme, "ParameterValue");
			DefaultNode node = DefaultNodeFactories.create(value);

			String id = getFieldStringOrNull(sme, "ParameterID");
			node.setTitle(id);

			String tsStr = SubmodelUtils.findPropertyById(sme, "EventDateTime")
										.map(idxed -> {
											Property tsProp = idxed.value();
											Instant ts = DataTypes.DATE_TIME.parseValueString(tsProp.getValue());
											return String.format(" (ts=%s)", DataTypes.DATE_TIME.toValueString(ts));
										})
										.orElse("");
			node.setValue(node.getValue() + tsStr);
			return node;
		}
		
		private String getFieldStringOrNull(SubmodelElement smc, String field) {
			return SubmodelUtils.findPropertyById(smc, field)
								.map(idxed -> idxed.value().getValue())
								.orElse(null);
		}
	}
}
