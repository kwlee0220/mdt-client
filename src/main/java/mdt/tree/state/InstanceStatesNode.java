package mdt.tree.state;

import java.util.List;

import org.barfuin.texttree.api.Node;

import com.google.common.collect.Lists;

import utils.Utilities;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceInfo;
import mdt.model.instance.OperationInfo;
import mdt.model.instance.ParameterInfo;
import mdt.tree.CompositeNode;
import mdt.tree.DefaultNode;
import mdt.tree.ListNode;
import mdt.tree.TitleUpdatableNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class InstanceStatesNode implements Node {
	private final MDTInstance m_instance;
	private final List<Node> m_children;
	
	public InstanceStatesNode(MDTInstance instance, boolean showParameters, boolean showOperations) {
		m_instance = instance;
		MDTInstanceInfo info = instance.getInfo();
		
		m_children = Lists.newArrayList();
		if ( showParameters ) {
			TitleUpdatableNode parameters = createParameterInfoListNode("Parameters", info.getParameters(), false);
			m_children.add(parameters);
		}

		if ( showOperations ) {
			List<? extends TitleUpdatableNode> opInfoNodes = FStream.from(info.getOperations())
																	.map(this::createOperationNode)
																	.toList();
			CompositeNode operations = new CompositeNode("Operations", opInfoNodes);
			m_children.add(operations);
		}
	}

	@Override
	public String getText() {
		return String.format("MDTInstance(%s)", m_instance.getId());
	}
	@Override
	public Iterable<? extends Node> getChildren() {
		return m_children;
	}
	
	DefaultNode createParameterInfoNode(ParameterInfo paramInfo) {
		String type = Utilities.split(paramInfo.getType(), ':', Tuple.of("", paramInfo.getType()))._2;
		String title = String.format("%s (%s): %s", paramInfo.getId(), type, paramInfo.toValueString());
        return new DefaultNode(title);
	}
	
	TitleUpdatableNode createParameterInfoListNode(String title, List<ParameterInfo> paramList, boolean ordered) {
		List<DefaultNode> paramNodeList = FStream.from(paramList)
												.map(this::createParameterInfoNode)
												.toList();
		return (ordered) ? new ListNode(title, paramNodeList) : new CompositeNode(title, paramNodeList);
	}
	
	TitleUpdatableNode createOperationNode(OperationInfo opInfo) {
		String title = String.format("%s(%s)", opInfo.getId(), opInfo.getType());
		List<TitleUpdatableNode> paramNodeList
					= List.of(
						createParameterInfoListNode("Inputs", opInfo.getInputArguments(), true),
						createParameterInfoListNode("Outputs", opInfo.getOutputArguments(), true)
					);
		return new CompositeNode(title, paramNodeList);
	}
}
