package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

/**
 * 엔티티 필드 annotation({@link PropertyField}, {@link SMCollectionField} 등)에 붙이는
 * 메타 annotation.
 * <p>
 * {@link AbstractSMEContainerEntity}는 이 메타 annotation이 붙은 annotation을 가진 필드만을
 * SubmodelElement와 대응되는 필드로 간주한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubmodelElementEntityField {
	/**
	 * 대상 필드가 대응되는 SubmodelElement 종류.
	 *
	 * @return	SubmodelElement 종류.
	 */
	public AasSubmodelElements value();
}
