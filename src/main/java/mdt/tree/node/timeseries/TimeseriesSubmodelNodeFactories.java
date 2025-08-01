package mdt.tree.node.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.UtilityClass;

import utils.stream.FStream;

import mdt.model.timeseries.DefaultRecord;
import mdt.model.timeseries.Record.FieldValue;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.DefaultNodeFactory;
import mdt.tree.node.TerminalNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public final class TimeseriesSubmodelNodeFactories {
	public static class RecordTransform implements DefaultNodeFactory {
		@Override
		public DefaultNode create(SubmodelElement sme) {
			DefaultRecord rec = new DefaultRecord();
			rec.updateFromAasModel(sme);
			
			TerminalNode node = new TerminalNode();
			node.setTitle(rec.getIdShort());
			node.setValueType(" (REC)");
			
			String csv = FStream.from(rec.getFieldValues())
								.map(this::toString)
								.join(", ");
			node.setValue(csv);
			
			return node;
		}
		
		private String toString(FieldValue fv) {
			return String.format("%s:%s", fv.getField().getName(), fv.getValue());
		}
	}
}

