package mdt.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.collect.Lists;

import utils.InternalException;
import utils.func.Try;
import utils.stream.FStream;

import mdt.ksx9101.AasCRUDActions;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AbstractMDTSubmodelElementContainer implements AasCRUDActions {
	protected String m_idShort;
	protected String m_idShortRef;
	protected String m_category;
	protected List<SubmodelElement> m_submodelElementList = null;

	protected AbstractMDTSubmodelElementContainer(String idShort, String idShortRef) {
		m_idShort = idShort;
		m_idShortRef = idShortRef;
	}
	protected AbstractMDTSubmodelElementContainer() {
		this(null, null);
	}
	
	public void setIdShort(String idShort) {
		m_idShort = idShort;
	}
	
	public void setIdShortRef(String ref) {
		m_idShortRef = ref;
	}
	
	public void setCategory(String category) {
		m_category = category;
	}
	
	protected String getIdShort() {
		if ( m_idShort != null && m_idShort.length() > 0 ) {
			return m_idShort;
		}
		if ( m_idShortRef == null || m_idShortRef.length() == 0 ) {
			return null;
		}
		if ( m_submodelElementList == null || m_submodelElementList.size() == 0 ) {
			throw new IllegalStateException("SubmodelElementList is empty");
		}
		
		try {
			return "" + PropertyUtils.getSimpleProperty(this, m_idShortRef);
		}
		catch ( Exception ignored ) {
			return null;
		}
	}
	
	protected void toFields(List<SubmodelElement> submodelElements) {
		Map<String,SubmodelElement> smeMap = FStream.from(submodelElements).toMap(sme -> sme.getIdShort());
		for ( Field field:  getClass().getDeclaredFields() ) {
			if ( !PropertyUtils.isWriteable(this, field.getName()) ) {
				String msg = String.format("Field[%s] is not writable", field.getName());
				throw new ModelGenerationException(msg);
			}
			
			PropertyField propAnno = field.getAnnotation(PropertyField.class);
			if ( propAnno != null ) {
				readPropertyField(field, propAnno, smeMap);
				continue;
			}
			
			SMCField smcAnno = field.getAnnotation(SMCField.class);
			if ( smcAnno != null ) {
				readSubmodelElementCollectionField(field, smcAnno, smeMap);
				continue;
			}
			
			SMLField smlAnno = field.getAnnotation(SMLField.class);
			if ( smlAnno != null ) {
				updateListField(field, smlAnno, smeMap);
				continue;
			}
		}
	}
	
	protected List<SubmodelElement> fromFields() {
		m_submodelElementList = Lists.newArrayList();
		for ( Field field:  getClass().getDeclaredFields() ) {
			try {
				field.setAccessible(true);
				
				Object fieldValue = field.get(this);
				SubmodelElement element = MDTSubmodelElementUtils.toSubmodelElement(field, fieldValue);
				if ( element != null ) {
					m_submodelElementList.add(element);
				}
			}
			catch ( Exception ignored ) {
				ignored.printStackTrace();
			}
		}
		
		return m_submodelElementList;
	}
	
	public void update(String idShortPath, Object value) {
		List<String> pathSegs = SubmodelUtils.parseIdShortPath(idShortPath).toList();
		Field field = (pathSegs.size() > 0) ? findFieldByIdShort(pathSegs.get(0)) : null;
		if ( field != null ) {
			try {
				field.setAccessible(true);
				Object fieldValue = field.get(this);
				if ( fieldValue instanceof AasCRUDActions op ) {
					String subIdShortPath = SubmodelUtils.buildIdShortPath(pathSegs.subList(1, pathSegs.size()));
					op.update(subIdShortPath, fieldValue);
				}
			}
			catch ( Exception expected ) { }
		}
	}
	
	protected Field findFieldByIdShort(String idShort) {
		for ( Field field:  getClass().getDeclaredFields() ) {
			PropertyField propAnno = field.getAnnotation(PropertyField.class);
			if ( propAnno != null && idShort.equals(propAnno.idShort()) ) {
				return field;
			}
			SMCField smcAnno = field.getAnnotation(SMCField.class);
			if ( smcAnno != null && idShort.equals(smcAnno.idShort()) ) {
				return field;
			}
			SMLField smlAnno = field.getAnnotation(SMLField.class);
			if ( smlAnno != null && idShort.equals(smlAnno.idShort()) ) {
				return field;
			}
			
			try {
				field.setAccessible(true);
				Object fieldValue = field.get(this);
				if ( fieldValue instanceof SubmodelElementCollectionEntity smcAdaptor
					&& idShort.equals(smcAdaptor.getIdShort()) ) {
					return field;
				}
				if ( fieldValue instanceof SubmodelElementListEntity<?, ?> smlHandle
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

	protected void readPropertyField(Field field, PropertyField anno, Map<String,SubmodelElement> elmMap) {
		SubmodelElement element = elmMap.get(anno.idShort());
		if ( element != null && element instanceof Property prop ) {
			DataType<?> dtype = DataTypes.fromDataTypeName(anno.valueType());
			Object value = dtype.parseValueString(prop.getValue());
			try {
				PropertyUtils.setSimpleProperty(this, field.getName(), value);
			}
			catch ( Exception ignored ) { }
		}
	}

	protected void readSubmodelElementCollectionField(Field field, SMCField anno,
													Map<String,SubmodelElement> elmMap) {
		SubmodelElement element = elmMap.get(anno.idShort());
		if ( element == null ) {
			return;
		}
		if ( !(element instanceof SubmodelElementCollection) ) {
			String msg = String.format("SubmodelElement(%s) is not SubmodelElementCollection", anno.idShort());
			throw new ModelGenerationException(msg);
		}
		
		Class<?> fieldType = anno.adaptorClass();
		if ( !SubmodelElementCollectionEntity.class.isAssignableFrom(fieldType) ) {
			fieldType = field.getType();
		}
		if ( !SubmodelElementCollectionEntity.class.isAssignableFrom(fieldType) ) {
			String msg = String.format("Field(%s.%s) is not SubmodelElementCollection adaptor",
										field.getDeclaringClass(), field.getName());
			throw new ModelGenerationException(msg);
		}
		
		SubmodelElementCollection smc;
		try {
			smc = (SubmodelElementCollection)element;
			SubmodelElementCollectionEntity adaptor
							= (SubmodelElementCollectionEntity)fieldType.getConstructor().newInstance();
			adaptor.fromAasModel(smc);
			PropertyUtils.setSimpleProperty(this, field.getName(), adaptor);
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

	protected void updateListField(Field field, SMLField anno, Map<String,SubmodelElement> elmMap) {
		SubmodelElement element = elmMap.get(anno.idShort());
		if ( element == null ) {
			return;
		}
		if ( !(element instanceof SubmodelElementList) ) {
			String msg = String.format("SubmodelElement(%s) is not SubmodelElementList", anno.idShort());
			throw new ModelGenerationException(msg);
		}
		
		SubmodelElementList sml = (SubmodelElementList)element;
		
		// Field 값이 존재하고, 그 값의 타입이 AbstractSubmodelElementListHandle인 경우에는
		// 해당 값의 'fromAasModel()' 메소드를 호출하여 field 값을 갱신한다.
		Try<Object> fieldValue = Try.get(() -> PropertyUtils.getSimpleProperty(this, field.getName()));
		if ( fieldValue.isSuccessful()
			&& fieldValue.getUnchecked() != null
			&& fieldValue.getUnchecked() instanceof SubmodelElementListEntity handle ) {
			handle.fromAasModel(element);
			return;
		}
		
		Class<?> fieldType = field.getType();
		if ( Collection.class.isAssignableFrom(fieldType) ) {
			Class<?> elmClass = anno.elementClass();
			if ( SubmodelElementEntity.class.isAssignableFrom(elmClass) ) {
				try {
					Constructor<?> elmCtor = elmClass.getDeclaredConstructor();
					List<SubmodelElementEntity> coll = FStream.from(sml.getValue())
															.mapOrThrow(sme -> {
																SubmodelElementEntity elm = (SubmodelElementEntity)elmCtor.newInstance();
																elm.fromAasModel(sme);
																return elm;
															})
															.toList();
					PropertyUtils.setSimpleProperty(this, field.getName(), coll);
				}
				catch ( Exception e ) {
					String msg = String.format("Failed to read a SubmodelElementList (%s) into the field %s, cause=%s",
												anno.idShort(), field.getName(), e);
					throw new ModelGenerationException(msg);
				}
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
		else {
			String msg = String.format("Field(%s.%s) is not SubmodelElementList adaptor",
										field.getDeclaringClass(), field.getName());
			throw new ModelGenerationException(msg);
		}
	}
}
