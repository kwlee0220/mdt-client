package mdt.model.sm;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.aas.DataTypes;


/**
 * AAS SubmodelElement 종류별 타입 기술자.
 * <p>
 * 각 기술자는 타입 이름(예: {@code "Property"}), {@link AasSubmodelElements} 값,
 * 대응되는 aas4j 인터페이스 클래스를 갖는다. 미리 등록된 기술자 목록에서
 * {@link #getTypeDescriptor(SubmodelElement)}로 SubmodelElement의 기술자를 찾을 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementTypeDescriptor {
	private final String m_name;
	private final AasSubmodelElements m_type;
	private final Class<? extends SubmodelElement> m_elementClass;
	
	/**
	 * 타입 기술자를 생성한다.
	 *
	 * @param name	타입 이름.
	 * @param type	{@link AasSubmodelElements} 타입 값.
	 * @param elementClass	타입에 대응되는 aas4j SubmodelElement 인터페이스 클래스.
	 */
	public SubmodelElementTypeDescriptor(String name, AasSubmodelElements type,
									Class<? extends SubmodelElement> elementClass) {
		m_name = name;
		m_type = type;
		m_elementClass = elementClass;
	}
	
	/**
	 * 타입 이름을 반환한다.
	 *
	 * @return	타입 이름 (예: {@code "Property"}, {@code "SubmodelElementCollection"}).
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * {@link AasSubmodelElements} 타입 값을 반환한다.
	 *
	 * @return	타입 값.
	 */
	public AasSubmodelElements getType() {
		return m_type;
	}
	
	/**
	 * 타입에 대응되는 aas4j SubmodelElement 인터페이스 클래스를 반환한다.
	 *
	 * @return	SubmodelElement 클래스.
	 */
	public Class<? extends SubmodelElement> getElementClass() {
		return m_elementClass;
	}
	
	/**
	 * SubmodelElement에 해당하는 타입 기술자를 반환한다.
	 * <p>
	 * 등록된 기술자 목록을 순서대로 검사하여 {@code element}가 인스턴스인 첫 기술자를 반환한다.
	 *
	 * @param element	대상 {@link SubmodelElement} 객체.
	 * @return	{@link SubmodelElementTypeDescriptor} 객체.
	 * @throws IllegalArgumentException	등록되지 않은 종류의 SubmodelElement인 경우.
	 */
	public static SubmodelElementTypeDescriptor getTypeDescriptor(SubmodelElement element) {
		return FStream.from(TYPE_DESCS)
						.findFirst(desc -> desc.getElementClass().isInstance(element))
						.getOrThrow(() -> new IllegalArgumentException("Unknown SubmodelElement type: "
																		+ element.getClass()));
	}
	
	/**
	 * SubmodelElement의 value type 문자열을 반환한다.
	 * <p>
	 * {@link Property}인 경우는 value 타입에 해당하는 {@link DataTypes} 데이터 타입의 식별자를,
	 * 그 외의 경우는 SubmodelElement 타입 이름(예: {@code "File"})을 반환한다.
	 *
	 * @param element	대상 {@link SubmodelElement} 객체.
	 * @return	Value type 문자열.
	 * @throws IllegalArgumentException	등록되지 않은 종류의 SubmodelElement인 경우.
	 */
	public static String getValueTypeString(SubmodelElement element) {
		if ( element instanceof Property prop ) {
			return DataTypes.fromAas4jDatatype(prop.getValueType()).getId();
		}
		else {
			SubmodelElementTypeDescriptor desc = getTypeDescriptor(element);
			return desc.m_name;
		}
	}
	
	private static final List<SubmodelElementTypeDescriptor> TYPE_DESCS = Lists.newArrayList();
	static {
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("Property", AasSubmodelElements.PROPERTY, Property.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("SubmodelElementCollection",
													AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION,
													SubmodelElementCollection.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("SubmodelElementList",
													AasSubmodelElements.SUBMODEL_ELEMENT_LIST,
													SubmodelElementList.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("File", AasSubmodelElements.FILE, File.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("MultiLanguageProperty",
													AasSubmodelElements.MULTI_LANGUAGE_PROPERTY,
													MultiLanguageProperty.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("Range", AasSubmodelElements.RANGE, Range.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("Operation",
													AasSubmodelElements.OPERATION,
													org.eclipse.digitaltwin.aas4j.v3.model.Operation.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("AnnotatedRelationshipElement",
													AasSubmodelElements.ANNOTATED_RELATIONSHIP_ELEMENT,
													AnnotatedRelationshipElement.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("Blob", AasSubmodelElements.BLOB, Blob.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("Capability", AasSubmodelElements.CAPABILITY, Capability.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("Entity", AasSubmodelElements.ENTITY, Entity.class));
		TYPE_DESCS.add(new SubmodelElementTypeDescriptor("RelationshipElement",
													AasSubmodelElements.RELATIONSHIP_ELEMENT,
													RelationshipElement.class));
	};
}
