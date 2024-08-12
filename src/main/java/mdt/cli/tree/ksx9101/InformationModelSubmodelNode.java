package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.InformationModel;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class InformationModelSubmodelNode implements Node {
	private InformationModel m_infoModel;
	
	public InformationModelSubmodelNode(InformationModel infoModel) {
		m_infoModel = infoModel;
	}

	@Override
	public String getText() {
		return String.format("InformationModel");
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return List.of(new TwinCompositionNode(m_infoModel.getTwinComposition()));
	}
}