package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;


/**
 * 엔티티 필드를 임의의 SubmodelElement에 그대로 대응시키는 annotation.
 * <p>
 * 대상 필드의 타입은 {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}(또는 그 하위 타입)이며,
 * 변환 없이 SubmodelElement 객체 자체가 필드 값으로 사용된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.SUBMODEL_ELEMENT)
public @interface SMElementField {
	/**
	 * 필드에 대응되는 SubmodelElement의 idShort.
	 *
	 * @return	idShort.
	 */
	public String idShort() default "";
}
