package mdt.cli.get.model;

import java.io.IOException;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.model.AASUtils;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTSubmodelDescriptor;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.TerminalNode;

import picocli.CommandLine.Command;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "submodels",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get submodel informations for the MDTInstance."
)
public class GetModelSubmodelCommand extends AbstractGetMDTModelEntityCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetModelSubmodelCommand.class);

	public static final void main(String... args) throws Exception {
		main(new GetModelSubmodelCommand(), args);
	}
	
	public GetModelSubmodelCommand() {
		setLogger(s_logger);
	}

	@Override
	protected Node toMDTModelNode(MDTInstance instance) throws IOException {
		return new SubmodelDescriptorNode(instance.getMDTSubmodelDescriptorAll());
	}
	
	private static class SubmodelDescriptorNode extends DefaultNode {
		private List<? extends DefaultNode> m_children;

		public SubmodelDescriptorNode(List<MDTSubmodelDescriptor> submodels) {
			setTitle("Submodels");
			
			m_children = FStream.from(submodels)
								.map(SubmodelInfoNode::new)
								.toList();
		}

		@Override
		public Iterable<? extends Node> getChildren() {
			return m_children;
		}
		
		private static class SubmodelInfoNode extends DefaultNode {
			private List<? extends DefaultNode> m_children;

			public SubmodelInfoNode(MDTSubmodelDescriptor sm) {
				setTitle(sm.getIdShort());
				
				String smIdEncoded = AASUtils.encodeBase64UrlSafe(sm.getId());
				m_children = List.of(new TerminalNode("id", "", sm.getId()),
									new TerminalNode("idEncoded", "", smIdEncoded),
									new TerminalNode("semanticId", "", sm.getSemanticId()));
			}

			@Override
			public Iterable<? extends Node> getChildren() {
				return m_children;
			}
		}
	}
}
