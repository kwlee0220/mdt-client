package mdt.tree;

import java.util.Collections;

import org.barfuin.texttree.api.Node;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileNode implements Node {
	private final org.eclipse.digitaltwin.aas4j.v3.model.File m_file;
	
	public FileNode(org.eclipse.digitaltwin.aas4j.v3.model.File file) {
		m_file = file;
	}

	@Override
	public String getText() {
		return String.format("%s (FILE): %s (%s)",
								m_file.getIdShort(), m_file.getValue(), m_file.getContentType());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
