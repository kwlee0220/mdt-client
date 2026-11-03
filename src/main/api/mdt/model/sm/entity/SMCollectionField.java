package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;


/**
 * 엔티티 필드를 AAS {@code SubmodelElementCollection}에 대응시키는 annotation.
 * <p>
 * 대상 필드의 값은 {@link SubmodelElementEntity}이며, 해당 엔티티의
 * {@link SubmodelElementEntity#newSubmodelElement()}로 SubmodelElementCollection이 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION)
public @interface SMCollectionField {
	/**
	 * 필드에 대응되는 SubmodelElementCollection의 idShort.
	 * <p>
	 * 엔티티 → AAS 변환 시 빈 문자열이면 필드 이름이 idShort로 사용된다.
	 *
	 * @return	idShort.
	 */
	public String idShort() default "";
	/**
	 * 필드 값이 {@code null}인 경우에도 빈 SubmodelElementCollection을 생성할지 여부.
	 *
	 * @return	{@code null} 값 필드도 생성하면 {@code true}.
	 */
	public boolean keepNullField() default false;
	
	/**
	 * AAS → 필드 변환 시 생성할 {@link SubmodelElementEntity} 구현 클래스.
	 * <p>
	 * 지정하지 않으면({@code void.class}) 필드의 선언 타입을 사용한다. 지정된 경우 필드 타입에
	 * 대입 가능한 클래스여야 한다.
	 *
	 * @return	엔티티 구현 클래스.
	 */
	public Class<?> adaptorClass() default void.class;
}
