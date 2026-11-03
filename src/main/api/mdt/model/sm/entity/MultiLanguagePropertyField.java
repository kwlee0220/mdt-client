package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

/**
 * 엔티티 필드를 AAS {@code MultiLanguageProperty} SubmodelElement에 대응시키는 annotation.
 * <p>
 * 대상 필드의 타입은 {@link mdt.model.sm.value.MultiLanguagePropertyValue}이어야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.MULTI_LANGUAGE_PROPERTY)
public @interface MultiLanguagePropertyField {
	/**
	 * 필드에 대응되는 MultiLanguageProperty의 idShort.
	 *
	 * @return	idShort.
	 */
	public String idShort() default "";
	/**
	 * 필드 값이 {@code null}인 경우에도 값이 없는 MultiLanguageProperty를 생성할지 여부.
	 *
	 * @return	{@code null} 값 필드도 생성하면 {@code true}.
	 */
	public boolean keepNullField() default false;
}
