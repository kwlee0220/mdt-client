package mdt.model.sm.value;

/**
 * AAS DataElement({@code Property}, {@code File}, {@code Range}, {@code MultiLanguageProperty},
 * {@code ReferenceElement})의 값을 표현하는 {@link ElementValue}들의 공통 인터페이스.
 * <p>
 * 별도의 메소드를 정의하지 않으며, 값 타입을 DataElement 계열로 한정할 때 사용된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public sealed interface DataElementValue extends ElementValue
		permits PropertyValue, FileValue, RangeValue, MultiLanguagePropertyValue, ReferenceElementValue {
}
