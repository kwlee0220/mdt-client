package mdt.model.sm;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.CSV;
import utils.Indexed;
import utils.InternalException;
import utils.Preconditions;
import utils.func.Funcs;
import utils.func.Try;
import utils.stream.FStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;
import mdt.model.MDTSemanticIds;
import mdt.model.ModelValidationException;
import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.timeseries.TimeSeries;

/**
 * AAS Submodel과 SubmodelElement를 다루기 위한 정적 유틸리티 메소드 모음.
 * <p>
 * idShort 경로 파싱/생성/탐색, SubmodelElementCollection 필드 접근, SubmodelElement 생성,
 * semanticId 기반의 Submodel/SubmodelElement 종류 판별, 참조(Reference) 직렬화 문자열 파싱 등의
 * 기능을 제공한다.
 * <p>
 * idShort 경로는 {@code '.'}로 구분된 idShort 세그먼트로 구성되며, SubmodelElementList의 원소는
 * {@code "Inputs[0].InputValue"}처럼 세그먼트 뒤에 {@code [인덱스]}를 붙여 지정한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubmodelUtils {
	private SubmodelUtils() {
		throw new AssertionError("Should not be called: class=" + SubmodelUtils.class.getName());
	}
	
	/**
	 * 주어진 SubmodelElement 객체를 복사하여 반환한다.
	 *
	 * @param sme 복제할 SubmodelElement 객체.
	 * @return 주어진 SubmodelElement 객체의 깊은 복사본.
	 * @throws InternalException JSON 직렬화 또는 역직렬화 과정에서 오류가 발생한 경우.
	 */
	public static SubmodelElement duplicate(SubmodelElement sme) {
		try {
			String jsonStr = MDTModelSerDe.toJsonString(sme);
			return MDTModelSerDe.readValue(jsonStr, sme.getClass());
		}
		catch ( IOException e ) {
			throw new InternalException("Failed to duplicate: " + sme, e);
		}
	}
	
	/**
	 * 주어진 idShort 경로 문자열을 '.' 구분자로 분할하여 각 세그먼트를 개별적으로 파싱한 후,
	 * 모든 세그먼트를 순서대로 반환하는 스트림을 생성하여 반환한다.
	 *
	 * <p>
	 * {@code [인덱스]} 표기는 별도의 세그먼트로 분리된다. 예를 들어 {@code "A.B[0][1].C"}는
	 * {@code "A"}, {@code "B"}, {@code "0"}, {@code "1"}, {@code "C"}로 분할된다.
	 *
	 * @param idShortPath	idShort 경로 문자열.
	 * @return	idShort 경로의 각 세그먼트를 순서대로 반환하는 스트림.
	 * @throws IllegalArgumentException	형식에 맞지 않는 세그먼트가 포함된 경우(스트림 소비 시점).
	 */
	public static FStream<String> parseIdShortPath(String idShortPath) {
		return CSV.parseCsv(idShortPath, '.')
					.flatMapIterable(seg -> parsePathSegment(seg));
	}
	// idShort 이름 뒤에 0개 이상의 '[숫자]' 인덱스가 붙는 경로 세그먼트.
	private static final Pattern SEGMENT_PATTERN = Pattern.compile("(\\w+)((?:\\[\\d+\\])*)");
	private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");
	private static List<String> parsePathSegment(String idShort) {
		Matcher matcher = SEGMENT_PATTERN.matcher(idShort);
		if ( !matcher.matches() ) {
			throw new IllegalArgumentException("Invalid idShort path segment: " + idShort);
		}

		List<String> result = Lists.newArrayList();
		result.add(matcher.group(1));	// idShort 이름

		Matcher idxMatcher = INDEX_PATTERN.matcher(matcher.group(2));
		while ( idxMatcher.find() ) {
			result.add(idxMatcher.group(1));	// SubmodelElementList 인덱스
		}

		return result;
	}
	
	/**
	 * 주어진 idShort 리스트를 기반으로 idShort 경로 문자열을 생성하여 반환한다.
	 * <p>
	 * 정수로 파싱되는 세그먼트는 {@code [인덱스]} 형태로, 그 외의 세그먼트는 {@code '.'}로 연결된다.
	 * {@link #parseIdShortPath(String)}의 역연산이다.
	 *
	 * @param segList idShort 경로의 세그먼트 리스트. 각 세그먼트는 idShort 또는 인덱스 형태일 수 있다.
	 * @return 생성된 idShort 경로 문자열.
	 */
	public static String buildIdShortPath(Iterable<String> segList) {
		StringBuilder builder = new StringBuilder();
		for ( String seg: segList ) {
			try {
				int idx = Integer.parseInt(seg);
				builder = builder.append(String.format("[%d]", idx));
			}
			catch ( NumberFormatException e ) {
				if ( !builder.isEmpty() ) {
					builder = builder.append('.');
				}
				builder = builder.append(seg);
			}
		}
		
		return builder.toString();
	}

	/**
	 * 주어진 Property의 값을 지정된 class 타입으로 변환하여 반환한다.
	 *
	 * @param <T>	변환 대상 class의 타입 매개변수.
	 * @param prop	값을 읽을 대상	Property 객체.
	 * @param cls	변환 대상 class 객체. Property의 valueType과 호환되는 class 객체여야 한다.
	 * @return	Property의 값이 cls로 변환된 객체. Property 값이 없으면 {@code null}.
	 * @throws IllegalArgumentException	파싱된 값이 {@code cls} 타입이 아닌 경우.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getPropertyValue(Property prop, Class<T> cls) {
		Object value = DataTypes.fromAas4jDatatype(prop.getValueType()).parseValueString(prop.getValue());
		if ( value == null ) {
			return null;
		}
		if ( cls.isAssignableFrom(value.getClass()) ) {
			return (T)value;
		}
		else {
			throw new IllegalArgumentException("Cannot cast to " + cls);
		}
	}

	/**
	 * 주어진 SubmodelElement에서 지정된 idShort 경로를 따라 탐색하여 최종적으로 도달한 Property의 값을 지정된
	 * class 타입으로 변환하여 반환한다.
	 *
	 * @param <T>         변환 대상 class의 타입 매개변수.
	 * @param start       탐색을 시작할 SubmodelElement 객체.
	 * @param idShortPath 탐색할 idShort 경로명.
	 * @param valueClass  Property의 값을 변환할 대상 class 객체. Property의 valueType과 호환되는
	 *                    class 객체여야 한다.
	 * @return 탐색 결과로 도달한 Property의 값이 valueClass로 변환된 객체.
	 * @throws ResourceNotFoundException 탐색 경로에 해당하는 SubmodelElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	도달한 요소가 Property가 아니거나 값이 {@code valueClass}
	 * 									타입이 아닌 경우.
	 */
	public static <T> T getPropertyValueByPath(SubmodelElement start, String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = traverse(start, idShortPath, Property.class);
		return getPropertyValue(prop, valueClass);
	}
	
	/**
	 * 주어진 Submodel에서 지정된 idShort 경로를 따라 탐색하여 최종적으로 도달한 Property의 값을 지정된
	 * class 타입으로 변환하여 반환한다.
	 *
	 * @param <T>         변환 대상 class의 타입 매개변수.
	 * @param submodel    탐색을 시작할 Submodel 객체.
	 * @param idShortPath 탐색할 idShort 경로명.
	 * @param valueClass  Property의 값을 변환할 대상 class 객체. Property의 valueType과 호환되는
	 *                    class 객체여야 한다.
	 * @return 탐색 결과로 도달한 Property의 값이 valueClass로 변환된 객체.
	 * @throws ResourceNotFoundException 탐색 경로에 해당하는 SubmodelElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	도달한 요소가 Property가 아니거나 값이 {@code valueClass}
	 * 									타입이 아닌 경우.
	 */
	public static <T> T getPropertyValueByPath(Submodel submodel, String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = cast(traverse(submodel, idShortPath), Property.class);
		return getPropertyValue(prop, valueClass);
	}
	
	/**
	 * 조상 idShort 경로와 자손 idShort 경로가 주어졌을 때, 조상에서 자손까지의 상대적인 idShort 경로를 반환한다.
	 * <p>
	 * 조상과 자손이 동일한 경우에는 빈 문자열을 반환한다. 자손이 조상의 자손이 아닌 경우에는
	 * {@code null}을 반환한다.
	 * 
	 * @param ancestor		조상 idshort 경로.
	 * @param descendant    자손 idshort 경로.
	 * @return		조상에서 자손까지의 상대적인 idShort 경로.
	 * 				만약 조상과 자손이 동일한 경우에는 빈 문자열을 반환한다.
	 * 				자손이 조상의 자손이 아닌 경우에는 {@code null}
	 */
	public static String toRelativeIdShortPath(String ancestor, String descendant) {
		if ( !isIdShortPathPrefix(ancestor, descendant) ) {
			return null;
		}
		if ( descendant.length() == ancestor.length() ) {
			return "";
		}
		int prefixLen = ancestor.length();
		// '.' 구분자는 상대 경로에서 제거하고, '[' (list 인덱스)는 유지한다.
		if ( descendant.charAt(prefixLen) == '.' ) {
			++prefixLen;
		}
		return descendant.substring(prefixLen);
	}

	/**
	 * {@code prefix}가 {@code path}의 idShort 경로 접두어인지 여부를 반환한다.
	 * <p>
	 * 단순 문자열 접두어가 아니라 경로 세그먼트 경계({@code '.'} 또는 {@code '['})를 기준으로 판정한다.
	 * 따라서 {@code "A.Value1"}은 {@code "A.Value10"}의 접두어가 아니다.
	 * 두 경로가 동일한 경우에는 {@code true}를 반환한다.
	 *
	 * @param prefix	접두어 후보 idShort 경로.
	 * @param path		대상 idShort 경로.
	 * @return			{@code prefix}가 {@code path}의 경로 접두어이면 {@code true}.
	 */
	public static boolean isIdShortPathPrefix(String prefix, String path) {
		if ( !path.startsWith(prefix) ) {
			return false;
		}
		if ( path.length() == prefix.length() ) {
			return true;
		}
		char next = path.charAt(prefix.length());
		return next == '.' || next == '[';
	}
	
	/**
	 * 주어진 SubmodelElement를 지정된 class로 타입 변환시킨다.
	 * 
	 * @param <T>		타입 변환 대상 SubmodelElement의 class 타입.
	 * @param sme	 	타입 변환 대상 SubmodelElement 객체.
	 * @param toClass	타입 변환 대상 class 객체.	
	 * @return	타입 변환된 SubmodelElement 객체.
	 * @throws IllegalArgumentException	{@code sme}가 {@code toClass} 타입이 아닌 경우.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends SubmodelElement> T cast(SubmodelElement sme, Class<T> toClass) {
		if ( toClass.isAssignableFrom(sme.getClass()) ) {
			return (T)sme;
		}
		else {
			throw new IllegalArgumentException("Cannot cast to " + toClass);
		}
	}
	
	/**
	 * 주어진 idShort 경로를 따라 SubmodelElement를 탐색하여 반환한다.
	 * <p>
	 * SubmodelElementCollection에서는 idShort로, SubmodelElementList에서는 정수 세그먼트이면
	 * 인덱스로, 그렇지 않으면 원소의 idShort로 하위 요소를 찾는다.
	 * 
	 * @param root			탐색을 시작할 SubmodelElement 객체.
	 * @param idShortPath	탐색 idShort 경로명. {@code root}에 대한 상대 경로이다.
	 * @return	탐색 결과 SubmodelElement 객체.
	 * @throws ResourceNotFoundException 경로에 해당하는 SubmodeElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	경로 세그먼트의 형식이 올바르지 않은 경우.
	 */
	public static SubmodelElement traverse(SubmodelElement root, String idShortPath) throws ResourceNotFoundException {
		List<String> pathSegs = CSV.parseCsv(idShortPath, '.')
									.flatMapIterable(seg -> parsePathSegment(seg))
									.toList();
		SubmodelElement current = root;
		for ( String seg: pathSegs ) {
			current = hop(current, seg);
			if ( current == null ) {
				throw new ResourceNotFoundException("SubmodelElement", "idShortPath=" + idShortPath);
			}
		}
		
		return current;
	}
	
	/**
	 * 주어진 idShort 경로를 따라 SubmodelElement를 탐색하여 반환한다.
	 *
	 * @param <T>         탐색 결과로 기대되는 SubmodelElement의 class 타입.
	 * @param start       탐색을 시작할 SubmodelElement 객체.
	 * @param idShortPath 탐색 idShort 경로명.
	 * @param targetClass 탐색 결과로 기대되는 SubmodelElement의 class 객체.
	 * @return	탐색 결과 SubmodelElement 객체.
	 * @throws ResourceNotFoundException 경로에 해당하는 SubmodeElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	탐색 결과가 {@code targetClass} 타입이 아닌 경우.
	 */
	public static <T extends SubmodelElement> T traverse(SubmodelElement start, String idShortPath, Class<T> targetClass)
		throws ResourceNotFoundException {
		return cast(traverse(start, idShortPath), targetClass);
	}
	
	/**
	 * 주어진 Submodel의 최상위 SubmodelElement들부터 idShort 경로를 따라 탐색하여
	 * SubmodelElement를 반환한다.
	 * 
	 * @param submodel		탐색 대상 Submodel 객체.
	 * @param idShortPath	탐색 경로명.
	 * @return	탐색 결과 SubmodelElement 객체.
	 * @throws ResourceNotFoundException 경로에 해당하는 SubmodeElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	{@code submodel} 또는 {@code idShortPath}가 {@code null}인 경우.
	 */
	public static SubmodelElement traverse(Submodel submodel, String idShortPath) throws ResourceNotFoundException {
		Preconditions.checkNotNullArgument(submodel, "Submodel was null");
		Preconditions.checkNotNullArgument(idShortPath, "idShortPath was null");
		
		SubmodelElementCollection start = new DefaultSubmodelElementCollection.Builder()
													.value(submodel.getSubmodelElements())
													.build();
		return traverse(start, idShortPath);
	}
	
	/**
	 * 주어진 idShort 경로를 따라 Submodel을 탐색하여 SubmodelElement를 반환한다.
	 *
	 * @param <T>	    	탐색 결과로 기대되는 SubmodelElement의 class 타입.
	 * @param submodel		탐색 대상 Submodel 객체.
	 * @param idShortPath	탐색 idShort 경로명.
	 * @param targetClass	탐색 결과로 기대되는 SubmodelElement의 class 객체.
	 * @return	탐색 결과 SubmodelElement 객체.
	 * @throws ResourceNotFoundException	경로에 해당하는 SubmodeElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	탐색 결과가 {@code targetClass} 타입이 아닌 경우.
	 */
	public static <T extends SubmodelElement> T traverse(Submodel submodel, String idShortPath, Class<T> targetClass)
		throws ResourceNotFoundException {
		return cast(traverse(submodel, idShortPath), targetClass);
	}
	
	/**
	 * 주어진 SubmodelElement에서 idShort가 fieldName인 하위 요소를 탐색하여 존재 여부를 반환한다.
	 *
	 * @param smc       탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName 탐색할 idShort 이름.
	 * @return	하위 요소 존재 여부.
	 */
	public static boolean containsFieldById(SubmodelElementCollection smc, String fieldName) {
		return Funcs.exists(smc.getValue(), field -> field.getIdShort().equals(fieldName));
	}
	
	/**
	 * 주어진 SubmodelElement에서 idShort가 fieldName인 하위 요소를 탐색하여 반환한다.
	 * <p>
	 * 탐색 대상 SubmodelElement 객체가 SubmodelElementCollection이 아닌 경우에는 {@code null}를 반환한다.
	 *
	 * @param smc       탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName 탐색할 idShort 이름.
	 * @return idShort가 fieldName인 하위 요소. 존재하지 않는 경우에는 {@code null}를 반환한다.
	 */
	public static SubmodelElement findFieldById(SubmodelElement smc, String fieldName) {
		if ( smc instanceof SubmodelElementCollection coll ) {
			return Funcs.findFirst(coll.getValue(), field -> field.getIdShort().equals(fieldName));
		}
		else {
			return null;
		}
	}
	
	/**
	 * 주어진 SubmodelElement에서 idShort가 fieldName인 하위 요소를 탐색하여 지정된 class 타입으로 반환한다.
	 * <p>
	 * 탐색 대상 SubmodelElement 객체가 SubmodelElementCollection이 아닌 경우에는
	 * {@code null}를 반환한다.
	 *
	 * @param <T>         탐색 결과로 기대되는 SubmodelElement의 class 타입.
	 * @param smc         탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName   탐색할 idShort 이름.
	 * @param outputClass 탐색 결과로 기대되는 SubmodelElement의 class 객체.
	 * @return idShort가 fieldName인 하위 요소. 존재하지 않는 경우에는 {@code null}를 반환한다.
	 */
	public static <T extends SubmodelElement> T
	findFieldById(SubmodelElement smc, String fieldName, Class<T> outputClass) {
		if ( smc instanceof SubmodelElementCollection coll ) {
			SubmodelElement field = Funcs.findFirst(coll.getValue(), f -> f.getIdShort().equals(fieldName));
			if ( field != null ) {
				return cast(field, outputClass);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	/**
	 * 주어진 SubmodelElement에서 idShort가 fieldName인 하위 요소를 탐색하여 반환한다.
	 * <p>
	 * 탐색 결과가 존재하지 않는 경우에는 IllegalArgumentException이 발생한다.
	 * 탐색 대상 SubmodelElement 객체가 SubmodelElementCollection이 아닌 경우에는
	 * {@link ClassCastException}이 발생한다.
	 *
	 * @param smc       탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName 탐색할 idShort 이름.
	 * @return idShort가 fieldName인 하위 요소.
	 * @throws IllegalArgumentException 탐색 결과가 존재하지 않는 경우.
	 * @throws ClassCastException	탐색 대상이 SubmodelElementCollection이 아닌 경우.
	 */
	public static SubmodelElement getFieldById(SubmodelElement smc, String fieldName)
		throws IllegalArgumentException {
		SubmodelElement field = findFieldById(smc, fieldName);
		if ( field != null ) {
			return field;
		}

		String fieldNames = FStream.from(((SubmodelElementCollection)smc).getValue())
									.map(SubmodelElement::getIdShort)
									.join(", ");
		String msg = String.format("Failed to find the field '%s' from %s{%s}",
									fieldName, smc.getIdShort(), fieldNames);
		throw new IllegalArgumentException(msg);
	}
	
	/**
	 * 주어진 SubmodelElement에서 idShort가 fieldName인 하위 요소를 탐색하여 지정된 class 타입으로 반환한다.
	 * <p>
	 * 탐색 결과가 존재하지 않거나 {@code outputClass} 타입이 아닌 경우에는
	 * IllegalArgumentException이 발생한다. 탐색 대상 SubmodelElement 객체가
	 * SubmodelElementCollection이 아닌 경우에는 {@link ClassCastException}이 발생한다.
	 *
	 * @param <T>         탐색 결과로 기대되는 SubmodelElement의 class 타입.
	 * @param smc         탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName   탐색할 idShort 이름.
	 * @param outputClass 탐색 결과로 기대되는 SubmodelElement의 class 객체.
	 * @return idShort가 fieldName인 하위 요소.
	 * @throws IllegalArgumentException 탐색 결과가 존재하지 않거나 {@code outputClass} 타입이 아닌 경우.
	 * @throws ClassCastException	탐색 대상이 SubmodelElementCollection이 아닌 경우.
	 */
	public static <T extends SubmodelElement> T getFieldById(SubmodelElement smc, String fieldName,
                                    						Class<T> outputClass) throws IllegalArgumentException {
		SubmodelElement field = getFieldById(smc, fieldName);
		return cast(field, outputClass);
	}
	
	/**
	 * 주어진 SubmodelElementCollection에서 idShort가 fieldName인 하위 {@link Property} 요소를
	 * 탐색하여 그 값 문자열을 반환한다.
	 * <p>
	 * 탐색 결과가 존재하지 않거나 Property가 아닌 경우에는 IllegalArgumentException이 발생한다.
	 *
	 * @param smc       탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName 탐색할 idShort 이름.
	 * @return idShort가 fieldName인 하위 Property의 값. 값이 없으면 {@code null}.
	 * @throws IllegalArgumentException {@code smc}가 {@code null}이거나, 탐색 결과가 존재하지 않거나
	 *                                  Property가 아닌 경우.
	 */
	public static String getStringFieldById(SubmodelElementCollection smc, String fieldName) {
		Preconditions.checkNotNullArgument(smc, "SubmodelElementCollection is null");
		return getFieldById(smc, fieldName, Property.class).getValue();
	}
	/**
	 * 주어진 SubmodelElementCollection에서 idShort가 fieldName인 하위 {@link Property} 요소를
	 * 탐색하여 그 값을 {@link Double}로 파싱하여 반환한다.
	 *
	 * @param smc       탐색 대상 SubmodelElementCollection 객체.
	 * @param fieldName 탐색할 idShort 이름.
	 * @return 하위 Property의 값을 파싱한 {@link Double}. 값이 없으면 {@code null}.
	 * @throws IllegalArgumentException {@code smc}가 {@code null}이거나, 탐색 결과가 존재하지 않거나
	 *                                  Property가 아닌 경우.
	 * @throws NumberFormatException	Property 값이 실수 형식이 아닌 경우.
	 */
	public static Double getDoubleFieldById(SubmodelElementCollection smc, String fieldName) {
		Preconditions.checkNotNullArgument(smc, "SubmodelElementCollection is null");
		
		String val = getFieldById(smc, fieldName, Property.class).getValue();
		return (val != null) ? Double.parseDouble(val) : null;
	}

	/**
	 * 주어진 SubmodelElementCollection에서 idShort가 fieldName인 하위 요소를 탐색하여 삭제한다.
	 * <p>
	 * 탐색 결과가 존재하지 않는 경우는 무시한다.
	 *
	 * @param smc       탐색 대상 부모 SubmodelElementCollection 객체.
	 * @param idShort	삭제 대상 하위 요소의 idShort 식별자.
	 */
	public static void removeFieldById(SubmodelElementCollection smc, String idShort) {
		smc.getValue().removeIf(field -> field.getIdShort().equals(idShort));
	}

	/**
	 * 주어진 SubmodelElementCollection에서 idShort가 일치하는 첫 하위 요소를 새 요소로 교체한다.
	 * <p>
	 * 해당 idShort의 하위 요소가 없는 경우는 아무 것도 하지 않는다.
	 *
	 * @param smc		대상 부모 SubmodelElementCollection 객체.
	 * @param idShort	교체 대상 하위 요소의 idShort.
	 * @param newField	새 하위 요소.
	 */
	public static void replaceFieldById(SubmodelElementCollection smc, String idShort, SubmodelElement newField) {
		List<SubmodelElement> fieldList = smc.getValue();
		for ( int i = 0; i < fieldList.size(); ++i ) {
			SubmodelElement field = fieldList.get(i);
			if ( field.getIdShort().equals(idShort) ) {
				fieldList.set(i, newField);
				return;
			}
		}
	}
	
	/**
	 * 주어진 SubmodelElement의 리스트에 포함된 SubmodelElementCollection (SMC)들 중에서
	 * SubmodelElementCollection의 하위 요소 중에서 idShort가 'fieldName'인 Property를 찾아서
	 * 그 값이 'value'인 SubmodelElementCollection을 찾아서 반환한다.
	 *
	 * @param smeList	검색 대상 SubmodelElement 리스트.
	 * @param fieldName	필드 이름.
	 * @param value		찾을 값.
	 * @return	처음으로 검색된 SubmodelElementCollection과 그 순번. 없으면 빈 {@link Optional}.
	 * 			순번은 {@code smeList}에서 SubmodelElementCollection만 셌을 때의 위치이다.
	 */
	public static Optional<Indexed<SubmodelElementCollection>>
	findFieldSMCByIdValue(List<SubmodelElement> smeList, String fieldName, String value) {
		return FStream.from(smeList)
						.castSafely(SubmodelElementCollection.class)
						.zipWithIndex()
						.findFirst(idxed -> {
							SubmodelElementCollection smc = idxed.value();
							var prop = findFieldById(smc, fieldName, Property.class);
							if ( prop != null ) {
								return prop.getValue().equals(value);
							}
							else {
								return false;
							}
						})
						.toOptional();
	}
	/**
	 * {@link #findFieldSMCByIdValue(List, String, String)}와 동일하게 검색하되,
	 * 검색 결과가 없으면 예외를 발생시킨다.
	 *
	 * @param smeList	검색 대상 SubmodelElement 리스트.
	 * @param fieldName	필드 이름.
	 * @param value		찾을 값.
	 * @return	처음으로 검색된 SubmodelElementCollection과 그 순번.
	 * @throws IllegalArgumentException	조건을 만족하는 SubmodelElementCollection이 없는 경우.
	 */
	public static Indexed<SubmodelElementCollection>
	getFieldSMCByIdValue(List<SubmodelElement> smeList, String fieldName, String value) throws IllegalArgumentException {
		return findFieldSMCByIdValue(smeList, fieldName, value)
				.orElseThrow(() -> {
					String msg = String.format("Failed to find SMC of %s=%s", fieldName, value);
					return new IllegalArgumentException(msg);
				});
	}
	
	/**
	 * 주어진 idShort, 값 문자열, value 타입으로 {@link Property}를 생성한다.
	 *
	 * @param idShort	Property의 idShort.
	 * @param value		값 문자열.
	 * @param valueType	Property의 value 타입.
	 * @return	생성된 Property 객체.
	 */
	public static DefaultProperty newProperty(String idShort, String value, DataTypeDefXsd valueType) {
		return new DefaultProperty.Builder()
									.idShort(idShort)
									.value(value)
									.valueType(valueType)
									.build();
	}
	/**
	 * 주어진 Java 값으로부터 {@link Property}를 생성한다.
	 * <p>
	 * value 타입은 {@link DataTypes#fromJavaObject(Object)}로 결정하고, 값 문자열은 해당
	 * 데이터 타입의 {@code toValueString}으로 변환한다.
	 *
	 * @param idShort	Property의 idShort.
	 * @param value		Property 값 객체.
	 * @return	생성된 Property 객체.
	 */
	public static DefaultProperty newProperty(String idShort, Object value) {
		DataType<?> dtype = DataTypes.fromJavaObject(value);
		return newProperty(idShort, dtype.toValueString(value), dtype.getTypeDefXsd());
	}
	
	/**
	 * 주어진 idShort과 SubmodelElement 리스트를 기반으로 새로운 SubmodelElementCollection 객체를 생성하여 반환한다.
	 *
	 * @param idShort	생성될 SubmodelElementCollection의 idShort 값.
	 * @param elements	생성될 SubmodelElementCollection의 하위 요소로 포함될 SubmodelElement 리스트.
	 * @return	생성된 SubmodelElementCollection 객체.
	 */
	public static DefaultSubmodelElementCollection newSubmodelElementCollection(String idShort,
																				List<SubmodelElement> elements) {
		return new DefaultSubmodelElementCollection.Builder()
													.idShort(idShort)
													.value(elements)
													.build();
	}
	
	/**
	 * 주어진 idShort과 SubmodelElement 리스트를 기반으로 새로운 SubmodelElementList 객체를 생성하여 반환한다.
	 * <p>
	 * 원소 SubmodelElement의 타입은 Property가 아니어야 한다.
	 *
	 * @param idShort 생성될 SubmodelElementList의 idShort 값.
	 * @param elements  생성될 SubmodelElementList의 요소로 포함될 SubmodelElement 리스트.
	 * @param orderRelevant 생성될 SubmodelElementList의 요소 순서가 중요한지 여부.
	 * @param elementType 생성될 SubmodelElementList의 요소 타입을 나타내는 AasSubmodelElements 열거형 값.
	 * @return 생성된 SubmodelElementList 객체.
	 * @throws IllegalArgumentException	{@code elementType}이 {@code PROPERTY}인 경우.
	 */
	public static DefaultSubmodelElementList newSubmodelElementList(String idShort, List<SubmodelElement> elements,
			                                                        boolean orderRelevant,
			                                                        AasSubmodelElements elementType) {
		Preconditions.checkArgument(elementType != AasSubmodelElements.PROPERTY, "elementType should not be PROPERTY");
		
		return new DefaultSubmodelElementList.Builder()
												.idShort(idShort)
												.orderRelevant(orderRelevant)
												.typeValueListElement(elementType)
												.value(elements)
												.build();
	}
	
	/**
	 * 주어진 idShort과 Property 원소 리스트를 기반으로 새로운 SubmodelElementList 객체를 생성하여 반환한다.
	 * 
	 * @param idShort	생성될 SubmodelElementList의 idShort 값.
	 * @param elements	생성될 SubmodelElementList의 요소로 포함될 Property 리스트.
	 * @param orderRelevant 생성될 SubmodelElementList의 요소 순서가 중요한지 여부.
	 * @param xsdType	생성될 SubmodelElementList의 요소 타입을 나타내는 DataTypeDefXsd 열거형 값.
	 * 					이 값은 Property의 valueType과 호환되어야 한다.
	 * @return 생성된 SubmodelElementList 객체.
	 */
	public static DefaultSubmodelElementList newSubmodelElementList(String idShort, List<Property> elements,
			                                                        boolean orderRelevant,
			                                                        DataTypeDefXsd xsdType) {
		List<SubmodelElement> elms = Funcs.map(elements, sme -> (SubmodelElement)sme);
		return new DefaultSubmodelElementList.Builder()
												.idShort(idShort)
												.orderRelevant(orderRelevant)
												.typeValueListElement(AasSubmodelElements.PROPERTY)
												.valueTypeListElement(xsdType)
												.value(elms)
												.build();
	}
	
	/**
	 * {@code source}의 값으로 {@code target}의 값을 갱신한다.
	 * <p>
	 * 두 요소가 SubmodelElementList인 경우에는 먼저 {@code target}의 원소 수를 {@code source}에
	 * 맞춘다. 원소가 많으면 뒤쪽 원소를 제거하고, 적으면 {@code source}의 나머지 원소 객체를
	 * 그대로 추가한다. 이후 {@link ElementValues}를 이용해 {@code source}의 값을 {@code target}에
	 * 반영한다. 빈 {@code target} 리스트에 원소를 추가한 경우는 값 반영 단계를 생략한다.
	 *
	 * @param target	값을 갱신할 SubmodelElement.
	 * @param source	새 값을 가진 SubmodelElement.
	 * @throws IllegalArgumentException	두 요소의 구현 클래스가 다른 경우.
	 */
	public static void updateValue(SubmodelElement target, SubmodelElement source) {
		if ( target.getClass() != source.getClass() ) {
			throw new IllegalArgumentException("Incompatible SubmodelElement type: " + target.getClass()
												+ " vs " + source.getClass());
		}
		
		if ( target instanceof SubmodelElementList tarList ) {
			SubmodelElementList srcList = (SubmodelElementList)source;
			if ( tarList.getValue().size() > srcList.getValue().size() ) {
				var reduced = tarList.getValue().subList(0, srcList.getValue().size());
				tarList.setValue(reduced);
			}
			else if ( tarList.getValue().size() < srcList.getValue().size() ) {
				var extended = srcList.getValue().subList(tarList.getValue().size(),
															srcList.getValue().size());
				tarList.getValue().addAll(extended);
				if ( tarList.getValue().size() == extended.size() ) {
					return;
				}
			}
		}
		ElementValue ev = ElementValues.getValue(source);
		ElementValues.update(target, ev);
	}

	/**
	 * 주어진 Submodel이 InformationModel Submodel인지 여부를 반환한다.
	 *
	 * @param sm	검사 대상 Submodel 객체.
	 * @return	주어진 Submodel이 InformationModel Submodel인 경우에는 true, 그렇지 않은 경우에는 false.
	 */
	public static boolean isInformationModel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return InformationModel.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 Submodel이 Data Submodel인지 여부를 반환한다.
	 *
	 * @param sm 검사 대상 Submodel 객체.
	 * @return 주어진 Submodel이 Data Submodel인 경우에는 true, 그렇지 않은 경우에는 false.
	 */
	public static boolean isDataSubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return Data.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 Submodel이 TimeSeries Submodel인지 여부를 반환한다.
	 *
	 * @param sm 검사 대상 Submodel 객체.
	 * @return 주어진 Submodel이 TimeSeries Submodel인 경우에는 true, 그렇지 않은 경우에는 false.
	 */
	public static boolean isTimeSeriesSubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return TimeSeries.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 SubmodelElement가 ParameterValue SubmodelElement인지 여부를 반환한다.
	 *
	 * @param sme 검사 대상 SubmodelElement 객체.
	 * @return 주어진 SubmodelElement가 ParameterValue SubmodelElement인 경우에는 true, 그렇지
	 *         않은 경우에는 false.
	 */
	public static boolean isParameterValue(SubmodelElement sme) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sme.getSemanticId());
		return ParameterValue.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 Data Submodel에서 사용하는 ParameterValue SubmodelElement의 idShort 경로 접두어를 반환한다.
	 * <p>
	 * Data Submodel의 DataInfo 요소에 Equipment 요소가 포함된 경우에는
	 * "DataInfo.Equipment.EquipmentParameterValues"를, Operation 요소가 포함된 경우에는
	 * "DataInfo.Operation.OperationParameterValues"를 반환한다.
	 *
	 * @param dataSubmodel ParameterValue SubmodelElement가 포함된 Data Submodel 객체.
	 * @return ParameterValue SubmodelElement의 idShort 경로 접두어.
	 * @throws IllegalArgumentException Data Submodel의 DataInfo 요소에 Equipment 또는
	 *                                  Operation 요소가 포함되어 있지 않은 경우.
	 */
	public static String getParameterValuePrefix(Submodel dataSubmodel) {
		SubmodelElementCollection dataInfo = traverse(dataSubmodel, "DataInfo", SubmodelElementCollection.class);
		if ( containsFieldById(dataInfo, "Equipment") ) {
			return "DataInfo.Equipment.EquipmentParameterValues";
		}
		else if ( containsFieldById(dataInfo, "Operation") ) {
			return "DataInfo.Operation.OperationParameterValues";
		}
		else {
			throw new IllegalArgumentException("Invalid DataInfo: " + dataInfo.getIdShort());
		}
	}
	
	/**
	 * 주어진 Submodel이 AI Submodel인지 여부를 반환한다.
	 *
	 * @param sm 검사 대상 Submodel 객체.
	 * @return 주어진 Submodel이 AI Submodel인 경우에는 true, 그렇지 않은 경우에는 false.
	 */
	public static boolean isAISubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return AI.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 Submodel이 Simulation Submodel인지 여부를 반환한다.
	 *
	 * @param sm 검사 대상 Submodel 객체.
	 * @return 주어진 Submodel이 Simulation Submodel인 경우에는 true, 그렇지 않은 경우에는 false.
	 */
	public static boolean isSimulationSubmodel(Submodel sm) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
		return Simulation.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 SubmodelElement가 Equipment SubmodelElement인지 여부를 반환한다.
	 *
	 * @param element 검사 대상 SubmodelElement 객체.
	 * @return 주어진 SubmodelElement가 Equipment SubmodelElement인 경우에는 true, 그렇지 않은
	 *         경우에는 false.
	 */
	public static boolean isEquipment(SubmodelElement element) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(element.getSemanticId());
		return Equipment.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 주어진 SubmodelElement가 Operation SubmodelElement인지 여부를 반환한다.
	 *
	 * @param element 검사 대상 SubmodelElement 객체.
	 * @return 주어진 SubmodelElement가 Operation SubmodelElement인 경우에는 true, 그렇지 않은
	 *         경우에는 false.
	 */
	public static boolean isAASOperation(SubmodelElement element) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(element.getSemanticId());
		return Operation.SEMANTIC_ID.equals(semanticId);
	}
	
	/**
	 * 파라미터 표현식을 해당 ParameterValue 요소의 idShort 경로로 변환한다.
	 * <p>
	 * {@code paramExpr}가 정수이면 이를 파라미터 인덱스로 사용한다. 그렇지 않으면 첫
	 * {@code '.'}(없으면 첫 {@code '['}) 앞부분을 파라미터 식별자로, 나머지를 하위 경로로 보고
	 * {@code resolveParameterIndex}로 식별자의 인덱스를 구한다. 결과 경로는
	 * {@code <paramPathPrefix>[<인덱스>].ParameterValue<하위 경로>} 형태이다.
	 *
	 * @param paramPathPrefix	ParameterValue 리스트의 idShort 경로
	 * 							(예: {@code DataInfo.Equipment.EquipmentParameterValues}).
	 * @param paramExpr	파라미터 인덱스 또는 파라미터 식별자(뒤에 하위 경로가 붙을 수 있음).
	 * @param resolveParameterIndex	파라미터 식별자를 인덱스로 변환하는 함수.
	 * @return	ParameterValue 요소의 idShort 경로.
	 */
	public static String resolveParameterValueElementPath(String paramPathPrefix, String paramExpr,
															Function<String,Integer> resolveParameterIndex) {
		// parameter expression에서 parameter ID를 추출하여 해당 parameter의 인덱스를 구한다.
		String subPath = "";
		int paramIdx;
		try {
			// 일단 parmeter-id가 숫자인 것으로 가정하고 파싱을 실시하여
			// parameter의 idShortPath를 생성하고, 숫자가 아니어서 예외가 발생한 경우에는
			// 일반적인 id 기반의 idShortPath를 생성한다.
			paramIdx = Integer.parseInt(paramExpr);
		}
		catch ( NumberFormatException e ) {
			String paramId;
			int idx = paramExpr.indexOf('.');
			if ( idx >= 0 ) {
				paramId = paramExpr.substring(0, idx);
				subPath = paramExpr.substring(idx);
			}
			else {
				idx = paramExpr.indexOf('[');
				if ( idx >= 0 ) {
					paramId = paramExpr.substring(0, idx);
					subPath = paramExpr.substring(idx);
				}
				else {
					paramId = paramExpr;
				}
			}
			paramIdx = resolveParameterIndex.apply(paramId);
		}
		return String.format("%s[%d].ParameterValue%s", paramPathPrefix, paramIdx, subPath);
	}
	
	/**
	 * 연산형(AI, Simulation) Submodel의 입출력 인자 구성을 기술하는 클래스.
	 *
	 * @see SubmodelUtils#loadOperationSubmodelDescriptor(Submodel)
	 */
	public static class OperationSubmodelDescriptor {
		private final String m_id;
		private final String m_semanticIdString;
		private final Map<String,SubmodelArgumentDescriptor> m_inputs;
		private final Map<String,SubmodelArgumentDescriptor> m_outputs;
		
		/**
		 * 연산형 Submodel 기술자를 생성한다.
		 *
		 * @param id	Submodel 식별 이름 (idShort).
		 * @param semanticIdStr	Submodel의 semanticId 문자열.
		 * @param inputs	입력 인자 식별자와 인자 기술자의 맵.
		 * @param outputs	출력 인자 식별자와 인자 기술자의 맵.
		 */
		public OperationSubmodelDescriptor(String id, String semanticIdStr,
											Map<String,SubmodelArgumentDescriptor> inputs,
											Map<String,SubmodelArgumentDescriptor> outputs) {
			m_id = id;
			m_semanticIdString = semanticIdStr;
			m_inputs = inputs;
			m_outputs = outputs;
		}
		
		/**
		 * Submodel 식별 이름(idShort)을 반환한다.
		 *
		 * @return	Submodel idShort.
		 */
		public String getId() {
			return m_id;
		}
		
		/**
		 * Submodel의 semanticId 문자열을 반환한다.
		 *
		 * @return	semanticId 문자열.
		 */
		public String getSemanticIdString() {
			return m_semanticIdString;
		}
		
		/**
		 * 입력 인자 기술자 맵을 반환한다.
		 *
		 * @return	입력 인자 식별자와 인자 기술자의 맵.
		 */
		public Map<String,SubmodelArgumentDescriptor> getInputs() {
			return m_inputs;
		}
		
		/**
		 * 출력 인자 기술자 맵을 반환한다.
		 *
		 * @return	출력 인자 식별자와 인자 기술자의 맵.
		 */
		public Map<String,SubmodelArgumentDescriptor> getOutputs() {
			return m_outputs;
		}
	}
	
	/**
	 * 연산형 Submodel의 입력 또는 출력 인자 하나를 기술하는 클래스.
	 */
	public static class SubmodelArgumentDescriptor {
		private final String m_id;
		private final int m_index;
		private final String m_idShortPath;
		private final SubmodelElement m_sme;
		
		/**
		 * 인자 기술자를 생성한다.
		 *
		 * @param id	인자 식별자 ({@code InputID}/{@code OutputID} 필드 값).
		 * @param index	인자 리스트에서의 인덱스.
		 * @param idShortPath	인자 값 요소의 idShort 경로 (예: {@code AIInfo.Inputs[0].InputValue}).
		 * @param sme	인자 값 SubmodelElement.
		 */
		public SubmodelArgumentDescriptor(String id, int index, String idShortPath, SubmodelElement sme) {
			m_id = id;
			m_index = index;
			m_idShortPath = idShortPath;
			m_sme = sme;
		}
		
		/**
		 * 인자 식별자를 반환한다.
		 *
		 * @return	인자 식별자.
		 */
		public String getId() {
			return m_id;
		}
		
		/**
		 * 인자 리스트에서의 인덱스를 반환한다.
		 *
		 * @return	인자 인덱스.
		 */
		public int getIndex() {
			return m_index;
		}
		
		/**
		 * Submodel 기준 인자 값 요소의 idShort 경로를 반환한다.
		 *
		 * @return	idShort 경로.
		 */
		public String idShortPath() {
			return m_idShortPath;
		}
		
		/**
		 * 인자 값 SubmodelElement를 반환한다.
		 *
		 * @return	인자 값 SubmodelElement.
		 */
		public SubmodelElement getSubmodelElement() {
			return m_sme;
		}
	}
	
	/**
	 * 연산형 Submodel(AI 또는 Simulation)에서 입출력 인자 구성을 읽어 기술자를 생성한다.
	 * <p>
	 * Submodel의 semanticId에 따라 {@code AIInfo} 또는 {@code SimulationInfo} 하위의
	 * {@code Inputs}/{@code Outputs} SubmodelElementList를 읽고, 각 원소의
	 * {@code InputID}/{@code InputValue}(또는 {@code OutputID}/{@code OutputValue}) 필드로
	 * 인자 기술자를 만든다.
	 *
	 * @param submodel	대상 Submodel.
	 * @return	연산형 Submodel 기술자.
	 * @throws ModelValidationException	semanticId가 없거나 AI/Simulation Submodel이 아닌 경우.
	 * @throws ResourceNotFoundException	입출력 인자 리스트 또는 인자 필드가 없는 경우.
	 */
	public static OperationSubmodelDescriptor loadOperationSubmodelDescriptor(Submodel submodel) {
		// 입출력 인자 SubmodelElement를 가져온다.
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
		if ( semanticId == null ) {
			throw new ModelValidationException("Submodel semanticId is missing: submodel idShort="
                                                + submodel.getIdShort());
		}
		String pathPrefix = switch ( semanticId ) {
            case MDTSemanticIds.SUBMODEL_AI -> "AIInfo";
            case MDTSemanticIds.SUBMODEL_SIMULATION -> "SimulationInfo";
            default ->
                throw new ModelValidationException("Unsupported Operation Submodel semanticId: " + semanticId);
        };
        SubmodelElementList inputsSml = traverse(submodel, pathPrefix + ".Inputs", SubmodelElementList.class);
        SubmodelElementList outputsSml = traverse(submodel, pathPrefix + ".Outputs", SubmodelElementList.class);
        
        return new OperationSubmodelDescriptor(submodel.getIdShort(), semanticId,
								        		loadArgumentList(inputsSml, pathPrefix + ".Inputs", "Input"),
								        		loadArgumentList(outputsSml, pathPrefix + ".Outputs", "Output"));
	}
	private static Map<String,SubmodelArgumentDescriptor> loadArgumentList(SubmodelElementList argSmcList,
																	String idShortPathPrefix, String argKind) {
		return FStream.from(argSmcList.getValue())
				.zipWithIndex()
				.map(idxed -> {
					Property idProp = SubmodelUtils.traverse(idxed.value(), argKind+"ID", Property.class);
					SubmodelElement argValue = SubmodelUtils.traverse(idxed.value(), argKind+"Value");
					String idShortPath = String.format("%s[%d].%sValue", idShortPathPrefix, idxed.index(), argKind);
					return new SubmodelArgumentDescriptor(idProp.getValue(), idxed.index(), idShortPath, argValue);
				})
				.tagKey(SubmodelArgumentDescriptor::getId)
				.toMap();
	}

	private static @Nullable SubmodelElement hop(SubmodelElement sme, String seg) {
		if ( sme instanceof SubmodelElementCollection smc ) {
			return FStream.from(smc.getValue())
							.findFirst(e ->  e.getIdShort() != null && e.getIdShort().equals(seg))
							.getOrNull();
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			// Navigate 대상이 SubmodelElementList인 경우에는 'seg'가 숫자일 수도 있기 때문에
			// integer로의 파싱을 시도한다. 만일 숫자가 아닌 경우에는 'idx'가 -1로 설정된다.
			int idx = Try.of(() -> Integer.parseInt(seg)).getOrElse(-1);
			if ( idx >= 0 ) {
				if ( sml.getValue().size() > idx ) {
					return sml.getValue().get(idx);
				}
				else {
					return null;
				}
			}
			else {
				return FStream.from(sml.getValue())
								.findFirst(e ->  e.getIdShort() != null && e.getIdShort().equals(seg))
								.getOrNull();
			}
		}
		
		return null;
	}

	/**
	 * AAS 참조(Reference)의 문자열 직렬화 표현을 파싱하여 {@link Reference} 객체를 생성한다.
	 * <p>
	 * 문자열은 선택적인 {@code [ModelRef]}/{@code [ExternalRef]} 접두어(대시 사이에 referred
	 * semanticId를 넣은 {@code [ModelRef- ... -]} 형태도 가능)와 쉼표로 구분된
	 * {@code (KeyType)value} 형태의 키 목록으로 구성된다. 접두어가 없으면 첫 키의 타입이
	 * {@code GlobalReference}인 경우 외부 참조로, 그 외에는 모델 참조로 간주한다.
	 *
	 * @param serialized	참조 직렬화 문자열.
	 * @return	파싱된 {@link Reference} 객체.
	 * @throws IllegalArgumentException	문자열이 비어 있거나 참조 타입이 올바르지 않은 경우.
	 */
	public static Reference parseReferenceSerialization(String serialized) {
		serialized = serialized.trim();
		Preconditions.checkArgument(!serialized.isEmpty(), "empty reference serialization");

		Reference semanticId = null;
		ReferenceTypes refType = null;
		if ( serialized.startsWith("[") ) {
			int end = serialized.indexOf(']');
			String refTypeStr = serialized.substring(1, end).trim();
			serialized = serialized.substring(end+1);

			int dash = refTypeStr.indexOf('-');
			String refTypeToken = (dash >= 0) ? refTypeStr.substring(0, dash).trim() : refTypeStr;
			refType = switch ( refTypeToken ) {
				case "ModelRef" -> ReferenceTypes.MODEL_REFERENCE;
				case "ExternalRef" -> ReferenceTypes.EXTERNAL_REFERENCE;
				default -> throw new IllegalArgumentException("invalid ReferenceTypes: " + refTypeStr);
			};

			if ( dash >= 0 ) {
				String semanticIdStr = refTypeStr.substring(dash+1, refTypeStr.lastIndexOf('-')).trim();
				semanticId = parseReferenceSerialization(semanticIdStr);
			}
		}
		
		List<Key> keyList = parseKeyListSerialization(serialized);
		if ( refType == null ) {
			if ( keyList.get(0).getType() == KeyTypes.GLOBAL_REFERENCE ) {
				refType = ReferenceTypes.EXTERNAL_REFERENCE;
			}
			else {
				refType = ReferenceTypes.MODEL_REFERENCE;
			}
		}
		
		return new DefaultReference.Builder()
									.keys(keyList)
									.type(refType)
									.referredSemanticId(semanticId)
									.build();
	}
	
	private static List<Key> parseKeyListSerialization(String str) {
		return CSV.parseCsv(str).map(keyStr -> parseKeySerialization(keyStr)).toList();
	}

	private static final Pattern KEY_SER_PATTERN = Pattern.compile("\\((\\w+)\\)(\\S+)");
	private static Key parseKeySerialization(String str) {
		Matcher matcher = KEY_SER_PATTERN.matcher(str);
		matcher.find();
		
		KeyTypes type = KEY_TYPES_MAP.get(matcher.group(1));
		return new DefaultKey.Builder().type(type).value(matcher.group(2)).build();
	}
	
	private static final Map<String,KeyTypes> KEY_TYPES_MAP = Maps.newHashMap();
	static {
		KEY_TYPES_MAP.put("AnnotatedRelationshipElement", KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT);
		KEY_TYPES_MAP.put("AssetAdministrationShell", KeyTypes.ASSET_ADMINISTRATION_SHELL);
		KEY_TYPES_MAP.put("BasicEventElement", KeyTypes.BASIC_EVENT_ELEMENT);
		KEY_TYPES_MAP.put("BaseEventElement", KeyTypes.BASIC_EVENT_ELEMENT);	// 이전에 쓰던 잘못된 이름도 읽는다.
		KEY_TYPES_MAP.put("Blob", KeyTypes.BLOB);
		KEY_TYPES_MAP.put("Capability", KeyTypes.CAPABILITY);
		KEY_TYPES_MAP.put("ConceptDescription", KeyTypes.CONCEPT_DESCRIPTION);
		KEY_TYPES_MAP.put("DataElement", KeyTypes.DATA_ELEMENT);
		KEY_TYPES_MAP.put("Entity", KeyTypes.ENTITY);
		KEY_TYPES_MAP.put("EventElement", KeyTypes.EVENT_ELEMENT);
		KEY_TYPES_MAP.put("File", KeyTypes.FILE);
		KEY_TYPES_MAP.put("FragmentReference", KeyTypes.FRAGMENT_REFERENCE);
		KEY_TYPES_MAP.put("GlobalReference", KeyTypes.GLOBAL_REFERENCE);
		KEY_TYPES_MAP.put("Identifiable", KeyTypes.IDENTIFIABLE);
		KEY_TYPES_MAP.put("MultiLanguageProperty", KeyTypes.MULTI_LANGUAGE_PROPERTY);
		KEY_TYPES_MAP.put("Operation", KeyTypes.OPERATION);
		KEY_TYPES_MAP.put("Property", KeyTypes.PROPERTY);
		KEY_TYPES_MAP.put("Range", KeyTypes.RANGE);
		KEY_TYPES_MAP.put("Referable", KeyTypes.REFERABLE);
		KEY_TYPES_MAP.put("ReferenceElement", KeyTypes.REFERENCE_ELEMENT);
		KEY_TYPES_MAP.put("RelationshipElement", KeyTypes.RELATIONSHIP_ELEMENT);
		KEY_TYPES_MAP.put("Submodel", KeyTypes.SUBMODEL);
		KEY_TYPES_MAP.put("SubmodelElement", KeyTypes.SUBMODEL_ELEMENT);
		KEY_TYPES_MAP.put("SubmodelElementCollection", KeyTypes.SUBMODEL_ELEMENT_COLLECTION);
		KEY_TYPES_MAP.put("SubmodelElementList", KeyTypes.SUBMODEL_ELEMENT_LIST);
	}


	/**
	 * JSON 객체를 {@link DefaultSubmodelReference#parseJson(JsonNode)}로 파싱하여
	 * {@link MDTSubmodelReference}로 역직렬화하는 Jackson deserializer.
	 */
	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<MDTSubmodelReference> {
		/**
		 * 대상 값 클래스 없이 deserializer를 생성한다.
		 */
		public Deserializer() {
			this(null);
		}
		/**
		 * deserializer를 생성한다.
		 *
		 * @param vc	역직렬화 대상 값 클래스.
		 */
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public MDTSubmodelReference deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode node = parser.getCodec().readTree(parser);
			// 객체가 아니면 parseJson이 JacksonDeserializationException으로 거부한다(이전에는 IllegalStateException).
			return DefaultSubmodelReference.parseJson(node);
		}
	}

	/**
	 * {@link MDTSubmodelReference#serialize(JsonGenerator)}를 이용하는 Jackson serializer.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<MDTSubmodelReference> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<MDTSubmodelReference> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(MDTSubmodelReference ref, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			ref.serialize(gen);
		}
	}
}
