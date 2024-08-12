package mdt.timeseries;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import mdt.model.AbstractMDTSubmodelElementCollection;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class LinkedSegment extends AbstractMDTSubmodelElementCollection {
	private static final Reference SEMANTIC_ID
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value("https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1")
									.build())
				.build();

	@Override
	public void fromAasModel(SubmodelElement model) {
	}

	@Override
	public SubmodelElementCollection toAasModel() {
		return null;
	}
}
