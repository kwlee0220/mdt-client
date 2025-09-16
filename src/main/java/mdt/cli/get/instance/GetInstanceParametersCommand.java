package mdt.cli.get.instance;

import java.io.IOException;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTParameterDescriptor;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.DefaultNodeFactories;
import mdt.tree.node.ListNode;
import mdt.tree.node.TerminalNode;

import picocli.CommandLine.Command;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "parameters",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get MDT parameter informations for the MDTInstance."
)
public class GetInstanceParametersCommand extends AbstractInstanceSubCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceParametersCommand.class);
	
	private HttpMDTInstanceManager m_manager;
	private List<MDTParameterDescriptor> m_parameters;
	private MDTParameterDescriptorListNode m_paramNodes;

	public static final void main(String... args) throws Exception {
		main(new GetInstanceParametersCommand(), args);
	}
	
	public GetInstanceParametersCommand() {
		setLogger(s_logger);
	}

	@Override
	protected void displayAsJson(HttpMDTInstanceClient instance) throws SerializationException, IOException {
		List<MDTParameterDescriptor> descList = instance.getMDTParameterDescriptorAll();
		String json = MDTModelSerDe.JSON_SERIALIZER.writeList(descList);
		System.out.println(json);
	}

	@Override
	protected Node toMDTModelNode(MDTInstance instance) throws IOException {
		m_manager = (HttpMDTInstanceManager)instance.getInstanceManager();
		
		 // parameter nodes
		if ( m_paramNodes == null ) {
			m_parameters = instance.getMDTParameterDescriptorAll();
			m_paramNodes = new MDTParameterDescriptorListNode(instance.getId(), m_parameters);
		}
		
		return m_paramNodes;
	}
	
	private class MDTParameterDescriptorListNode extends ListNode {
		private List<ParameterNode> m_paramNodes;
		
		public MDTParameterDescriptorListNode(String instId, List<MDTParameterDescriptor> paramDescList) {
			setTitle("Parameters");
			setRepeatable(true);
			
			m_paramNodes = FStream.from(paramDescList)
									.map(pdesc -> {
										MDTElementReference ref = ElementReferences.parseExpr(pdesc.getReference());
										return create(pdesc, ref);
									})
									.toList();
		}
		
		@Override
		protected List<? extends DefaultNode> getElementNodes() {
			return m_paramNodes;
		}
		
		public ParameterNode create(MDTParameterDescriptor pdesc, MDTElementReference ref)  {
			ParameterNode node = new ParameterNode(pdesc, ref);
			node.setTitle(pdesc.getId());
			node.setValueType(String.format(" (%s)", pdesc.getValueType()));
			node.setHideValue(true);
			
			return node;
		}
	}
	
	private class ParameterNode extends DefaultNode {
		private final MDTParameterDescriptor m_pdesc;
		private final MDTElementReference m_ref;
		
		public ParameterNode(MDTParameterDescriptor pdesc, MDTElementReference ref) {
			m_pdesc = pdesc;
			
			setTitle(pdesc.getId());
			setValueType(String.format(" (%s)", pdesc.getValueType()));
			setHideValue(true);
			
			m_ref = ref;
		}
		
		@Override
		public List<? extends Node> getChildren() {
			TerminalNode nameNode = new TerminalNode("name", "", m_pdesc.getName());
			TerminalNode refNode = new TerminalNode("reference", "", m_pdesc.getReference());
			TerminalNode epNode = new TerminalNode("endpoint", "", m_pdesc.getEndpoint());
			try {
				if ( !m_ref.isActivated() ) {
					m_ref.activate(m_manager);
				}
				DefaultNode valueNode = DefaultNodeFactories.create(m_ref.read());
				valueNode.setTitle("value");
				return List.of(nameNode, refNode, epNode, valueNode);
			}
			catch ( Throwable e ) {
				return List.of(refNode);
			}
		}
	}
}
