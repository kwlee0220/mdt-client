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

import lombok.experimental.UtilityClass;

import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdSubmodelReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class References {
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

    public static ByIdSubmodelReference toSubmodelReference(Reference reference) {
        Preconditions.checkArgument(reference != null, "reference must be non-null");
        Preconditions.checkArgument(Objects.equals(reference.getType(), ReferenceTypes.MODEL_REFERENCE),
								"reference must be a ModelReference");
        
        List<Key> keys = reference.getKeys();
        Preconditions.checkArgument(keys != null && keys.size() >= 1, "reference must contain at least two keys");
        
        Key submodelKey = keys.get(0);
        Preconditions.checkArgument(submodelKey.getType() == KeyTypes.SUBMODEL, "the first key must be a submodel key");
        return DefaultSubmodelReference.ofId(submodelKey.getValue());
    }

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
        List<Key> idShortKeys = keys.subList(1, keys.size()-1);
		for ( Key key : idShortKeys ) {
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
			if ( spec.m_isTerminal ) {
				break;
			}
		}
		IdShortPath idShortPath = builder.build();
		
		return DefaultElementReference.newInstance(smRef, idShortPath.toString());
    }
}
