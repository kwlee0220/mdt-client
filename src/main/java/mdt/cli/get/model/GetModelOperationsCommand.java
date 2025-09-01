package mdt.cli.get.model;

import java.io.IOException;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTOperationDescriptor;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.ListNode;
import mdt.tree.node.TerminalNode;

import picocli.CommandLine.Command;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "operations",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get MDT operation (AI/Simulation) informations for the MDTInstance."
)
public class GetModelOperationsCommand extends AbstractGetMDTModelEntityCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetModelOperationsCommand.class);

	public static final void main(String... args) throws Exception {
		main(new GetModelOperationsCommand(), args);
	}
	
	public GetModelOperationsCommand() {
		setLogger(s_logger);
	}

	@Override
	protected Node toMDTModelNode(MDTInstance instance) throws IOException {
		return new OperationInfoListNode(instance.getMDTOperationDescriptorAll());
	}

	private static class OperationInfoListNode extends DefaultNode {
		protected List<OperationInfoNode> m_opNodeList;
		
		public OperationInfoListNode(List<MDTOperationDescriptor> opList) {
			setTitle("Operations");
			m_opNodeList = FStream.from(opList)
									.map(OperationInfoNode::new)
									.toList();
		}
	
		@Override
		public Iterable<? extends Node> getChildren() {
			return m_opNodeList;
		}
		
		static class OperationInfoNode extends DefaultNode {
			private final List<ArgumentListNode> m_children;
			
			public OperationInfoNode(MDTOperationDescriptor op) {
				setTitle(op.getId());
				setValueType(String.format(" (%s)", op.getOperationType()));
	
				ArgumentListNode inputListNode = new ArgumentListNode("Inputs", op.getInputArguments());
				ArgumentListNode outputListNode = new ArgumentListNode("Outputs", op.getOutputArguments());
				m_children = List.of(inputListNode, outputListNode);
			}
	
			@Override
			public Iterable<? extends Node> getChildren() {
				return m_children;
			}
		}
		
		static class ArgumentListNode extends ListNode {
			private final List<TerminalNode> m_argNodes;
			
			public ArgumentListNode(String title, List<MDTOperationDescriptor.ArgumentDescriptor> args) {
				setTitle(title);
				m_argNodes = FStream.from(args)
									.map(this::create)
									.toList();
			}
	
			@Override
			protected List<? extends DefaultNode> getElementNodes() {
				return m_argNodes;
			}
			
			private TerminalNode create(MDTOperationDescriptor.ArgumentDescriptor arg) {
				TerminalNode node = new TerminalNode();
				node.setTitle(arg.getId());
				node.setValueType(String.format(" (%s)", arg.getValueType()));
				return node;
			}
		}
	}
}
