package mdt.tree.sm;

import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;

import com.google.common.collect.Lists;

import mdt.client.Utils;
import mdt.tree.TextNode;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class BasicEventElementNode implements Node {
	private BasicEventElement m_event;
	
	public BasicEventElementNode(BasicEventElement submodel) {
		m_event = submodel;
	}

	@Override
	public String getText() {
		return String.format("%s", m_event.getIdShort());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> attributes = Lists.newArrayList();
		attributes.add(new TextNode("Observed: " + m_event.getObserved().getKeys().get(0).getValue()));
		attributes.add(new TextNode("Direction: " + m_event.getDirection()));
		attributes.add(new TextNode("State: " + m_event.getState()));
		attributes.add(new TextNode("MessageTopic: " + m_event.getMessageTopic()));
		attributes.add(new TextNode("MessageBroker: " + m_event.getMessageBroker()));
		attributes.add(new TextNode("LastUpdate: " + m_event.getLastUpdate()));
		attributes.add(new TextNode("MinInterval: " + m_event.getMinInterval()));
		attributes.add(new TextNode("MaxInterval: " + m_event.getMaxInterval()));
		attributes.add(new TextNode("DisplayName: " + Utils.concatLangStringNameTypes(m_event.getDisplayName())));
		
		return FStream.from(attributes);
	}
}
