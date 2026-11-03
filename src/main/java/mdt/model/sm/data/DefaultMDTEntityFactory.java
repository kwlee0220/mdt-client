package mdt.model.sm.data;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import mdt.model.MDTEntityFactory;
import mdt.model.ModelGenerationException;
import mdt.model.sm.entity.SubmodelElementEntity;


/**
 * Data 서브모델의 {@code DataInfo}에 포함되는 최상위 엔티티 객체를 생성하는
 * {@link MDTEntityFactory}의 기본 구현 클래스이다.
 * <p>
 * 엔티티 인터페이스 ({@link Equipment}, {@link Operation}, {@link Line}, {@link Andons} 등)의
 * 전체 클래스 이름 또는 단순 클래스 이름을 식별자로 하여 해당 Default 구현 객체를 생성한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultMDTEntityFactory implements MDTEntityFactory {
	private Map<String,Supplier<? extends SubmodelElementEntity>> FQCN_TO_ADAPTORS = Maps.newHashMap();
	private Map<String,Supplier<? extends SubmodelElementEntity>> NAME_TO_ADAPTORS = Maps.newHashMap();
	
	/**
	 * 지원하는 모든 Data 서브모델 엔티티가 등록된 팩토리 객체를 생성한다.
	 */
	public DefaultMDTEntityFactory() {
		FQCN_TO_ADAPTORS.put(Equipment.class.getName(), DefaultEquipment::new);
		FQCN_TO_ADAPTORS.put(Operation.class.getName(), DefaultOperation::new);
		FQCN_TO_ADAPTORS.put(Line.class.getName(), DefaultLine::new);
		FQCN_TO_ADAPTORS.put(ProductionPlannings.class.getName(), DefaultProductionPlannings::new);
		FQCN_TO_ADAPTORS.put(ProductionOrders.class.getName(), DefaultProductionOrders::new);
		FQCN_TO_ADAPTORS.put(ProductionPerformances.class.getName(), DefaultProductionPerformances::new);
		FQCN_TO_ADAPTORS.put(Repairs.class.getName(), DefaultRepairs::new);
		FQCN_TO_ADAPTORS.put(Andons.class.getName(), DefaultAndons::new);
		FQCN_TO_ADAPTORS.put(ItemMasters.class.getName(), DefaultItemMasters::new);
		FQCN_TO_ADAPTORS.put(BOMs.class.getName(), DefaultBOMs::new);
		FQCN_TO_ADAPTORS.put(Routings.class.getName(), DefaultRoutings::new);
		FQCN_TO_ADAPTORS.put(Equipments.class.getName(), DefaultEquipments::new);
		
		NAME_TO_ADAPTORS.put(Equipment.class.getSimpleName(), DefaultEquipment::new);
		NAME_TO_ADAPTORS.put(Operation.class.getSimpleName(), DefaultOperation::new);
		NAME_TO_ADAPTORS.put(Line.class.getSimpleName(), DefaultLine::new);
		NAME_TO_ADAPTORS.put(ProductionPlannings.class.getSimpleName(), DefaultProductionPlannings::new);
		NAME_TO_ADAPTORS.put(ProductionOrders.class.getSimpleName(), DefaultProductionOrders::new);
		NAME_TO_ADAPTORS.put(ProductionPerformances.class.getSimpleName(), DefaultProductionPerformances::new);
		NAME_TO_ADAPTORS.put(Repairs.class.getSimpleName(), DefaultRepairs::new);
		NAME_TO_ADAPTORS.put(Andons.class.getSimpleName(), DefaultAndons::new);
		NAME_TO_ADAPTORS.put(ItemMasters.class.getSimpleName(), DefaultItemMasters::new);
		NAME_TO_ADAPTORS.put(BOMs.class.getSimpleName(), DefaultBOMs::new);
		NAME_TO_ADAPTORS.put(Routings.class.getSimpleName(), DefaultRoutings::new);
		NAME_TO_ADAPTORS.put(Equipments.class.getSimpleName(), DefaultEquipments::new);
	}
	
	/**
	 * 주어진 식별자에 해당하는 엔티티 객체를 생성한다.
	 *
	 * @param id	엔티티 인터페이스의 전체 클래스 이름 (예: {@code mdt.model.sm.data.Equipment}) 또는
	 * 			단순 클래스 이름 (예: {@code Equipment}).
	 * @return	생성된 엔티티 객체.
	 * @throws IllegalArgumentException	식별자에 해당하는 엔티티가 등록되지 않은 경우.
	 * @throws ModelGenerationException	엔티티 객체 생성 중 오류가 발생한 경우.
	 */
	public SubmodelElementEntity newInstance(String id) {
		Supplier<? extends SubmodelElementEntity> loader = FQCN_TO_ADAPTORS.get(id);
		if ( loader == null ) {
			loader = NAME_TO_ADAPTORS.get(id);
		}
		if ( loader == null ) {
			throw new IllegalArgumentException("Unknown MDTEntity: id=" + id);
		}
		
		try {
			return loader.get();
		}
		catch ( Throwable e ) {
			throw new ModelGenerationException("Failed to create MDTEntity: cause=" + e);
		}
	}
}
