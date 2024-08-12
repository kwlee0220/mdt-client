package mdt.cli.tree;

import java.util.Collections;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.File;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileNode implements Node {
	private File m_file;
	
	public FileNode(File file) {
		m_file = file;
	}

	@Override
	public String getText() {
		return String.format("%s: content-type=%s", m_file.getIdShort(), m_file.getContentType());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
