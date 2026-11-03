package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

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
 * AAS {@link Reference} 객체의 생성과 검사를 위한 유틸리티 클래스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ReferenceUtils {
	private ReferenceUtils() {
		throw new AssertionError("Should not be called: class=" + ReferenceUtils.class.getName());
	}
	
	/**
	 * 주어진 참조가 model reference인지 검사한다.
	 *
	 * @param ref	검사할 참조.
	 * @throws IllegalArgumentException	참조 타입이 {@link ReferenceTypes#MODEL_REFERENCE}가 아닌 경우.
	 */
	public static void assertModelReference(Reference ref) {
		if ( ref.getType() != ReferenceTypes.MODEL_REFERENCE ) {
			throw new IllegalArgumentException("Not ModelReference: type=" + ref.getType());
		}
	}
	/**
	 * 주어진 참조가 Submodel을 가리키는 model reference인지 검사한다.
	 * <p>
	 * 키가 하나뿐이고 그 타입이 {@link KeyTypes#SUBMODEL}이어야 한다.
	 *
	 * @param ref	검사할 참조.
	 * @throws IllegalArgumentException	Submodel 참조가 아닌 경우.
	 */
	public static void assertSubmodelReference(Reference ref) {
		assertModelReference(ref);
		
		List<Key> keySeq = ref.getKeys();
		if ( !(keySeq.size() == 1 && keySeq.get(0).getType() == KeyTypes.SUBMODEL) ) {
			throw new IllegalArgumentException("Not Submodel Reference: keys" + keySeq);
		}
	}
	/**
	 * 주어진 참조가 SubmodelElement를 가리키는 model reference인지 검사한다.
	 * <p>
	 * 키가 둘 이상이고 첫 번째 키의 타입이 {@link KeyTypes#SUBMODEL}이어야 한다.
	 *
	 * @param ref	검사할 참조.
	 * @throws IllegalArgumentException	SubmodelElement 참조가 아닌 경우.
	 */
	public static void assertSubmodelElementReference(Reference ref) {
		assertModelReference(ref);
		
		List<Key> keySeq = ref.getKeys();
		if ( !(keySeq.size() > 1
				&& keySeq.get(0).getType() == KeyTypes.SUBMODEL) ) {
			throw new IllegalArgumentException("Not SubmodelElement Reference: keys" + keySeq);
		}
	}
	
	/**
	 * 주어진 AAS 식별자를 가리키는 model reference를 생성한다.
	 *
	 * @param id	AAS 식별자.
	 * @return	{@link KeyTypes#ASSET_ADMINISTRATION_SHELL} 키 하나로 구성된 참조.
	 */
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
	
	/**
	 * 주어진 Submodel 식별자를 가리키는 model reference를 생성한다.
	 *
	 * @param id	Submodel 식별자.
	 * @return	{@link KeyTypes#SUBMODEL} 키 하나로 구성된 참조.
	 */
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

	/**
	 * 주어진 Submodel 내 idShortPath에 해당하는 SubmodelElement를 가리키는 model reference를 생성한다.
	 * <p>
	 * 첫 번째 키는 {@link KeyTypes#SUBMODEL} 타입의 Submodel 식별자이고, 이후 idShortPath의 각 구성요소가
	 * {@link KeyTypes#SUBMODEL_ELEMENT} 타입의 키로 이어진다.
	 *
	 * @param submodelId	Submodel 식별자.
	 * @param idShortPath	Submodel 내 SubmodelElement의 idShortPath.
	 * @return	생성된 참조.
	 */
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

	/**
	 * MDT 서브모델의 semantic id를 짧은 약어로 변환한다.
	 * <p>
	 * InformationModel, Data, Simulation, AI, TimeSeries, Shape 서브모델의 semantic id를 각각
	 * {@code "Info"}, {@code "Data"}, {@code "Sim"}, {@code "AI"}, {@code "TS"}, {@code "Shape"}로 변환한다.
	 *
	 * @param semanticIdStr	서브모델 semantic id 문자열.
	 * @return	약어. {@code semanticIdStr}이 {@code null}이면 {@code "?"}.
	 * @throws InternalException	알 수 없는 semantic id인 경우.
	 */
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
