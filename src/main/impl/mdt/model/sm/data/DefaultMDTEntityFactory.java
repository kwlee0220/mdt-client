package mdt.model.sm.data;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import mdt.model.MDTEntityFactory;
import mdt.model.ModelGenerationException;
import mdt.model.sm.entity.SubmodelElementEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultMDTEntityFactory implements MDTEntityFactory {
	private Map<String,Supplier<? extends SubmodelElementEntity>> FQCN_TO_ADAPTORS = Maps.newHashMap();
	private Map<String,Supplier<? extends SubmodelElementEntity>> NAME_TO_ADAPTORS = Maps.newHashMap();
	
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
