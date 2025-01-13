package mdt.tree.sm.data;

import mdt.model.sm.data.Andon;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class AndonNode extends DefaultNode {
	private Andon m_andon;
	
	public AndonNode(Andon andon) {
		String title = String.format("Andon: Operation=%s, Start=%s, End=%s, Cause=%s",
										m_andon.getOperationID(),
										m_andon.getStartDateTime().trim(),
										m_andon.getEndDateTime().trim(),
										m_andon.getCauseName());
		setTitle(title);
	}
}