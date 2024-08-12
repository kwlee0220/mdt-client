package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.InternalException;

import mdt.cli.tree.ListNode;
import mdt.ksx9101.TopLevelEntity;
import mdt.ksx9101.model.Andon;
import mdt.ksx9101.model.Andons;
import mdt.ksx9101.model.BOM;
import mdt.ksx9101.model.BOMs;
import mdt.ksx9101.model.Data;
import mdt.ksx9101.model.Equipment;
import mdt.ksx9101.model.Equipments;
import mdt.ksx9101.model.ItemMaster;
import mdt.ksx9101.model.ItemMasters;
import mdt.ksx9101.model.Line;
import mdt.ksx9101.model.Operation;
import mdt.ksx9101.model.ProductionOrder;
import mdt.ksx9101.model.ProductionOrders;
import mdt.ksx9101.model.ProductionPerformance;
import mdt.ksx9101.model.ProductionPerformances;
import mdt.ksx9101.model.ProductionPlanning;
import mdt.ksx9101.model.ProductionPlannings;
import mdt.ksx9101.model.Repair;
import mdt.ksx9101.model.Repairs;
import mdt.ksx9101.model.Routing;
import mdt.ksx9101.model.Routings;


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
		return String.format("Data (%s)", m_data.getId());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return m_data.getDataInfo()
						.getKSX9101EntityList()
						.stream()
						.filter(ent -> !(ent instanceof ProductionOrders))
						.map(this::toNode)
						.toList();
	}

	private Node toNode(TopLevelEntity entity) {
		Class<?> entityClass = entity.getClass();
		if ( Line.class.isAssignableFrom(entityClass) ) {
			return new LineNode((Line)entity);
		}
		else if ( Equipment.class.isAssignableFrom(entityClass) ) {
			return new EquipmentNode((Equipment)entity);
		}
		else if ( Operation.class.isAssignableFrom(entityClass) ) {
			return new KSX9101OperationNode((Operation)entity);
		}
		else if ( ProductionPlannings.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionPlanning> plannings = ((ProductionPlannings)entity).getElements();
			if ( plannings.size() != 1 ) {
				return new ListNode<>("ProductionPlannings", plannings, ProductionPlanningNode::new);
			}
			else {
				return new ProductionPlanningNode(plannings.get(0));
			}
		}
		else if ( ProductionOrders.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionOrder> orders = ((ProductionOrders)entity).getElements();
			if ( orders.size() != 1 ) {
				return new ListNode<>("ProductionOrders", orders, ProductionOrderNode::new);
			}
			else {
				return new ProductionOrderNode(orders.get(0));
			}
		}
		else if ( ProductionPerformances.class.isAssignableFrom(entityClass) ) {
			List<? extends ProductionPerformance> perfs = ((ProductionPerformances)entity).getElements();
			if ( perfs.size() != 1 ) {
				return new ListNode<>("ProductionPerformances", perfs, ProductionPerformanceNode::new);
			}
			else {
				return new ProductionPerformanceNode(perfs.get(0));
			}
		}
		else if ( Repairs.class.isAssignableFrom(entityClass) ) {
			List<? extends Repair> repairs = ((Repairs)entity).getElements();
			if ( repairs.size() != 1 ) {
				return new ListNode<>("Repairs", repairs, RepairNode::new);
			}
			else {
				return new RepairNode(repairs.get(0));
			}
		}
		else if ( ItemMasters.class.isAssignableFrom(entityClass) ) {
			List<? extends ItemMaster> items = ((ItemMasters)entity).getElements();
			if ( items.size() != 1 ) {
				return new ListNode<>("ItemMasters", items, ItemMasterNode::new);
			}
			else {
				return new ItemMasterNode(items.get(0));
			}
		}
		else if ( Andons.class.isAssignableFrom(entityClass) ) {
			List<? extends Andon> andons = ((Andons)entity).getElements();
			if ( andons.size() != 1 ) {
				return new ListNode<>("Andons", andons, AndonNode::new);
			}
			else {
				return new AndonNode(andons.get(0));
			}
		}
		else if ( BOMs.class.isAssignableFrom(entityClass) ) {
			List<? extends BOM> boms = ((BOMs)entity).getElements();
			if ( boms.size() != 1 ) {
				return new ListNode<>("BOMs", boms, BOMNode::new);
			}
			else {
				return new BOMNode(boms.get(0));
			}
		}
		else if ( Routings.class.isAssignableFrom(entityClass) ) {
			List<? extends Routing> routings = ((Routings)entity).getElements();
			if ( routings.size() != 1 ) {
				return new ListNode<>("Routings", routings, RoutingNode::new);
			}
			else {
				return new RoutingNode(routings.get(0));
			}
		}
		else if ( Equipments.class.isAssignableFrom(entityClass) ) {
			List<? extends Equipment> equipments = ((Equipments)entity).getElements();
			if ( equipments.size() != 1 ) {
				return new ListNode<>("Equipments", equipments, EquipmentNode::new);
			}
			else {
				return new EquipmentNode(equipments.get(0));
			}
		}
		else {
			throw new InternalException("Unknown KSX9101RootEntity: id=" + entityClass.getName());
		}
	}
}