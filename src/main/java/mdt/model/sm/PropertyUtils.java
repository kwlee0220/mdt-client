package mdt.model.sm;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.jetbrains.annotations.Nullable;

import utils.stream.FStream;

import mdt.aas.DataTypes;
import mdt.model.sm.value.MultiLanguagePropertyValue;

/**
 * AAS {@link Property}/{@link MultiLanguageProperty} SubmodelElement 생성을 위한 유틸리티 클래스.
 * <p>
 * 타입별 팩토리 메소드는 주어진 값을 {@link DataTypes}의 해당 타입으로 값 문자열로 변환하여
 * {@code value}에, 대응되는 {@link DataTypeDefXsd}를 {@code valueType}에 설정한다.
 * 값이 {@code null}이면 {@code value}가 {@code null}인 Property가 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class PropertyUtils {
	private PropertyUtils() {
		throw new AssertionError("Should not be called: class=" + PropertyUtils.class.getName());
	}
	
	/**
	 * 주어진 idShort, value 타입, 값 문자열로 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param valueType	Property의 value 타입.
	 * @param value	값 문자열. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property newProperty(String id, DataTypeDefXsd valueType, @Nullable String value) {
		return new DefaultProperty.Builder()
									.idShort(id)
									.value(value)
									.valueType(valueType)
									.build();
	}
	/**
	 * 주어진 idShort와 value 타입으로 값이 없는 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param valueType	Property의 value 타입.
	 * @return	생성된 {@link Property}.
	 */
	public static Property newProperty(String id, DataTypeDefXsd valueType) {
		return new DefaultProperty.Builder()
									.idShort(id)
									.valueType(valueType)
									.build();
	}
	
	/**
	 * {@code xs:string} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	문자열 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property STRING(String id, @Nullable String value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), value);
	}
	
	/**
	 * {@code xs:int} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	정수 값.
	 * @return	생성된 {@link Property}.
	 */
	public static Property INT(String id, Integer value) {
		return newProperty(id, DataTypes.INT.getTypeDefXsd(), DataTypes.INT.toValueString(value));
	}

	/**
	 * {@code xs:long} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	정수 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property LONG(String id, @Nullable Long value) {
		return newProperty(id, DataTypes.LONG.getTypeDefXsd(), DataTypes.LONG.toValueString(value));
	}
	
	/**
	 * {@code xs:float} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	실수 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property FLOAT(String id, @Nullable Float value) {
		return newProperty(id, DataTypes.FLOAT.getTypeDefXsd(), DataTypes.FLOAT.toValueString(value));
	}
	/**
	 * {@code xs:double} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	실수 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property DOUBLE(String id, @Nullable Double value) {
		return newProperty(id, DataTypes.DOUBLE.getTypeDefXsd(), DataTypes.DOUBLE.toValueString(value));
	}

	/**
	 * {@code xs:dateTime} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	시각 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property DATE_TIME(String id, @Nullable Instant value) {
		return newProperty(id, DataTypes.DATE_TIME.getTypeDefXsd(), DataTypes.DATE_TIME.toValueString(value));
	}

	/**
	 * {@code xs:date} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	날짜 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property DATE(String id, @Nullable LocalDate value) {
		return newProperty(id, DataTypes.DATE.getTypeDefXsd(), DataTypes.DATE.toValueString(value));
	}

	/**
	 * {@code xs:duration} 타입의 {@link Property}를 생성한다.
	 *
	 * @param id	Property의 idShort.
	 * @param value	기간 값. {@code null}일 수 있다.
	 * @return	생성된 {@link Property}.
	 */
	public static Property DURATION(String id, @Nullable Duration value) {
		return newProperty(id, DataTypes.DURATION.getTypeDefXsd(), DataTypes.DURATION.toValueString(value));
	}

	/**
	 * 주어진 다국어 텍스트 값으로 {@link MultiLanguageProperty}를 생성한다.
	 *
	 * @param id	MultiLanguageProperty의 idShort.
	 * @param value	다국어 텍스트 값.
	 * @return	생성된 {@link MultiLanguageProperty}.
	 */
	public static MultiLanguageProperty MULTI_LANGUAGE(String id, MultiLanguagePropertyValue value) {
		List<LangStringTextType> textList
				= FStream.from(value.getLangTextAll())
							.map(langText -> new DefaultLangStringTextType.Builder()
																			.language(langText.getLanguage())
																			.text(langText.getText())
																			.build())
							.cast(LangStringTextType.class)
							.toList();
		return new DefaultMultiLanguageProperty.Builder()
						.idShort(id)
						.value(textList)
						.build();
	}
}
