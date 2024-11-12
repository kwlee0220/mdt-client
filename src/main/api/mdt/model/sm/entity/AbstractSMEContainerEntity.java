package mdt.model.sm.entity;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import utils.InternalException;
import utils.Utilities;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.ModelGenerationException;
import mdt.model.sm.SubmodelUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractSMEContainerEntity<T> implements AasCRUDActions, AASModelEntity<T> {
	private String m_idShort;
	private Reference m_semanticId;
	
	public String getIdShort() {
		return m_idShort;
	}
	
	public void setIdShort(String idShort) {
		m_idShort = idShort;
	}
	
	public Reference getSemanticId() {
		return m_semanticId;
	}
	
	public void setSemanticId(Reference semanticId) {
		m_semanticId = semanticId;
	}
	
	public void update(String idShortPath, Object value) {
		List<String> pathSegs = SubmodelUtils.parseIdShortPath(idShortPath).toList();
		Field field = (pathSegs.size() > 0) ? findFieldByIdShort(pathSegs.get(0)) : null;
		if ( field != null ) {
			try {
				field.setAccessible(true);
				Object fieldValue = PropertyUtils.getProperty(this, field.getName()); 
				if ( fieldValue instanceof AasCRUDActions op ) {
					String subIdShortPath = SubmodelUtils.buildIdShortPath(pathSegs.subList(1, pathSegs.size()));
					op.update(subIdShortPath, fieldValue);
				}
			}
			catch ( Exception expected ) { }
		}
	}
	
	protected void updateFields(List<SubmodelElement> submodelElements) {
		Map<String,SubmodelElement> smeMap = FStream.from(submodelElements).toMap(sme -> sme.getIdShort());
		for ( Field field:  getClass().getDeclaredFields() ) {
			if ( !PropertyUtils.isWriteable(this, field.getName()) ) {
				continue;
			}
			
			PropertyField propAnno = field.getAnnotation(PropertyField.class);
			if ( propAnno != null ) {
				SubmodelElement element = smeMap.get(propAnno.idShort());
				if ( element != null ) {
					if ( element instanceof Property prop ) {
						updatePropertyField(field, propAnno, prop);
					}
					else {
						String msg = String.format("Field[%s] requires Property, but %s",
													field.getName(), element.getClass());
						throw new ModelGenerationException(msg);
					}
				}
				continue;
			}
			
			SMCollectionField smcAnno = field.getAnnotation(SMCollectionField.class);
			if ( smcAnno != null ) {
				SubmodelElement element = smeMap.get(smcAnno.idShort());
				if ( element != null ) {
					if ( element instanceof SubmodelElementCollection smc ) {
						updateSMCField(field, smcAnno, smc);
					}
					else {
						String msg = String.format("Field[%s] requires SubmodelElementCollection, but %s",
													field.getName(), element.getClass());
						throw new ModelGenerationException(msg);
					}
				}
				continue;
			}
			
			SMListField smlAnno = field.getAnnotation(SMListField.class);
			if ( smlAnno != null ) {
				SubmodelElement element = smeMap.get(smlAnno.idShort());
				if ( element != null ) {
					if ( element instanceof SubmodelElementList sml ) {
						updateSMLField(field, smlAnno, sml);
					}
					else {
						String msg = String.format("Field[%s] requires SubmodelElementList, but %s",
													smlAnno.idShort(), element.getClass());
						throw new ModelGenerationException(msg);
					}
				}
				continue;
			}
			
			SMElementField smeAnno = field.getAnnotation(SMElementField.class);
			if ( smeAnno != null ) {
				SubmodelElement element = smeMap.get(smeAnno.idShort());
				if ( element != null ) {
					if ( element instanceof SubmodelElement ) {
						updateSMEField(field, smeAnno, (SubmodelElement)element);
					}
					else {
						String msg = String.format("Field[%s] requires SubmodelElement, but %s",
													smeAnno.idShort(), element.getClass());
						throw new ModelGenerationException(msg);
					}
				}
				continue;
			}
			
//			ReferenceField refAnno = field.getAnnotation(ReferenceField.class);
//			if ( refAnno != null ) {
//				SubmodelElement element = smeMap.get(refAnno.idShort());
//				if ( element != null ) {
//					if ( element instanceof SubmodelElement ) {
//						updateSMEField(field, smeAnno, (SubmodelElement)element);
//					}
//					else {
//						String msg = String.format("Field[%s] requires SubmodelElement, but %s",
//													smeAnno.idShort(), element.getClass());
//						throw new ModelGenerationException(msg);
//					}
//				}
//				continue;
//			}
		}
	}
	
	protected List<SubmodelElement> readSubmodelElementFromFields() {
		List<SubmodelElement> elements = Lists.newArrayList();
		
		for ( Field field:  getClass().getDeclaredFields() ) {
			try {
				field.setAccessible(true);
				
				Object fieldValue = PropertyUtils.getProperty(this, field.getName()); 
				SubmodelElement element = toSubmodelElement(field, fieldValue);
				if ( element != null ) {
					elements.add(element);
				}
			}
			catch ( Exception ignored ) {
				ignored.printStackTrace();
			}
		}
		
		return elements;
	}
	
	protected Field findFieldByIdShort(String idShort) {
		for ( Field field:  getClass().getDeclaredFields() ) {
			PropertyField propAnno = field.getAnnotation(PropertyField.class);
			if ( propAnno != null && idShort.equals(propAnno.idShort()) ) {
				return field;
			}
			SMCollectionField smcAnno = field.getAnnotation(SMCollectionField.class);
			if ( smcAnno != null && idShort.equals(smcAnno.idShort()) ) {
				return field;
			}
			SMListField smlAnno = field.getAnnotation(SMListField.class);
			if ( smlAnno != null && idShort.equals(smlAnno.idShort()) ) {
				return field;
			}
			
			try {
				field.setAccessible(true);
				Object fieldValue = PropertyUtils.getProperty(this, field.getName()); 
				if ( fieldValue instanceof SubmodelElementCollectionEntity smcAdaptor
					&& idShort.equals(smcAdaptor.getIdShort()) ) {
					return field;
				}
				if ( fieldValue instanceof SubmodelElementListEntity smlHandle
						&& idShort.equals(smlHandle.getIdShort()) ) {
					return field;
				}
			}
			catch ( Exception expected ) { }
			
			if ( idShort.equals(field.getName()) ) {
				return field;
			}
		}
		
		return null;
	}
	
	public static DataType<?> getDataType(PropertyField anno, Property prop) {
		if ( anno.valueType().equals("") ) {
			return FOption.mapOrElse(prop.getValueType(), DataTypes::fromAas4jDatatype, DataTypes.STRING);
		}
		else {
			return DataTypes.fromDataTypeName(anno.valueType());
		}
	}
	
	public static DataType<?> getDataType(PropertyField anno, Object value) {
		return ( anno.valueType().equals("") )
				? DataTypes.fromJavaClass(value.getClass())
				: DataTypes.fromDataTypeName(anno.valueType());
	}

	protected void updatePropertyField(Field field, PropertyField anno, Property prop) {
		DataType<?> dtype = getDataType(anno, prop);
		Object value = dtype.parseValueString(prop.getValue());
		try {
			PropertyUtils.setSimpleProperty(this, field.getName(), value);
		}
		catch ( Exception ignored ) { }
	}

	protected void updateSMCField(Field field, SMCollectionField anno, SubmodelElementCollection smc) {
		Class<?> entityType = anno.adaptorClass();
		
		// SMCollectionField.adaptorClass가 별도로 지정되지 않은 경우에는
		// Field의 타입을 사용한다.
		if ( entityType.equals(void.class) ) {
			entityType = field.getType();
		}
		else if ( !field.getType().isAssignableFrom(entityType) ) {
			String msg = String.format("Field[%s] is not compatable to the adaptor class: field(%s) <-> adaptor(%s)",
										field.getName(), field.getType(), entityType);
			throw new ModelGenerationException(msg);
		}
		
		try {
			SubmodelElementCollectionEntity entity = (SubmodelElementCollectionEntity)Utilities.newInstance(entityType);
			entity.updateFromAasModel(smc);
			PropertyUtils.setSimpleProperty(this, field.getName(), entity);
		}
		catch ( ModelGenerationException e ) {
			throw e;
		}
		catch ( Exception e ) {
			String msg = String.format("Failed to read a SubmodelElementCollection (%s) into the field %s, cause=%s",
										anno.idShort(), field.getName(), e);
			throw new ModelGenerationException(msg);
		}
	}

	protected void updateSMLField(Field field, SMListField anno, SubmodelElementList sml) {
		Object fieldValue;
		try {
			fieldValue = PropertyUtils.getSimpleProperty(this, field.getName());
		}
		catch ( Throwable e ) {
			throw new ModelGenerationException("Failed to read field value: name="
												+ field.getName() + ", cause=" + e);
		}
		
		// Field 값이 존재하고, 그 값의 타입이 SubmodelElementListEntity인 경우에는
		// 해당 값의 'fromAasModel()' 메소드를 호출하여 field 값을 갱신한다.
		if ( fieldValue instanceof SubmodelElementListEntity entity ) {
			entity.updateFromAasModel(sml);
			return;
		}

		Class<?> elmClass = anno.elementClass();
		
		// Field의 타입이 collection인 경우에는 입력 sml에서 구한 SubmodelElement 리스트로 갱신한다.
		if ( fieldValue instanceof List ) {
			List<SubmodelElementEntity> newValue = fromSubmodelElementList(elmClass, sml.getValue());
			try {
				PropertyUtils.setSimpleProperty(this, field.getName(), newValue);
			}
			catch ( Throwable e ) {
				String msg = String.format("Failed to read a SubmodelElementList (%s) into the field %s, cause=%s",
											anno.idShort(), field.getName(), e);
				throw new ModelGenerationException(msg);
			}
		}
		else if ( SubmodelElementEntity.class.isAssignableFrom(elmClass) ) {
			List<SubmodelElementEntity> newValue = fromSubmodelElementList(elmClass, sml.getValue());
			setField(anno.idShort(), field.getName(), newValue);
		}
		else if ( elmClass.equals(String.class)
				|| elmClass.isPrimitive() || Primitives.isWrapperType(elmClass) ) {
			List<SubmodelElementEntity> newValue = fromSubmodelElementList(PropertyEntity.class, sml.getValue());
			List<String> primvValues = FStream.from(newValue).map(ent -> ((PropertyEntity)ent).getValue()).toList();
			setField(anno.idShort(), field.getName(), primvValues);
		}
		else if ( elmClass.equals(void.class) ) {
			try {
				PropertyUtils.setSimpleProperty(this, field.getName(), sml.getValue());
			}
			catch ( Exception e ) {
				String msg = String.format("Failed to read a SubmodelElementList (%s) into the field %s, cause=%s",
											anno.idShort(), field.getName(), e);
				throw new ModelGenerationException(msg);
			}
		}
		else {
			throw new InternalException("Failed to read SubmodelElementList: "
										+ "element-class is not a MDTSubmodelElement, " + elmClass);
		}
	}
	
	private void setField(String idShort, String name, Object value) {
		try {
			PropertyUtils.setSimpleProperty(this, name, value);
		}
		catch ( Throwable e ) {
			String msg = String.format("Failed to read a SubmodelElementList (%s) into the field %s, cause=%s",
									idShort, name, e);
			throw new ModelGenerationException(msg);
		}
	}

	protected void updateSMEField(Field field, SMElementField anno, SubmodelElement sme) {
		try {
			PropertyUtils.setSimpleProperty(this, field.getName(), sme);
		}
		catch ( Exception e ) {
			String msg = String.format("Failed to read a SubmodelElement (%s) into the field %s, cause=%s",
										anno.idShort(), field.getName(), e);
			throw new ModelGenerationException(msg);
		}
	}
	
	public static List<SubmodelElementEntity>
	fromSubmodelElementList(Class<?> elmEntityClass, List<SubmodelElement> members) {
		return FStream.from(members)
						.map(elm -> {
							SubmodelElementEntity entity = (SubmodelElementEntity)Utilities.newInstance(elmEntityClass);
							entity.updateFromAasModel(elm);
							return entity;
						})
						.toList();
	}
	
	//
	//	Field -> SubmodelElement
	//
	
	private static SubmodelElement toSubmodelElement(Field field, Object fieldValue) {
		// 대상 field에 '@PropertyField' annotation이 붙은 경우,
		// field 값을 Property로 변환시킨다.
		PropertyField propAnno = field.getAnnotation(PropertyField.class);
		if ( propAnno != null ) {
			return toProperty(propAnno, fieldValue);
		}
		
		SMCollectionField smcAnno = field.getAnnotation(SMCollectionField.class);
		if ( smcAnno != null ) {
			return toSubmodelElementCollection(smcAnno, field.getName(), fieldValue);
		}
		
		SMListField smlAnno = field.getAnnotation(SMListField.class);
		if ( smlAnno != null ) {
			return toSubmodelElementList(smlAnno, field.getName(), fieldValue);
		}
		
		SMElementField smeAnno = field.getAnnotation(SMElementField.class);
		if ( smeAnno != null ) {
			if ( fieldValue instanceof SubmodelElement sme ) {
				return sme;
			}
			else {
				throw new ModelGenerationException("@SMElementField requires SubmodelElement field, but=" + fieldValue);
			}
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
	private static Property toProperty(PropertyField anno, Object value) {
		try {
			if ( value != null || anno.keepNullField() ) {
				DataType dtype = getDataType(anno, value);
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
	
	private static SubmodelElementCollection toSubmodelElementCollection(SMCollectionField smcAnno, String fieldName,
																	Object fieldValue) {
		SubmodelElementCollection output;
		if ( fieldValue != null ) {
			if ( fieldValue instanceof SubmodelElementEntity smeEntity ) {
				SubmodelElement fieldSme = smeEntity.newSubmodelElement();
				if ( fieldSme instanceof SubmodelElementCollection smc ) {
					output = smc;
				}
				else {
					String msg = String.format("@SMCField should generate a SubmodelElementCollection: field="
												+ fieldName);
					throw new ModelGenerationException(msg);
				}
			}
			else {
				String msg = String.format("@SMCField should be associated to SubmodelElementEntity: field="
											+ fieldName);
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

		String idShort = (smcAnno.idShort().length() > 0) ? smcAnno.idShort() : fieldName;
		if ( idShort != null && output != null ) {
			output.setIdShort(idShort);
		}
		
		return output;
	}
	
	private static SubmodelElementList toSubmodelElementList(SMListField smlAnno, String fieldName, Object fieldValue) {
		SubmodelElementList output;
		if ( fieldValue != null ) {
			if ( fieldValue instanceof SubmodelElementEntity entity ) {
				SubmodelElement sme = entity.newSubmodelElement();
				if ( sme instanceof SubmodelElementList sml ) {
					return sml;
				}
				else {
					String msg = String.format("Field handle should generate a SubmodelElementList: "
												+ "field=" + fieldName);
					throw new ModelGenerationException(msg);
				}
			}
			else if ( fieldValue instanceof Iterable iter ) {
				output = toSubmodelElementList(iter);
			}
			else {
				String msg = String.format("@SMLField's value should be associated to Iterable: field=" + fieldName);
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

		String idShort = (smlAnno.idShort().length() > 0) ? smlAnno.idShort() : fieldName;
		if ( idShort != null && output != null ) {
			output.setIdShort(idShort);
		}
		output.setTypeValueListElement(smlAnno.typeValueListElement());
		output.setValueTypeListElement(smlAnno.valueTypeListElement());
		
		return output;
	}
	
	private static DefaultSubmodelElementList toSubmodelElementList(Iterable<?> iterable) {
		List<SubmodelElement> smeMembers = Lists.newArrayList();
		for ( Object member: iterable ) {
			if ( member instanceof SubmodelElementEntity smeEntity) {
				smeMembers.add((SubmodelElement)smeEntity.newSubmodelElement());
			}
			else if ( member instanceof SubmodelElement sme ) {
				smeMembers.add((SubmodelElement)sme);
			}
			else if ( member instanceof String
					|| member.getClass().isPrimitive()
					|| Primitives.isWrapperType(member.getClass()) ) {
				DataType<?> dtype = DataTypes.fromJavaClass(member.getClass());
				Property prop = mdt.model.sm.PropertyUtils.newProperty(null, dtype.getTypeDefXsd(), ""+member);
				smeMembers.add(prop);
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
}
