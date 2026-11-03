package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 * MDT 프레임워크가 정의한 서브모델 및 SubmodelElement들의 semantic id 상수들을 모아 놓은 클래스이다.
 * <p>
 * 모든 semantic id는 {@link #MDT_PREFIX}로 시작하고 {@link #VERSION}으로 끝나는 URI 형태이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MDTSemanticIds {
	private MDTSemanticIds() {
		throw new AssertionError("Should not be called: class=" + MDTSemanticIds.class.getName());
	}
	
	/** MDT semantic id들의 공통 접두어. */
	public static final String MDT_PREFIX = "https://etri.re.kr/mdt";
	/** MDT semantic id들의 공통 버전 접미어. */
	public static final String VERSION = "/1/1";

	/** InformationModel 서브모델의 semantic id. */
	public static final String SUBMODEL_INFO = MDT_PREFIX + "/Submodel/InformationModel" + VERSION;
	/** InformationModel 내 MDTInfo 요소의 semantic id. */
	public static final String MDT_INFO = MDT_PREFIX + "/Submodel/InformationModel/MDTInfo" + VERSION;
	/** InformationModel 내 TwinComposition 요소의 semantic id. */
	public static final String TWIN_COMPOSITION = MDT_PREFIX + "/Submodel/InformationModel/TwinComposition" + VERSION;
	/** TwinComposition 내 CompositionItem 요소의 semantic id. */
	public static final String COMPOSITION_ITEM = MDT_PREFIX + "/Submodel/InformationModel/CompositionItem" + VERSION;
	/** TwinComposition 내 CompositionDependency 요소의 semantic id. */
	public static final String COMPOSITION_DEPENDENCY = MDT_PREFIX + "/Submodel/InformationModel/CompositionDependency" + VERSION;

	/** Data 서브모델의 semantic id. */
	public static final String SUBMODEL_DATA = MDT_PREFIX + "/Submodel/Data" + VERSION;
	/** Data 서브모델 내 Equipment 요소의 semantic id. */
	public static final String EQUIPMENT = MDT_PREFIX + "/Submodel/Data/Equipment" + VERSION;
	/** Data 서브모델 내 Operation 요소의 semantic id. */
	public static final String OPERATION = MDT_PREFIX + "/Submodel/Data/Operation" + VERSION;
	/** Data 서브모델 내 Parameter 요소의 semantic id. */
	public static final String PARAMETER = MDT_PREFIX + "/Submodel/Data/Parameter" + VERSION;
	/** Data 서브모델 내 ParameterValue 요소의 semantic id. */
	public static final String PARAMETER_VALUE = MDT_PREFIX + "/Submodel/Data/ParameterValue" + VERSION;
	
	/** AI 서브모델의 semantic id. */
	public static final String SUBMODEL_AI = MDT_PREFIX + "/Submodel/AI" + VERSION;
	/** Simulation 서브모델의 semantic id. */
	public static final String SUBMODEL_SIMULATION = MDT_PREFIX + "/Submodel/Simulation" + VERSION;
	/** 연산 입력 인자({@link Input}) 요소의 semantic id. */
	public static final String ARGUMENT_INPUT = MDT_PREFIX + "/Submodel/Operation/Input" + VERSION;
	/** 연산 출력 인자({@link Output}) 요소의 semantic id. */
	public static final String ARGUMENT_OUTPUT = MDT_PREFIX + "/Submodel/Operation/Output" + VERSION;
	
	/** Shape 서브모델의 semantic id. */
	public static final String SUBMODEL_SHAPE = MDT_PREFIX + "/Submodel/Shape" + VERSION;
	
	/** TimeSeries 서브모델의 구간 레코드 조회(ReadRecordsByRange) 연산의 semantic id. */
	public static final String TIME_SERIES_READ_RECORDS_BY_RANGE = MDT_PREFIX + "/Submodel/TimeSeries/ReadRecordsByRange" + VERSION;
	/** TimeSeries 구간(Range) 요소의 semantic id. */
	public static final String TIME_SERIES_RANGE = MDT_PREFIX + "/Submodel/TimeSeries/Range" + VERSION;
	/** TimeSeries 컬럼(Columns) 요소의 semantic id. */
	public static final String TIME_SERIES_COLUMNS = MDT_PREFIX + "/Submodel/TimeSeries/Columns" + VERSION;
	
	/**
	 * 주어진 semantic id 문자열을 담은 외부 참조({@link Reference})를 생성한다.
	 * <p>
	 * 생성된 참조는 {@link ReferenceTypes#EXTERNAL_REFERENCE} 타입이며, {@link KeyTypes#GLOBAL_REFERENCE}
	 * 타입의 키 하나만을 갖는다.
	 *
	 * @param semanticId	semantic id 문자열.
	 * @return	생성된 {@link Reference} 객체.
	 */
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
