package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.SUBMODEL_ELEMENT_LIST)
public @interface SMListField {
	public String idShort() default "";
	public AasSubmodelElements typeValueListElement() default AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION;
	public DataTypeDefXsd valueTypeListElement() default DataTypeDefXsd.STRING;
	public boolean orderRelevant() default false;
	public boolean keepNullField() default false;
	
	public Class<? > elementClass() default void.class;
}
