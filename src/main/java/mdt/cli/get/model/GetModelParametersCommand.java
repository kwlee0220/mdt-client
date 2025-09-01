package mdt.cli.get.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTParameterDescriptor;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.DefaultNodeFactories;
import mdt.tree.node.ListNode;

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
public class GetModelParametersCommand extends AbstractGetMDTModelEntityCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetModelParametersCommand.class);
	
	private List<MDTParameterDescriptor> m_parameters;
	private MDTParameterDescriptorListNode m_paramNodes;

	public static final void main(String... args) throws Exception {
		main(new GetModelParametersCommand(), args);
	}
	
	public GetModelParametersCommand() {
		setLogger(s_logger);
	}

	@Override
	protected Node toMDTModelNode(MDTInstance instance) throws IOException {
		if ( m_paramNodes == null ) {
			m_parameters = instance.getMDTParameterDescriptorAll();
			m_paramNodes = new MDTParameterDescriptorListNode((HttpMDTInstanceManager)instance.getInstanceManager(),
																instance.getId(), m_parameters);
		}
		else {
			
		}
		
		return m_paramNodes;
	}
	
	private class MDTParameterDescriptorListNode extends ListNode {
		protected List<MDTParameterDescriptor> m_paramInfoList;
		private List<MDTElementReference> m_paramRefs;
		private List<ReferenceNode> m_paramNodes;
		
		public MDTParameterDescriptorListNode(HttpMDTInstanceManager manager, String instId, List<MDTParameterDescriptor> paramInfoList) {
			m_paramInfoList = paramInfoList;
			m_paramRefs = FStream.from(paramInfoList)
								.map(info -> ElementReferences.parseExpr(info.getReference()))
								.peek(ref -> ref.activate(manager))
								.toList();
			m_paramNodes = FStream.from(m_paramInfoList)
								.zipWith(FStream.from(m_paramRefs))
								.mapOrThrow(tup -> create(tup._1, tup._2))
								.toList();
			
			setTitle("Parameters");
			setRepeatable(true);
		}
		
		@Override
		protected List<? extends DefaultNode> getElementNodes() {
			return FStream.from(m_paramInfoList)
							.zipWith(FStream.from(m_paramRefs))
							.mapOrThrow(tup -> create(tup._1, tup._2))
							.toList();
		}
		
		public static ReferenceNode create(MDTParameterDescriptor parameter, MDTElementReference ref)  {
			ReferenceNode node = new ReferenceNode(ref);
			node.setTitle(parameter.getId());
			node.setValueType(String.format(" (%s)", parameter.getValueType()));
			
			return node;
		}
	}
	
	private static class ReferenceNode extends DefaultNode {
		private final MDTElementReference m_ref;
		
		public ReferenceNode(MDTElementReference ref) {
			m_ref = ref;
		}
		
		@Override
		public Iterable<? extends Node> getChildren() {
			try {
				DefaultNode valueNode = DefaultNodeFactories.create(m_ref.read());
				return valueNode.getChildren();
			}
			catch ( IOException e ) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
