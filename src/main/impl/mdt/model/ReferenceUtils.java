package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import lombok.experimental.UtilityClass;

import utils.InternalException;
import utils.stream.FStream;

import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.shape.Shape;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.IdShortPath;
import mdt.model.sm.value.IdShortPath.IdShort;
import mdt.model.timeseries.TimeSeries;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class ReferenceUtils {
	public static void assertModelReference(Reference ref) {
		if ( ref.getType() != ReferenceTypes.MODEL_REFERENCE ) {
			throw new IllegalArgumentException("Not ModelReference: type=" + ref.getType());
		}
	}
	public static void assertSubmodelReference(Reference ref) {
		assertModelReference(ref);
		
		List<Key> keySeq = ref.getKeys();
		if ( !(keySeq.size() == 1 && keySeq.get(0).getType() == KeyTypes.SUBMODEL) ) {
			throw new IllegalArgumentException("Not Submodel Reference: keys" + keySeq);
		}
	}
	public static void assertSubmodelElementReference(Reference ref) {
		assertModelReference(ref);
		
		List<Key> keySeq = ref.getKeys();
		if ( !(keySeq.size() > 1
				&& keySeq.get(0).getType() == KeyTypes.SUBMODEL) ) {
			throw new IllegalArgumentException("Not SubmodelElement Reference: keys" + keySeq);
		}
	}
	
	public static Reference toAASReference(String id) {
		Key key = new DefaultKey.Builder()
								.value(id)
								.type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
								.build();
		return new DefaultReference.Builder()
									.type(ReferenceTypes.MODEL_REFERENCE)
									.keys(key)
									.build();
	}
	
	public static Reference toSubmodelReference(String id) {
		Key key = new DefaultKey.Builder()
								.value(id)
								.type(KeyTypes.SUBMODEL)
								.build();
		return new DefaultReference.Builder()
									.type(ReferenceTypes.MODEL_REFERENCE)
									.keys(key)
									.build();
	}

	public static Reference toSubmodelElementReference(String submodelId, IdShortPath idShortPath) {
		Key smKey = new DefaultKey.Builder()
									.type(KeyTypes.SUBMODEL)
									.value(submodelId)
									.build();
		List<Key> keyList = FStream.from(idShortPath.getIdShortList())
									.map(idShort -> ReferenceUtils.newKey(KeyTypes.SUBMODEL_ELEMENT, idShort))
									.toList();
		keyList.add(0, smKey);
		
		return new DefaultReference.Builder()
									.type(ReferenceTypes.MODEL_REFERENCE)
									.keys(keyList)
									.build();
	}
	
	/**
	 * 주어진 Reference에서 semanticId로 사용되는 Key의 값을 반환한다.
	 *
	 * @param semanticId	semanticId Reference 객체.
	 * 						이 Reference는 Submodel 또는 SubmodelElement의 semanticId로 사용되는 Reference여야 한다.
	 * @return	semanticId로 사용되는 Key의 값. 만약 semanticId가 null인 경우에는 null을 반환한다.
	 */
	public static String getSemanticIdStringOrNull(Reference semanticId) {
		return (semanticId != null) ? semanticId.getKeys().get(0).getValue() : null;
	}

	public static String getShortSubmodelSemanticId(String semanticIdStr) {
		if ( semanticIdStr == null ) {
			return "?";
		}
		
		return switch ( semanticIdStr ) {
			case InformationModel.SEMANTIC_ID -> "Info";
			case Data.SEMANTIC_ID -> "Data";
			case Simulation.SEMANTIC_ID -> "Sim";
			case AI.SEMANTIC_ID -> "AI";
			case TimeSeries.SEMANTIC_ID -> "TS";
			case Shape.SEMANTIC_ID -> "Shape";
			default -> throw new InternalException("unknown Submodel " + semanticIdStr);
		};
	}

	private static Key newKey(KeyTypes keyType, IdShort idShort) {
		return new DefaultKey.Builder()
								.type(keyType)
								.value(idShort.getKey())
								.build();
	}
}
