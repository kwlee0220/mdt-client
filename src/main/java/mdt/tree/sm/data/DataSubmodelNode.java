package mdt.tree.sm.data;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.InternalException;
import utils.stream.FStream;

import mdt.model.sm.data.Andon;
import mdt.model.sm.data.Andons;
import mdt.model.sm.data.BOM;
import mdt.model.sm.data.BOMs;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Equipments;
import mdt.model.sm.data.ItemMaster;
import mdt.model.sm.data.ItemMasters;
import mdt.model.sm.data.Line;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ProductionOrder;
import mdt.model.sm.data.ProductionOrders;
import mdt.model.sm.data.ProductionPerformance;
import mdt.model.sm.data.ProductionPerformances;
import mdt.model.sm.data.ProductionPlanning;
import mdt.model.sm.data.ProductionPlannings;
import mdt.model.sm.data.Repair;
import mdt.model.sm.data.Repairs;
import mdt.model.sm.data.Routing;
import mdt.model.sm.data.Routings;
import mdt.model.sm.entity.SubmodelElementEntity;
import mdt.tree.ArrayNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DataSubmodelNode implements Node {
	private Data m_data;
	
	public DataSubmodelNode(Data data) {
		m_data = data;
	}

	@Override
	public String getText() {
		return String.format("Data (%s)", m_data.getSubModelInfo().getTitle());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return m_data.getDataInfo()
						.getSubmodelElementEntityAll()
						.stream()
						.filter(ent -> !(ent instanceof ProductionOrders))
						.map(this::toNode)
						.toList();
	}

	private Node toNode(SubmodelElementEntity entity) {
		Class<?> entityClass = entity.getClass();
		if ( Line.class.isAssignableFrom(entityClass) ) {
			return new LineNode((Line)entity);
		}
		else if ( Equipment.class.isAssignableFrom(entityClass) ) {
			return new EquipmentNode("", (Equipment)entity);
		}
		else if ( Operation.class.isAssignableFrom(entityClass) ) {
			return new ProcessNode((Operation)entity);
		}
		else if ( ProductionPlannings.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionPlanning> plannings = ((ProductionPlannings)entity).getElementAll();
			if ( plannings.size() != 1 ) {
				List<ProductionPlanningNode> nodes = FStream.from(plannings)
															.map(ProductionPlanningNode::new)
															.toList();
				return new ArrayNode("ProductionPlannings", nodes);
			}
			else {
				return new ProductionPlanningNode(plannings.get(0));
			}
		}
		else if ( ProductionOrders.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionOrder> orders = ((ProductionOrders)entity).getElementAll();
			if ( orders.size() != 1 ) {
				List<ProductionOrderNode> nodes = FStream.from(orders)
															.map(ProductionOrderNode::new)
															.toList();
				return new ArrayNode("ProductionOrders", nodes);
			}
			else {
				return new ProductionOrderNode(orders.get(0));
			}
		}
		else if ( ProductionPerformances.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionPerformance> perfs = ((ProductionPerformances)entity).getElementAll();
			if ( perfs.size() != 1 ) {
				List<ProductionPerformanceNode> nodes = FStream.from(perfs)
															.map(ProductionPerformanceNode::new)
															.toList();
				return new ArrayNode("ProductionPerformances", nodes);
			}
			else {
				return new ProductionPerformanceNode(perfs.get(0));
			}
		}
		else if ( Repairs.class.isAssignableFrom(entityClass) ) {
			List<? extends Repair> repairs = ((Repairs)entity).getElementAll();
			if ( repairs.size() != 1 ) {
				List<RepairNode> nodes = FStream.from(repairs)
												.map(RepairNode::new)
												.toList();
				return new ArrayNode("Repairs", nodes);
			}
			else {
				return new RepairNode(repairs.get(0));
			}
		}
		else if ( ItemMasters.class.isAssignableFrom(entityClass) ) {
			List<? extends ItemMaster> items = ((ItemMasters)entity).getElementAll();
			if ( items.size() != 1 ) {
				List<ItemMasterNode> nodes = FStream.from(items)
												.map(ItemMasterNode::new)
												.toList();
				return new ArrayNode("ItemMasters", nodes);
			}
			else {
				return new ItemMasterNode(items.get(0));
			}
		}
		else if ( Andons.class.isAssignableFrom(entityClass) ) {
			List<? extends Andon> andons = ((Andons)entity).getElementAll();
			if ( andons.size() != 1 ) {
				List<AndonNode> nodes = FStream.from(andons)
												.map(AndonNode::new)
												.toList();
				return new ArrayNode("ItemMasters", nodes);
			}
			else {
				return new AndonNode(andons.get(0));
			}
		}
		else if ( BOMs.class.isAssignableFrom(entityClass) ) {
			List<? extends BOM> boms = ((BOMs)entity).getElementAll();
			if ( boms.size() != 1 ) {
				List<BOMNode> nodes = FStream.from(boms)
												.map(BOMNode::new)
												.toList();
				return new ArrayNode("BOMs", nodes);
			}
			else {
				return new BOMNode(boms.get(0));
			}
		}
		else if ( Routings.class.isAssignableFrom(entityClass) ) {
			List<? extends Routing> routings = ((Routings)entity).getElementAll();
			if ( routings.size() != 1 ) {
				List<RoutingNode> nodes = FStream.from(routings)
												.map(RoutingNode::new)
												.toList();
				return new ArrayNode("Routings", nodes);
			}
			else {
				return new RoutingNode(routings.get(0));
			}
		}
		else if ( Equipments.class.isAssignableFrom(entityClass) ) {
			List<? extends Equipment> equipments = ((Equipments)entity).getElementAll();
			if ( equipments.size() != 1 ) {
				List<EquipmentNode> nodes = FStream.from(equipments)
													.map((equip) -> new EquipmentNode("", equip))
													.toList();
				return new ArrayNode("Equipments", nodes);
			}
			else {
				return new EquipmentNode("", equipments.get(0));
			}
		}
		else {
			throw new InternalException("Unknown KSX9101RootEntity: id=" + entityClass.getName());
		}
	}
}