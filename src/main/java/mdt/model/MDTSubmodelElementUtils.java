package mdt.model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.google.common.collect.Lists;

import utils.func.FOption;

import lombok.experimental.UtilityClass;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class MDTSubmodelElementUtils {
	public static SubmodelElement toSubmodelElement(Field field, Object fieldValue) {
		// 대상 field에 '@PropertyField' annotation이 붙은 경우,
		// field 값을 Property로 변환시킨다.
		PropertyField propAnno = field.getAnnotation(PropertyField.class);
		if ( propAnno != null ) {
			return toProperty(propAnno, fieldValue);
		}
		
		SMCField smcAnno = field.getAnnotation(SMCField.class);
		if ( smcAnno != null ) {
			return toSubmodelElementCollection(smcAnno, field, fieldValue);
		}
		
		SMLField smlAnno = field.getAnnotation(SMLField.class);
		if ( smlAnno != null ) {
			return toSubmodelElementList(smlAnno, field, fieldValue);
		}
		
		if ( fieldValue instanceof Iterable iter ) {
			return toSubmodelElementList(iter);
		}
		if ( fieldValue instanceof SubmodelElement ) {
			return (SubmodelElement)fieldValue;
		}
		
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Property toProperty(PropertyField anno, Object value) {
		try {
			if ( value != null || anno.keepNullField() ) {
				DataType dtype = DataTypes.fromDataTypeName(anno.valueType());
				String propStr = FOption.map(value, dtype::toValueString); 

				String idShort = (anno.idShort().length() > 0) ? anno.idShort() : null;
				return new DefaultProperty.Builder()
												.idShort(idShort)
												.value(propStr)
												.valueType(dtype.getTypeDefXsd())
												.build();
			}
			else {
				return null;
			}
		}
		catch ( Exception ignored ) {
			return null;
		}
	}
	
	static SubmodelElementCollection toSubmodelElementCollection(SMCField smcAnno, Field field,
																	Object fieldValue) {
		SubmodelElementCollection output;
		if ( fieldValue != null ) {
			if ( fieldValue instanceof SubmodelElementEntity smcAdaptor ) {
				SubmodelElement fieldSme = smcAdaptor.toAasModel();
				if ( fieldSme instanceof SubmodelElementCollection smc ) {
					output = smc;
				}
				else {
					String msg = String.format("@SMCField should generate a SubmodelElementCollection: field="
												+ field.getName());
					throw new ModelGenerationException(msg);
				}
			}
			else {
				String msg = String.format("@SMCField should be associated to MDTSubmodelElement: field="
											+ field.getName());
				throw new ModelGenerationException(msg);
			}
		}
		else if ( smcAnno.keepNullField() ) {
			output = new DefaultSubmodelElementCollection.Builder()
							.value(Collections.emptyList())
							.build();
		}
		else {
			output = null;
		}

		String idShort = (smcAnno.idShort().length() > 0) ? smcAnno.idShort() : field.getName();
		if ( idShort != null && output != null ) {
			output.setIdShort(idShort);
		}
		
		return output;
	}
	
	static SubmodelElementList toSubmodelElementList(SMLField smlAnno, Field field, Object fieldValue) {
		SubmodelElementList output;
		if ( fieldValue != null ) {
			if ( fieldValue instanceof SubmodelElementEntity handle ) {
				SubmodelElement sme = handle.toAasModel();
				if ( sme instanceof SubmodelElementList sml ) {
					return sml;
				}
				else {
					String msg = String.format("Field handle should generate a SubmodelElementList: "
												+ "field=" + field.getName());
					throw new ModelGenerationException(msg);
				}
			}
			else if ( fieldValue instanceof Iterable iter ) {
				output = toSubmodelElementList(iter);
			}
			else {
				String msg = String.format("@SMLField's value should be associated to Iterable: field="
											+ field.getName());
				throw new ModelGenerationException(msg);
			}
		}
		else if ( smlAnno.keepNullField() ) {
			output = new DefaultSubmodelElementList.Builder()
								.value(Collections.emptyList())
								.build();
		}
		else {
			output = null;
		}

		String idShort = (smlAnno.idShort().length() > 0) ? smlAnno.idShort() : field.getName();
		if ( idShort != null && output != null ) {
			output.setIdShort(idShort);
		}
		
		return output;
	}
	
	static DefaultSubmodelElementList toSubmodelElementList(Iterable<?> iterable) {
		List<SubmodelElement> smeMembers = Lists.newArrayList();
		for ( Object member: iterable ) {
			if ( member instanceof SubmodelElementEntity adaptor) {
				smeMembers.add((SubmodelElement)adaptor.toAasModel());
			}
			else if ( member instanceof SubmodelElement sme ) {
				smeMembers.add((SubmodelElement)sme);
			}
			else {
				String msg = String.format("member is not MDTSubmodelElement");
				throw new ModelGenerationException(msg);
			}
		}

		return new DefaultSubmodelElementList.Builder()
							.typeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT)
							.value(smeMembers)
							.build();
	}
	static SubmodelElementList toSubmodelElementList(SubmodelElementEntity adaptor) {
		SubmodelElement sme = adaptor.toAasModel();
		if ( sme instanceof SubmodelElementList sml ) {
			return sml;
		}
		else {
			String msg = String.format("MDTSubmodelElement should generate a SubmodelElementLis");
			throw new ModelGenerationException(msg);
		}
	}
}
