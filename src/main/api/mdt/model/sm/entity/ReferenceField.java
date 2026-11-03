package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;


/**
 * 엔티티 필드를 AAS {@code ReferenceElement} SubmodelElement에 대응시키는 annotation.
 * <p>
 * {@link AbstractSMEContainerEntity}의 필드 변환 로직은 현재 이 annotation을 처리하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.REFERENCE_ELEMENT)
public @interface ReferenceField {
	/**
	 * 필드에 대응되는 ReferenceElement의 idShort.
	 *
	 * @return	idShort.
	 */
	public String idShort() default "";
}
