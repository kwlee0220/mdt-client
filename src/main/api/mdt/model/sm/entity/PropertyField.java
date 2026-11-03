package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

/**
 * 엔티티 필드를 AAS {@code Property} SubmodelElement에 대응시키는 annotation.
 * <p>
 * Enum 타입 필드는 Enum 이름을 값으로 하는 {@code xs:string} Property로 변환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.PROPERTY)
public @interface PropertyField {
	/**
	 * 필드에 대응되는 Property의 idShort.
	 *
	 * @return	idShort.
	 */
	public String idShort() default "";
	/**
	 * Property의 value 타입 이름 ({@link mdt.aas.DataTypes#fromDataTypeName(String)}로 해석됨).
	 * <p>
	 * 빈 문자열이면 AAS → 필드 변환 시에는 Property의 valueType을(없으면 {@code xs:string}),
	 * 필드 → AAS 변환 시에는 필드 값의 Java 클래스로부터 결정한 타입을 사용한다.
	 *
	 * @return	value 타입 이름.
	 */
	public String valueType() default "";
	/**
	 * 필드 값이 {@code null}인 경우에도 Property를 생성할지 여부.
	 * <p>
	 * 생성되는 Property의 value는 {@code null}이고, valueType은 {@link #valueType()}이 지정되어 있으면 그 타입,
	 * 그렇지 않으면 {@code xs:string}이다.
	 *
	 * @return	{@code null} 값 필드도 Property로 생성하면 {@code true}.
	 */
	public boolean keepNullField() default false;
}
