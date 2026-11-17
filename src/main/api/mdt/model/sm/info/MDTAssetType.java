package mdt.model.sm.info;

/**
 * 트윈화 대상 자산 타입.
 * <p>
 * {@code Machine}과 {@code Material}은 Data 서브모델의 {@code DataInfo.Equipment}에, {@code Process}는
 * {@code DataInfo.Operation}에 정보와 파라미터를 담는다. {@code Line}과 {@code Factory}는 파라미터가 없다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum MDTAssetType {
	/** 설비(기계). */
	Machine,
	/** 공정. */
	Process,
	/** 생산 라인. */
	Line,
	/** 공장. */
	Factory,
	/** 자재(가공 대상물 등). */
	Material,
}
