package mdt.model.sm.entity;


/**
 * AAS 모델 객체(Submodel, SubmodelElement)와 상호 변환되는 Java 엔티티 인터페이스.
 * <p>
 * {@link #updateFromAasModel(Object)}는 AAS 모델 → 엔티티 방향, {@link #updateAasModel(Object)}는
 * 엔티티 → AAS 모델 방향의 변환을 수행한다.
 *
 * @param <T>	대응되는 AAS 모델 객체의 타입.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AASModelEntity<T> {
	/**
	 * 주어진 AAS 모델 객체에 이 엔티티의 내용을 반영한다.
	 *
	 * @param model	갱신할 AAS 모델 객체.
	 */
	public void updateAasModel(T model);
	/**
	 * 주어진 AAS 모델 객체의 내용으로 이 엔티티를 갱신한다.
	 *
	 * @param model	읽어 들일 AAS 모델 객체.
	 */
	public void updateFromAasModel(T model);
}
