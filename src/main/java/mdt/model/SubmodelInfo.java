package mdt.model;

import static java.lang.annotation.ElementType.TYPE;
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
@Target({TYPE})
public @interface SubmodelInfo {
	public String idShort() default "";
	public String idShortRef() default "";
}
