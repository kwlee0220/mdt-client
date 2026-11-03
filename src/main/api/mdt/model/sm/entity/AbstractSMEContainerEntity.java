package mdt.model.sm.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
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

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import utils.InternalException;
import utils.Preconditions;
import utils.ReflectionUtils;
import utils.Throwables;
import utils.stream.FStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.ModelGenerationException;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.MultiLanguagePropertyValue;


/**
 * Submodel 또는 SubmodelElement 컨테이너(SubmodelElementCollection/List)에 대응되는 엔티티의
 * 공통 기반 클래스.
 * <p>
 * idShort와 semanticId를 보관하며, 하위 클래스에 선언된 annotation({@link PropertyField},
 * {@link SMCollectionField}, {@link SMListField}, {@link MultiLanguagePropertyField},
 * {@link SMElementField}) 필드와 하위 SubmodelElement 사이의 리플렉션 기반 변환 기능을 제공한다.
 * 필드 값은 Apache Commons BeanUtils를 통해 Java Bean 접근자로 읽고 쓴다.
 *
 * @param <T>	대응되는 AAS 모델 객체의 타입.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractSMEContainerEntity<T> implements AasCRUDActions, AASModelEntity<T> {
	private String m_idShort;
	private Reference m_semanticId;
	
	/**
	 * 대응되는 AAS 모델 객체의 idShort를 반환한다.
	 *
	 * @return	idShort.
	 */
	public String getIdShort() {
		return m_idShort;
	}
	
	/**
	 * 대응되는 AAS 모델 객체의 idShort를 설정한다.
	 *
	 * @param idShort	idShort.
	 */
	public void setIdShort(String idShort) {
		m_idShort = idShort;
	}
	
	/**
	 * 대응되는 AAS 모델 객체의 semanticId를 반환한다.
	 *
	 * @return	semanticId 참조.
	 */
	public Reference getSemanticId() {
		return m_semanticId;
	}
	
	/**
	 * 대응되는 AAS 모델 객체의 semanticId를 설정한다.
	 *
	 * @param semanticId	semanticId 참조.
	 */
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
	
	/**
	 * annotation이 붙은 필드들의 값을 SubmodelElement로 변환하여 반환한다.
	 * <p>
	 * 값이 {@code null}이어서 생성되지 않은 요소는 결과에 포함되지 않는다. getter가 없는 필드는 건너뛴다.
	 *
	 * @return	필드들로부터 생성된 SubmodelElement 리스트.
	 * @throws ModelGenerationException	필드 값을 SubmodelElement로 변환하지 못한 경우.
	 */
	protected List<SubmodelElement> readSubmodelElementFromFields() {
		List<SubmodelElement> elements = Lists.newArrayList();
		
		for ( Field field: ReflectionUtils.getFieldList(getClass()) ) {
			if ( getSMEEntityField(field) == null || !PropertyUtils.isReadable(this, field.getName()) ) {
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
			catch ( ModelGenerationException e ) {
				throw e;
			}
			catch ( Exception e ) {
				throw new ModelGenerationException(String.format("Failed to generate SubmodelElement: field=%s, cause=%s",
																	field.getName(), Throwables.unwrapThrowable(e)));
			}
		}
		
		return elements;
	}
	
	/**
	 * AAS → 필드 변환 시 사용할 데이터 타입을 결정한다.
	 * <p>
	 * annotation의 {@code valueType}이 지정되어 있으면 이를 사용하고, 그렇지 않으면 Property의
	 * valueType(없으면 {@link DataTypes#STRING})을 사용한다.
	 *
	 * @param anno	필드의 {@link PropertyField} annotation.
	 * @param prop	읽어 들일 Property.
	 * @return	데이터 타입.
	 */
	public static DataType<?> getDataType(PropertyField anno, Property prop) {
		if ( anno.valueType().equals("") ) {
			DataTypeDefXsd vtype = prop.getValueType();
			return (vtype != null) ? DataTypes.fromAas4jDatatype(vtype) : DataTypes.STRING;
		}
		else {
			return DataTypes.fromDataTypeName(anno.valueType());
		}
	}
	
	/**
	 * 필드 → AAS 변환 시 사용할 데이터 타입을 결정한다.
	 * <p>
	 * annotation의 {@code valueType}이 지정되어 있으면 이를 사용하고, 그렇지 않으면 값의 Java 클래스로부터
	 * 데이터 타입을 결정한다.
	 *
	 * @param anno	필드의 {@link PropertyField} annotation.
	 * @param value	필드 값. {@code valueType}이 지정되지 않은 경우 {@code null}이 아니어야 한다.
	 * @return	데이터 타입.
	 */
	public static DataType<?> getDataType(PropertyField anno, Object value) {
		return ( anno.valueType().equals("") )
				? DataTypes.fromJavaClass(value.getClass())
				: DataTypes.fromDataTypeName(anno.valueType());
	}
	
	/**
	 * 주어진 SubmodelElement들을 idShort로 매칭하여 annotation이 붙은 필드들의 값을 갱신한다.
	 * <p>
	 * setter가 없는 필드나 annotation({@link SubmodelElementEntityField} 메타 annotation이 붙은 것)이
	 * 없는 필드는 무시된다. 필드 annotation의 {@code idShort}와 일치하는 요소가 없으면 해당 필드는
	 * 변경되지 않는다. {@link ReferenceField}가 붙은 필드는 처리하지 않는다.
	 *
	 * @param submodelElements	읽어 들일 SubmodelElement 리스트.
	 * @throws ModelGenerationException	대응되는 요소의 종류가 annotation과 맞지 않거나 변환에 실패한 경우.
	 */
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

	/**
	 * Property의 값을 파싱하여 주어진 필드에 설정한다.
	 * <p>
	 * Enum 타입 필드의 경우 값 문자열을 Enum 이름으로 해석하며, 빈 문자열은 {@code null}로 설정된다.
	 *
	 * @param field	대상 필드.
	 * @param anno	필드의 {@link PropertyField} annotation.
	 * @param prop	읽어 들일 Property.
	 * @throws ModelGenerationException	값 설정에 실패한 경우.
	 */
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
			throw new ModelGenerationException("Failed to read a Property into the field: "
												+ field.getName() + ", cause=" + e);
		}
	}

	/**
	 * MultiLanguageProperty의 값을 {@link MultiLanguagePropertyValue}로 변환하여 주어진 필드에 설정한다.
	 *
	 * @param field	대상 필드.
	 * @param anno	필드의 {@link MultiLanguagePropertyField} annotation.
	 * @param mlprop	읽어 들일 MultiLanguageProperty.
	 * @throws ModelGenerationException	값 설정에 실패한 경우.
	 */
	protected void updateMLPropertyField(Field field, MultiLanguagePropertyField anno, MultiLanguageProperty mlprop) {
		try {
			MultiLanguagePropertyValue mlpv = MultiLanguagePropertyValue.from(mlprop);
			PropertyUtils.setSimpleProperty(this, field.getName(), mlpv);
		}
		catch ( Exception e ) {
			throw new ModelGenerationException("Failed to read a Property into the field: "
												+ field.getName() + ", cause=" + e);
		}
	}

	/**
	 * SubmodelElementCollection으로부터 엔티티를 생성하여 주어진 필드에 설정한다.
	 * <p>
	 * 엔티티 클래스는 annotation의 {@code adaptorClass}(지정되지 않으면 필드 타입)이며,
	 * 기본 생성자로 생성한 후 {@link SubmodelElementEntity#updateFromAasModel(Object)}로 갱신한다.
	 *
	 * @param field	대상 필드.
	 * @param anno	필드의 {@link SMCollectionField} annotation.
	 * @param smc	읽어 들일 SubmodelElementCollection.
	 * @throws ModelGenerationException	{@code adaptorClass}가 필드 타입과 호환되지 않거나 변환에 실패한 경우.
	 */
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
			SubmodelElementEntity entity = (SubmodelElementEntity)ReflectionUtils.newInstance(entityType);
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

	/**
	 * SubmodelElementList로부터 주어진 필드의 값을 갱신한다.
	 * <p>
	 * 필드의 현재 값이 {@link SubmodelElementListEntity}이면 그 엔티티를 직접 갱신한다. 그 외의 경우는
	 * {@link SMListField#elementClass()}에 따라 원소 엔티티 리스트, 값 문자열 리스트 또는 원소
	 * SubmodelElement 리스트를 새로 만들어 필드에 설정한다.
	 *
	 * @param field	대상 필드.
	 * @param anno	필드의 {@link SMListField} annotation.
	 * @param sml	읽어 들일 SubmodelElementList.
	 * @throws ModelGenerationException	필드 값 읽기/설정에 실패한 경우.
	 * @throws InternalException	{@code elementClass}가 지원되지 않는 타입인 경우.
	 */
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

	/**
	 * 주어진 SubmodelElement 객체를 그대로 필드에 설정한다.
	 *
	 * @param field	대상 필드.
	 * @param anno	필드의 {@link SMElementField} annotation.
	 * @param sme	필드에 설정할 SubmodelElement.
	 * @throws ModelGenerationException	값 설정에 실패한 경우.
	 */
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
	
	/**
	 * SubmodelElement 리스트의 각 원소를 주어진 엔티티 클래스의 객체로 변환한다.
	 * <p>
	 * 각 원소마다 엔티티 클래스의 기본 생성자로 객체를 만들고
	 * {@link SubmodelElementEntity#updateFromAasModel(Object)}로 갱신한다.
	 *
	 * @param elmEntityClass	{@link SubmodelElementEntity} 구현 클래스.
	 * @param members	변환할 SubmodelElement 리스트.
	 * @return	변환된 엔티티 리스트.
	 * @throws InternalException	엔티티 생성 또는 갱신에 실패한 경우.
	 */
	public static List<SubmodelElementEntity>
	fromSubmodelElementList(Class<?> elmEntityClass, List<SubmodelElement> members) {
		return FStream.from(members)
						.map(elm -> {
							try {
								SubmodelElementEntity entity = (SubmodelElementEntity)ReflectionUtils.newInstance(elmEntityClass);
								entity.updateFromAasModel(elm);
								return entity;
							}
							catch ( Exception e ) {
								Throwable cause = e;
								if ( e instanceof InternalException ie ) {
									cause = Throwables.unwrapThrowable(ie);
								}
								String msg = String.format("Failed to create SubmodelElementEntity from SubmodelElement: "
															+ "element=%s, entityClass=%s, cause=%s",
															elm, elmEntityClass, cause);
								throw new InternalException(msg, cause);
							}
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
				
				if ( value == null ) {
					// keepNullField인 null 값: 값으로 타입을 정할 수 없으므로 annotation의 valueType을 쓰고,
					// 지정되지 않았으면 AAS → 필드 변환과 같이 xs:string으로 간주한다.
					dtype = anno.valueType().equals("") ? DataTypes.STRING
														: DataTypes.fromDataTypeName(anno.valueType());
					propStr = null;
				}
				// value의 타입이 Enum인 경우은 Enum의 name을 사용한다.
				else if ( value.getClass().isEnum() ) {
					dtype = DataTypes.STRING;
					propStr = ((Enum)value).name();
				}
				else {
					dtype = getDataType(anno, value);
					propStr = dtype.toValueString(value);
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
	 * 주어진 {@link Field}에 선언된 annotation 중에서 {@link SubmodelElementEntityField} 메타
	 * annotation이 붙은 첫 annotation을 검색한다.
	 *
	 * @param field	검색 대상 Field 객체.
	 * @return	검색된 필드 annotation 객체 (예: {@link PropertyField}).
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
