package mdt.model.sm;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import lombok.experimental.UtilityClass;

import utils.stream.FStream;

import mdt.aas.DataTypes;
import mdt.model.sm.value.MultiLanguagePropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class PropertyUtils {
	public static Property newProperty(String id, DataTypeDefXsd valueType, @Nullable String value) {
		return new DefaultProperty.Builder()
									.idShort(id)
									.value(value)
									.valueType(valueType)
									.build();
	}
	public static Property newProperty(String id, DataTypeDefXsd valueType) {
		return new DefaultProperty.Builder()
									.idShort(id)
									.valueType(valueType)
									.build();
	}
	
	public static Property STRING(String id, @Nullable String value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), value);
	}
	
	public static Property INT(String id, @Nullable Object value) {
		return newProperty(id, DataTypes.STRING.getTypeDefXsd(), (value != null) ? value.toString() : null);
	}
	public static Property INT(String id, int value) {
		return newProperty(id, DataTypes.INT.getTypeDefXsd(), DataTypes.INT.toValueString(value));
	}

	public static Property LONG(String id, @Nullable Long value) {
		return newProperty(id, DataTypes.LONG.getTypeDefXsd(), DataTypes.LONG.toValueString(value));
	}
	
	public static Property FLOAT(String id, @Nullable Float value) {
		return newProperty(id, DataTypes.FLOAT.getTypeDefXsd(), DataTypes.FLOAT.toValueString(value));
	}
	public static Property DOUBLE(String id, @Nullable Double value) {
		return newProperty(id, DataTypes.DOUBLE.getTypeDefXsd(), DataTypes.DOUBLE.toValueString(value));
	}

	public static Property DATE_TIME(String id, @Nullable Instant value) {
		return newProperty(id, DataTypes.DATE_TIME.getTypeDefXsd(), DataTypes.DATE_TIME.toValueString(value));
	}

	public static Property DATE(String id, @Nullable Date value) {
		return newProperty(id, DataTypes.DATE.getTypeDefXsd(), DataTypes.DATE.toValueString(value));
	}

	public static Property DURATION(String id, @Nullable Duration value) {
		return newProperty(id, DataTypes.DURATION.getTypeDefXsd(), DataTypes.DURATION.toValueString(value));
	}

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
