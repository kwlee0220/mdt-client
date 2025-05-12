package mdt.model.sm.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import utils.InternalException;
import utils.ReflectionUtils;
import utils.Utilities;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.ModelGenerationException;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.MultiLanguagePropertyValue;


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
	
//	public void update(String idShortPath, Object value) {
//		List<String> pathSegs = SubmodelUtils.parseIdShortPath(idShortPath).toList();
//		Field field = (pathSegs.size() > 0) ? findFieldByIdShort(pathSegs.get(0)) : null;
//		if ( field != null ) {
//			try {
//				field.setAccessible(true);
//				Object fieldValue = PropertyUtils.getProperty(this, field.getName()); 
//				if ( fieldValue instanceof AasCRUDActions op ) {
//					String subIdShortPath = SubmodelUtils.buildIdShortPath(pathSegs.subList(1, pathSegs.size()));
//					op.update(subIdShortPath, fieldValue);
//				}
//			}
//			catch ( Exception expected ) { }
//		}
//	}
	
	protected List<SubmodelElement> readSubmodelElementFromFields() {
		List<SubmodelElement> elements = Lists.newArrayList();
		
		for ( Field field: ReflectionUtils.getAllFieldsList(getClass()) ) {
			if ( getSMEEntityField(field) == null ) {
				continue;
			}
			
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
	
	protected void updateFields(List<SubmodelElement> submodelElements) {
		Map<String,SubmodelElement> smeMap = FStream.from(submodelElements)
													.tagKey(SubmodelElement::getIdShort)
													.toMap();
		for ( Field field:  FieldUtils.getAllFieldsList(getClass()) ) {
			if ( !PropertyUtils.isWriteable(this, field.getName()) ) {
				continue;
			}
			
			Annotation anno = getSMEEntityField(field);
			if ( anno == null ) {
				continue;
			}
			
			if ( anno instanceof PropertyField propAnno ) {
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
			}
			else if ( anno instanceof SMCollectionField smcAnno ) {
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
			}
			else if ( anno instanceof SMListField smlAnno ) {
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
			}
			else if ( anno instanceof MultiLanguagePropertyField mlpAnno ) {
				SubmodelElement element = smeMap.get(mlpAnno.idShort());
				if ( element != null ) {
					if ( element instanceof MultiLanguageProperty mlprop ) {
						updateMLPropertyField(field, mlpAnno, mlprop);
					}
					else {
						String msg = String.format("Field[%s] requires MultiLanguageProperty, but %s",
													field.getName(), element.getClass());
						throw new ModelGenerationException(msg);
					}
				}
			}
			else if ( anno instanceof SMElementField smeAnno ) {
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
			}
			
//			PropertyField propAnno = field.getAnnotation(PropertyField.class);
//			if ( propAnno != null ) {
//				SubmodelElement element = smeMap.get(propAnno.idShort());
//				if ( element != null ) {
//					if ( element instanceof Property prop ) {
//						updatePropertyField(field, propAnno, prop);
//					}
//					else {
//						String msg = String.format("Field[%s] requires Property, but %s",
//													field.getName(), element.getClass());
//						throw new ModelGenerationException(msg);
//					}
//				}
//				continue;
//			}
//			
//			SMCollectionField smcAnno = field.getAnnotation(SMCollectionField.class);
//			if ( smcAnno != null ) {
//				SubmodelElement element = smeMap.get(smcAnno.idShort());
//				if ( element != null ) {
//					if ( element instanceof SubmodelElementCollection smc ) {
//						updateSMCField(field, smcAnno, smc);
//					}
//					else {
//						String msg = String.format("Field[%s] requires SubmodelElementCollection, but %s",
//													field.getName(), element.getClass());
//						throw new ModelGenerationException(msg);
//					}
//				}
//				continue;
//			}
			
//			SMListField smlAnno = field.getAnnotation(SMListField.class);
//			if ( smlAnno != null ) {
//				SubmodelElement element = smeMap.get(smlAnno.idShort());
//				if ( element != null ) {
//					if ( element instanceof SubmodelElementList sml ) {
//						updateSMLField(field, smlAnno, sml);
//					}
//					else {
//						String msg = String.format("Field[%s] requires SubmodelElementList, but %s",
//													smlAnno.idShort(), element.getClass());
//						throw new ModelGenerationException(msg);
//					}
//				}
//				continue;
//			}
			
//			MultiLanguagePropertyField mlpAnno = field.getAnnotation(MultiLanguagePropertyField.class);
//			if ( mlpAnno != null ) {
//				SubmodelElement element = smeMap.get(mlpAnno.idShort());
//				if ( element != null ) {
//					if ( element instanceof MultiLanguageProperty mlprop ) {
//						updateMLPropertyField(field, mlpAnno, mlprop);
//					}
//					else {
//						String msg = String.format("Field[%s] requires MultiLanguageProperty, but %s",
//													field.getName(), element.getClass());
//						throw new ModelGenerationException(msg);
//					}
//				}
//				continue;
//			}
			
//			SMElementField smeAnno = field.getAnnotation(SMElementField.class);
//			if ( smeAnno != null ) {
//				SubmodelElement element = smeMap.get(smeAnno.idShort());
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void updatePropertyField(Field field, PropertyField anno, Property prop) {
		DataType<?> dtype = getDataType(anno, prop);
		Object value = dtype.parseValueString(prop.getValue());
		try {
			//  Enum 타입의 경우에는 Enum의 name을 사용한다.
			if ( field.getType().isEnum() ) {
				if ( value == null || ((String)value).length() == 0 ) {
					value = null;
				}
				else {
					value = Enum.valueOf((Class<? extends Enum>)field.getType(), (String)value);
				}
			}
			PropertyUtils.setSimpleProperty(this, field.getName(), value);
		}
		catch ( Exception e ) {
			throw new ModelGenerationException("Failed to read a Property into the field: " + field.getName());
		}
	}

	protected void updateMLPropertyField(Field field, MultiLanguagePropertyField anno, MultiLanguageProperty mlprop) {
		try {
			MultiLanguagePropertyValue mlpv = ElementValues.getMLPValue(mlprop);
			PropertyUtils.setSimpleProperty(this, field.getName(), mlpv);
		}
		catch ( Exception e ) {
			throw new ModelGenerationException("Failed to read a Property into the field: " + field.getName());
		}
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
			SubmodelElementEntity entity = (SubmodelElementEntity)Utilities.newInstance(entityType);
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
		
		MultiLanguagePropertyField mlpAnno = field.getAnnotation(MultiLanguagePropertyField.class);
		if ( mlpAnno != null ) {
			return toMLProperty(mlpAnno, fieldValue);
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
				DataType dtype;
				String propStr;
				
				// value의 타입이 Enum인 경우은 Enum의 name을 사용한다.
				if ( value.getClass().isEnum() ) {
					dtype = DataTypes.STRING;
					propStr = ((Enum)value).name();
				}
				else {
					dtype = getDataType(anno, value);
					propStr = FOption.map(value, dtype::toValueString);
				}

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
			throw new ModelGenerationException("Failed to generate Property: " + anno.idShort() + ", value=" + value);
		}
	}
	
	private static MultiLanguageProperty toMLProperty(MultiLanguagePropertyField anno, Object value) {
		Preconditions.checkArgument(value == null || value instanceof MultiLanguagePropertyValue,
									"@MultiLanguagePropertyField should be associated to MultiLanguagePropertyValue");
		if ( value == null ) {
			if ( anno.keepNullField() ) {
				return new DefaultMultiLanguageProperty.Builder().idShort(anno.idShort()).build();
			}
			else {
				return null;
			}
		}
		else if ( value instanceof MultiLanguagePropertyValue mlpv ) {
			DefaultMultiLanguageProperty mlp = new DefaultMultiLanguageProperty.Builder().build();
			mlp.setIdShort(anno.idShort());
			ElementValues.update(mlp, mlpv);
			return mlp;
		}
		else {
			throw new ModelGenerationException("Failed to generate MultiLanguageProperty: " + anno.idShort()
												+ ", value=" + value);
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
		if ( output != null ) {
			output.setTypeValueListElement(smlAnno.typeValueListElement());
			output.setValueTypeListElement(smlAnno.valueTypeListElement());
		}
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
	
	/**
	 * 주어진 {@link Field}에서 SubmodelElementEntityField annotation을 검색한다.
	 *
	 * @param field	검색 대상 Field 객체.
	 * @return	검색된 {@link SubmodelElementEntityField} annotation 객체.
	 * 			만일 검색되지 않은 경우에는 {@code null}.
	 */
	private static Annotation getSMEEntityField(Field field) {
		for ( Annotation anno : field.getDeclaredAnnotations() ) {
			Class<? extends Annotation> type = anno.annotationType();
			if ( type.isAnnotationPresent(SubmodelElementEntityField.class) ) {
				return anno;
			}
		}

		return null;
	}
}
