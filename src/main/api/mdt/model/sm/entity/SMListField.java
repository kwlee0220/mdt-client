package mdt.model.sm.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;


/**
 * 엔티티 필드를 AAS {@code SubmodelElementList}에 대응시키는 annotation.
 * <p>
 * 대상 필드의 값은 {@link SubmodelElementListEntity}이거나 원소들의 {@link java.util.List}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SubmodelElementEntityField(AasSubmodelElements.SUBMODEL_ELEMENT_LIST)
public @interface SMListField {
	/**
	 * 필드에 대응되는 SubmodelElementList의 idShort.
	 * <p>
	 * 엔티티 → AAS 변환 시 빈 문자열이면 필드 이름이 idShort로 사용된다.
	 *
	 * @return	idShort.
	 */
	public String idShort() default "";
	/**
	 * 생성될 SubmodelElementList의 {@code typeValueListElement}.
	 *
	 * @return	원소 SubmodelElement 종류.
	 */
	public AasSubmodelElements typeValueListElement() default AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION;
	/**
	 * 생성될 SubmodelElementList의 {@code valueTypeListElement}.
	 *
	 * @return	원소 Property의 value 타입.
	 */
	public DataTypeDefXsd valueTypeListElement() default DataTypeDefXsd.STRING;
	/**
	 * 원소 순서가 의미를 갖는지 여부.
	 * <p>
	 * 현재 필드 변환 로직에서는 사용되지 않는다.
	 *
	 * @return	순서가 의미를 가지면 {@code true}.
	 */
	public boolean orderRelevant() default false;
	/**
	 * 필드 값이 {@code null}인 경우에도 빈 SubmodelElementList를 생성할지 여부.
	 *
	 * @return	{@code null} 값 필드도 생성하면 {@code true}.
	 */
	public boolean keepNullField() default false;
	
	/**
	 * AAS → 필드 변환 시 원소별로 생성할 클래스.
	 * <p>
	 * 필드의 현재 값이 {@link java.util.List}이면 각 원소를 이 클래스의 {@link SubmodelElementEntity}로
	 * 변환한다. 그렇지 않은 경우, {@link SubmodelElementEntity} 구현 클래스이면 엔티티 리스트로,
	 * {@code String}/기본 타입(또는 래퍼)이면 원소 Property의 값 문자열 리스트로,
	 * {@code void.class}이면 원소 SubmodelElement 리스트를 그대로 필드 값으로 사용한다.
	 *
	 * @return	원소 클래스.
	 */
	public Class<? > elementClass() default void.class;
}
