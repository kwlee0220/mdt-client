package mdt.model.sm.value;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdSubmodelReference;

/**
 * AAS {@link Reference}를 판별하고 MDT 참조 객체로 변환하는 정적 유틸리티.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class References {
	private References() {
		throw new AssertionError("Should not be called: class=" + References.class.getName());
	}
	
	private static class KeySpec {
		private final KeyTypes m_type;
		private final boolean m_isSubmodelElement;
		private final boolean m_isTerminal;
		
		public KeySpec(KeyTypes type, boolean isSubmodelElement, boolean isTerminal) {
			m_type = type;
			m_isSubmodelElement = isSubmodelElement;
			m_isTerminal = isTerminal;
		}
	}
	private static final Map<KeyTypes, KeySpec> KEY_TYPE_SPECS = Maps.newHashMap();
	static {
		List<KeySpec> specs = List.of(
			new KeySpec(KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT, true, true),
			new KeySpec(KeyTypes.BASIC_EVENT_ELEMENT, true, true),
			new KeySpec(KeyTypes.BLOB, true, true),
			new KeySpec(KeyTypes.CAPABILITY, true, true),
			new KeySpec(KeyTypes.DATA_ELEMENT, true, true),
			new KeySpec(KeyTypes.ENTITY, true, true),
			new KeySpec(KeyTypes.EVENT_ELEMENT, true, true),
			new KeySpec(KeyTypes.FILE, true, true),
			new KeySpec(KeyTypes.MULTI_LANGUAGE_PROPERTY, true, true),
			new KeySpec(KeyTypes.OPERATION, true, true),
			new KeySpec(KeyTypes.PROPERTY, true, true),
			new KeySpec(KeyTypes.RANGE, true, true),
			new KeySpec(KeyTypes.REFERENCE_ELEMENT, true, true),
			new KeySpec(KeyTypes.SUBMODEL_ELEMENT, true, false),
			new KeySpec(KeyTypes.SUBMODEL_ELEMENT_COLLECTION, true, false),
			new KeySpec(KeyTypes.SUBMODEL_ELEMENT_LIST, true, false),
			new KeySpec(KeyTypes.RELATIONSHIP_ELEMENT, true, true),
			
			new KeySpec(KeyTypes.ASSET_ADMINISTRATION_SHELL, false, false),
			new KeySpec(KeyTypes.CONCEPT_DESCRIPTION, false, true),
			new KeySpec(KeyTypes.FRAGMENT_REFERENCE, false, true),
			new KeySpec(KeyTypes.GLOBAL_REFERENCE, false, true),
			new KeySpec(KeyTypes.IDENTIFIABLE, false, true),
			new KeySpec(KeyTypes.REFERABLE, false, true),
			new KeySpec(KeyTypes.SUBMODEL, false, false)
		);
		for ( KeySpec spec : specs ) {
			KEY_TYPE_SPECS.put(spec.m_type, spec);
		}
	}
	
	/**
	 * 주어진 참조가 Submodel을 가리키는 ModelReference인지 여부를 반환한다.
	 * <p>
	 * 참조 타입이 {@link ReferenceTypes#MODEL_REFERENCE}이고 키가 {@link KeyTypes#SUBMODEL} 하나뿐이어야 한다.
	 *
	 * @param reference	검사할 참조. {@code null}이면 {@code false}를 반환한다.
	 * @return	Submodel 참조이면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public static boolean isSubmodelReference(Reference reference) {
		if ( reference == null || reference.getType() != ReferenceTypes.MODEL_REFERENCE ) {
			return false;
		}
        
        List<Key> keys = reference.getKeys();
		if ( keys != null && keys.size() == 1 && keys.get(0).getType() == KeyTypes.SUBMODEL ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * 주어진 참조가 SubmodelElement를 가리키는 ModelReference인지 여부를 반환한다.
	 * <p>
	 * 참조 타입이 {@link ReferenceTypes#MODEL_REFERENCE}이고, 키가 둘 이상이며 첫 번째 키가
	 * {@link KeyTypes#SUBMODEL}이어야 한다. 나머지 키의 타입은 검사하지 않는다.
	 *
	 * @param reference	검사할 참조. {@code null}이면 {@code false}를 반환한다.
	 * @return	SubmodelElement 참조이면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public static boolean isSubmodelElementReference(Reference reference) {
		if ( reference == null || reference.getType() != ReferenceTypes.MODEL_REFERENCE ) {
			return false;
		}
        
        List<Key> keys = reference.getKeys();
		if ( keys != null && keys.size() > 1 && keys.get(0).getType() == KeyTypes.SUBMODEL ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * ModelReference의 첫 번째 키(Submodel 키)로부터 Submodel 식별자 기반 참조를 생성한다.
	 * <p>
	 * 두 번째 이후의 키는 무시된다.
	 *
	 * @param reference	변환할 참조.
	 * @return	Submodel 참조.
	 * @throws IllegalArgumentException	{@code reference}가 {@code null}이거나, ModelReference가 아니거나,
	 * 									키가 없거나, 첫 번째 키가 {@link KeyTypes#SUBMODEL}이 아닌 경우.
	 */
    public static ByIdSubmodelReference toSubmodelReference(Reference reference) {
        Preconditions.checkArgument(reference != null, "reference must be non-null");
        Preconditions.checkArgument(Objects.equals(reference.getType(), ReferenceTypes.MODEL_REFERENCE),
								"reference must be a ModelReference");
        
        List<Key> keys = reference.getKeys();
        Preconditions.checkArgument(keys != null && keys.size() >= 1, "reference must contain at least one key");
        
        Key submodelKey = keys.get(0);
        Preconditions.checkArgument(submodelKey.getType() == KeyTypes.SUBMODEL, "the first key must be a submodel key");
        return DefaultSubmodelReference.ofId(submodelKey.getValue());
    }

	/**
	 * ModelReference를 SubmodelElement 참조({@link DefaultElementReference})로 변환한다.
	 * <p>
	 * 첫 번째 키(Submodel 키)로 Submodel 참조를 만들고, 나머지 키들로 idShort 경로를 구성한다. 정수로 해석되는
	 * 키 값은 인덱스로, 그 밖의 값은 idShort로 취급한다. Property 등 하위 원소를 가질 수 없는 타입의 키는 마지막
	 * 키여야 한다.
	 *
	 * @param reference	변환할 참조.
	 * @return	SubmodelElement 참조.
	 * @throws IllegalArgumentException	{@code reference}가 {@code null}이거나, ModelReference가 아니거나,
	 * 									키가 둘 미만이거나, 첫 번째 키가 {@link KeyTypes#SUBMODEL}이 아니거나,
	 * 									나머지 키 중 SubmodelElement 타입이 아닌 키가 있거나, 하위 원소를 가질 수
	 * 									없는 타입의 키 뒤에 키가 더 있는 경우.
	 */
    public static DefaultElementReference toSubmodelElementReference(Reference reference) {
        Preconditions.checkArgument(reference != null, "reference must be non-null");
        Preconditions.checkArgument(Objects.equals(reference.getType(), ReferenceTypes.MODEL_REFERENCE),
								"reference must be a ModelReference");
        
        List<Key> keys = reference.getKeys();
        Preconditions.checkArgument(keys != null && keys.size() > 1, "reference must contain at least two keys");
        
        Key submodelKey = keys.get(0);
        Preconditions.checkArgument(submodelKey.getType() == KeyTypes.SUBMODEL, "the first key must be a submodel key");
        ByIdSubmodelReference smRef = DefaultSubmodelReference.ofId(submodelKey.getValue());
        
        IdShortPath.Builder builder = IdShortPath.builder();
        List<Key> idShortKeys = keys.subList(1, keys.size());
		for ( int i = 0; i < idShortKeys.size(); ++i ) {
			Key key = idShortKeys.get(i);
			KeySpec spec = KEY_TYPE_SPECS.get(key.getType());
			Preconditions.checkArgument(spec != null, "unknown key type: " + key.getType());
			Preconditions.checkArgument(spec.m_isSubmodelElement, "key type is not a submodel element: " + key.getType());
			
			try {
				int idx = Integer.parseInt(key.getValue());
				builder.index(idx);
			}
			catch ( NumberFormatException e ) {
				builder.idShort(key.getValue());
			}
			// Property 등은 하위 원소를 가질 수 없으므로, 뒤에 키가 더 있으면 잘못된 참조이다.
			Preconditions.checkArgument(!spec.m_isTerminal || i == idShortKeys.size() - 1,
										"key type %s cannot have child keys: reference=%s", key.getType(), reference);
		}
		IdShortPath idShortPath = builder.build();
		
		return DefaultElementReference.newInstance(smRef, idShortPath.toString());
    }
}
