package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MDTSemanticIds {
	private MDTSemanticIds() {
		throw new AssertionError("Should not be called: class=" + MDTSemanticIds.class.getName());
	}
	
	public static final String MDT_PREFIX = "https://etri.re.kr/mdt";
	public static final String VERSION = "/1/1";

	public static final String SUBMODEL_INFO = MDT_PREFIX + "/Submodel/InformationModel" + VERSION;
	public static final String MDT_INFO = MDT_PREFIX + "/Submodel/InformationModel/MDTInfo" + VERSION;
	public static final String TWIN_COMPOSITION = MDT_PREFIX + "/Submodel/InformationModel/TwinComposition" + VERSION;
	public static final String COMPOSITION_ITEM = MDT_PREFIX + "/Submodel/InformationModel/CompositionItem" + VERSION;
	public static final String COMPOSITION_DEPENDENCY = MDT_PREFIX + "/Submodel/InformationModel/CompositionDependency" + VERSION;

	public static final String SUBMODEL_DATA = MDT_PREFIX + "/Submodel/Data" + VERSION;
	public static final String EQUIPMENT = MDT_PREFIX + "/Submodel/Data/Equipment" + VERSION;
	public static final String OPERATION = MDT_PREFIX + "/Submodel/Data/Operation" + VERSION;
	public static final String PARAMETER = MDT_PREFIX + "/Submodel/Data/Parameter" + VERSION;
	public static final String PARAMETER_VALUE = MDT_PREFIX + "/Submodel/Data/ParameterValue" + VERSION;
	
	public static final String SUBMODEL_AI = MDT_PREFIX + "/Submodel/AI" + VERSION;
	public static final String SUBMODEL_SIMULATION = MDT_PREFIX + "/Submodel/Simulation" + VERSION;
	public static final String ARGUMENT_INPUT = MDT_PREFIX + "/Submodel/Operation/Input" + VERSION;
	public static final String ARGUMENT_OUTPUT = MDT_PREFIX + "/Submodel/Operation/Output" + VERSION;
	
	public static final String SUBMODEL_SHAPE = MDT_PREFIX + "/Submodel/Shape" + VERSION;
	
	public static final String TIME_SERIES_READ_LAST_RECORDS = MDT_PREFIX + "/Submodel/TimeSeries/ReadLastRecords" + VERSION;
	public static final String TIME_SERIES_RANGE = MDT_PREFIX + "/Submodel/TimeSeries/Range" + VERSION;
	public static final String TIME_SERIES_COLUMNS = MDT_PREFIX + "/Submodel/TimeSeries/Columns" + VERSION;
	
	public static Reference toReference(String semanticId) {
		return new DefaultReference.Builder()
					.type(ReferenceTypes.EXTERNAL_REFERENCE)
					.keys(new DefaultKey.Builder()
										.type(KeyTypes.GLOBAL_REFERENCE)
										.value(semanticId)
										.build())
					.build();
	}
}
