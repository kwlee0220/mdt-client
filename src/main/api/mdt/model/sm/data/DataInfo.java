package mdt.model.sm.data;

import java.util.List;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.sm.entity.SubmodelElementEntity;


/**
 * {@code DataInfo}는 Data 서브모델의 {@code DataInfo} SubmodelElementCollection에 포함된
 * 최상위 엔티티들 ({@link Equipment}, {@link Operation}, {@link Line}, {@link Andons} 등)을 다루는
 * 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface DataInfo {
	/**
	 * {@code DataInfo}에 포함된 모든 최상위 엔티티 목록을 반환한다.
	 *
	 * @return	엔티티 객체 목록.
	 */
	public List<SubmodelElementEntity> getSubmodelElementEntityAll();
	
	/**
	 * 주어진 타입의 엔티티가 포함되어 있는지 여부를 반환한다.
	 *
	 * @param intfc	검사할 엔티티 타입 (인터페이스).
	 * @return	{@code intfc} 타입의 엔티티가 하나 이상 있으면 {@code true},
	 * 			그렇지 않으면 {@code false}.
	 */
	public default boolean existsEntity(Class<?> intfc) {
		return Funcs.exists(getSubmodelElementEntityAll(), intfc::isInstance);
	}
	
	/**
	 * {@link Equipment} 엔티티가 포함되어 있는지 여부를 반환한다.
	 *
	 * @return	포함되어 있으면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public default boolean isEquipment() {
		return existsEntity(Equipment.class);
	}
	
	/**
	 * {@link Operation} 엔티티가 포함되어 있는지 여부를 반환한다.
	 *
	 * @return	포함되어 있으면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public default boolean isOperation() {
		return existsEntity(Operation.class);
	}
	
	/**
	 * 포함된 첫번째 {@link Equipment} 엔티티를 반환한다.
	 *
	 * @return	{@link Equipment} 객체.
	 * @throws ResourceNotFoundException	{@link Equipment} 엔티티가 없는 경우. 메시지는
	 * 			{@code "DataInfo entity(type=Equipment)"}이다.
	 */
	public default Equipment getEquipment() {
		return getFirstSubmodelElementEntityByClass(Equipment.class);
	}
	
	/**
	 * 포함된 첫번째 {@link Operation} 엔티티를 반환한다.
	 *
	 * @return	{@link Operation} 객체.
	 * @throws ResourceNotFoundException	{@link Operation} 엔티티가 없는 경우. 메시지는
	 * 			{@code "DataInfo entity(type=Operation)"}이다.
	 */
	public default Operation getOperation() {
		return getFirstSubmodelElementEntityByClass(Operation.class);
	}
	
	/**
	 * 주어진 타입에 해당하는 모든 엔티티 목록을 반환한다.
	 *
	 * @param intfc	검색할 엔티티 타입 (인터페이스).
	 * @return	{@code intfc} 타입의 엔티티 목록. 해당 엔티티가 없으면 빈 목록.
	 */
	public default List<SubmodelElementEntity> getSubmodelElementEntityAllByClass(Class<?> intfc) {
		return FStream.from(getSubmodelElementEntityAll())
						.filter(intfc::isInstance)
						.toList();
	}
	
	/**
	 * 주어진 타입에 해당하는 첫번째 엔티티를 반환한다.
	 *
	 * @param <T>	엔티티 타입.
	 * @param intfc	검색할 엔티티 타입 (인터페이스).
	 * @return	{@code intfc} 타입의 첫번째 엔티티.
	 * @throws ResourceNotFoundException	해당 타입의 엔티티가 없는 경우. 메시지는
	 * 			{@code "DataInfo entity(type=<타입의 단순 이름>)"}이다.
	 */
	public default <T> T getFirstSubmodelElementEntityByClass(Class<T> intfc)
		throws ResourceNotFoundException {
		return FStream.from(getSubmodelElementEntityAll())
						.filter(intfc::isInstance)
						.findFirst()
						.cast(intfc)
						.getOrThrow(() -> new ResourceNotFoundException("DataInfo entity", "type=" + intfc.getSimpleName()));
	}
}
