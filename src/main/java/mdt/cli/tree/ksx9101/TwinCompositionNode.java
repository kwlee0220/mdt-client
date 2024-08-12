package mdt.cli.tree.ksx9101;

import org.barfuin.texttree.api.Node;

import utils.Indexed;
import utils.stream.FStream;

import mdt.cli.tree.TextNode;
import mdt.ksx9101.model.CompositionDependency;
import mdt.ksx9101.model.TwinComposition;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class TwinCompositionNode implements Node {
	private TwinComposition m_twinCompo;
	
	public TwinCompositionNode(TwinComposition twinCompo) {
		m_twinCompo = twinCompo;
	}

	@Override
	public String getText() {
		return String.format("Twin Components:");
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return FStream.from(m_twinCompo.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals("contains"))
						.zipWithIndex()
						.map(this::toNode)
						.toList();
	}
	
	private Node toNode(Indexed<CompositionDependency> indexed) {
		CompositionDependency dep = indexed.value();
		String text = String.format("[%02d] %s", indexed.index(), dep.getTarget());
		return new TextNode(text);
	}
}