package mdt.tree.sm.data;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.InternalException;

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
import mdt.tree.ListNode;


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
			return new OperationNode((Operation)entity);
		}
		else if ( ProductionPlannings.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionPlanning> plannings = ((ProductionPlannings)entity).getMemberList();
			if ( plannings.size() != 1 ) {
				return new ListNode<>("ProductionPlannings", plannings, ProductionPlanningNode::new);
			}
			else {
				return new ProductionPlanningNode(plannings.get(0));
			}
		}
		else if ( ProductionOrders.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionOrder> orders = ((ProductionOrders)entity).getMemberList();
			if ( orders.size() != 1 ) {
				return new ListNode<>("ProductionOrders", orders, ProductionOrderNode::new);
			}
			else {
				return new ProductionOrderNode(orders.get(0));
			}
		}
		else if ( ProductionPerformances.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionPerformance> perfs = ((ProductionPerformances)entity).getMemberList();
			if ( perfs.size() != 1 ) {
				return new ListNode<>("ProductionPerformances", perfs, ProductionPerformanceNode::new);
			}
			else {
				return new ProductionPerformanceNode(perfs.get(0));
			}
		}
		else if ( Repairs.class.isAssignableFrom(entityClass) ) {
			List<? extends Repair> repairs = ((Repairs)entity).getMemberList();
			if ( repairs.size() != 1 ) {
				return new ListNode<>("Repairs", repairs, RepairNode::new);
			}
			else {
				return new RepairNode(repairs.get(0));
			}
		}
		else if ( ItemMasters.class.isAssignableFrom(entityClass) ) {
			List<? extends ItemMaster> items = ((ItemMasters)entity).getMemberList();
			if ( items.size() != 1 ) {
				return new ListNode<>("ItemMasters", items, ItemMasterNode::new);
			}
			else {
				return new ItemMasterNode(items.get(0));
			}
		}
		else if ( Andons.class.isAssignableFrom(entityClass) ) {
			List<? extends Andon> andons = ((Andons)entity).getMemberList();
			if ( andons.size() != 1 ) {
				return new ListNode<>("Andons", andons, AndonNode::new);
			}
			else {
				return new AndonNode(andons.get(0));
			}
		}
		else if ( BOMs.class.isAssignableFrom(entityClass) ) {
			List<? extends BOM> boms = ((BOMs)entity).getMemberList();
			if ( boms.size() != 1 ) {
				return new ListNode<>("BOMs", boms, BOMNode::new);
			}
			else {
				return new BOMNode(boms.get(0));
			}
		}
		else if ( Routings.class.isAssignableFrom(entityClass) ) {
			List<? extends Routing> routings = ((Routings)entity).getMemberList();
			if ( routings.size() != 1 ) {
				return new ListNode<>("Routings", routings, RoutingNode::new);
			}
			else {
				return new RoutingNode(routings.get(0));
			}
		}
		else if ( Equipments.class.isAssignableFrom(entityClass) ) {
			List<? extends Equipment> equipments = ((Equipments)entity).getMemberList();
			if ( equipments.size() != 1 ) {
				return new ListNode<>("Equipments", equipments, (equip) -> new EquipmentNode("", equip));
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