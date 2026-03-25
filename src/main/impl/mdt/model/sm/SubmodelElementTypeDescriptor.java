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
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementTypeDescriptor {
	private final String m_name;
	private final AasSubmodelElements m_type;
	private final Class<? extends SubmodelElement> m_elementClass;
	
	public SubmodelElementTypeDescriptor(String name, AasSubmodelElements type,
									Class<? extends SubmodelElement> elementClass) {
		m_name = name;
		m_type = type;
		m_elementClass = elementClass;
	}
	
	public String getName() {
		return m_name;
	}
	
	public AasSubmodelElements getType() {
		return m_type;
	}
	
	public Class<? extends SubmodelElement> getElementClass() {
		return m_elementClass;
	}
	
	/**
	 * SubmodelElement에 해당하는 타입 기술자를 반환한다.
	 *
	 * @param element	대상 {@link SubmodelElement} 객체.
	 * @return	{@link SubmodelElementTypeDescriptor} 객체.
	 */
	public static SubmodelElementTypeDescriptor getTypeDescriptor(SubmodelElement element) {
		return FStream.from(TYPE_DESCS)
						.findFirst(desc -> desc.getElementClass().isInstance(element))
						.getOrThrow(() -> new IllegalArgumentException("Unknown SubmodelElement type: "
																		+ element.getClass()));
	}
	
	/**
	 * SubmodelElement의 value type 문자열을 반환한다.
	 *
	 * @param element	대상 {@link SubmodelElement} 객체.
	 * @return	Value type 문자열
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
