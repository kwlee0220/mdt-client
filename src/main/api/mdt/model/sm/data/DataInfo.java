package mdt.model.sm.data;

import java.util.List;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.sm.entity.SubmodelElementEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface DataInfo {
	public List<SubmodelElementEntity> getSubmodelElementEntityAll();
	
	public default boolean existsEntity(Class<?> intfc) {
		return Funcs.exists(getSubmodelElementEntityAll(), intfc::isInstance);
	}
	
	public default boolean isEquipment() {
		return existsEntity(Equipment.class);
	}
	
	public default boolean isOperation() {
		return existsEntity(Operation.class);
	}
	
	public default Equipment getEquipment() {
		try {
			return getFirstSubmodelElementEntityByClass(Equipment.class);
		}
		catch ( ResourceNotFoundException e ) {
			throw new ResourceNotFoundException("Equipment", "");
		}
	}
	
	public default Operation getOperation() {
		try {
			return getFirstSubmodelElementEntityByClass(Operation.class);
		}
		catch ( ResourceNotFoundException e ) {
			throw new ResourceNotFoundException("Operation", "");
		}
	}
	
	public default List<SubmodelElementEntity> getSubmodelElementEntityAllByClass(Class<?> intfc) {
		return FStream.from(getSubmodelElementEntityAll())
						.filter(intfc::isInstance)
						.toList();
	}
	
	public default <T> T getFirstSubmodelElementEntityByClass(Class<T> intfc)
		throws ResourceNotFoundException {
		return FStream.from(getSubmodelElementEntityAll())
						.filter(intfc::isInstance)
						.findFirst()
						.cast(intfc)
						.getOrThrow(() -> new ResourceNotFoundException("TopLevelEntity", "interface=" + intfc));
	}
}
