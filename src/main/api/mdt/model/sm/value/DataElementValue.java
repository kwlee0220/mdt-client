package mdt.model.sm.value;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public sealed interface DataElementValue extends ElementValue
		permits PropertyValue, FileValue, RangeValue, MultiLanguagePropertyValue, ReferenceElementValue {
}
