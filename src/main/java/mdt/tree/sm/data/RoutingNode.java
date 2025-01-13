package mdt.tree.sm.data;

import mdt.model.sm.data.Routing;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RoutingNode extends DefaultNode {
	public RoutingNode(Routing routing) {
		String title = String.format("Routing: %s, Item=%s setup=%s",
										routing.getRoutingName(), routing.getItemID(), routing.getSetupTime());
		setTitle(title);
	}
}