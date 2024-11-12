package mdt.model.sm.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface SMCollectionField {
	public String idShort() default "";
	public boolean keepNullField() default false;
	
	public Class<?> adaptorClass() default void.class;
}
