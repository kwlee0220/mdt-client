package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.PROPERTY)
public @interface PropertyField {
	public String idShort() default "";
	public String valueType() default "";
	public boolean keepNullField() default false;
}
