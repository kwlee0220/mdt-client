package mdt.model.sm.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface SMListField {
	public String idShort() default "";
	public AasSubmodelElements typeValueListElement() default AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION;
	public DataTypeDefXsd valueTypeListElement() default DataTypeDefXsd.STRING;
	public boolean orderRelevant() default false;
	public boolean keepNullField() default false;
	
	public Class<? > elementClass() default void.class;
}
