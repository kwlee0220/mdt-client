package mdt.cli.get.model;

import java.io.IOException;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import mdt.model.AASUtils;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.TerminalNode;

import picocli.CommandLine.Command;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "instance",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Display a instance descriptor for the MDTInstance."
)
public class GetModelInstanceCommand extends AbstractGetMDTModelEntityCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetModelInstanceCommand.class);

	public static final void main(String... args) throws Exception {
		main(new GetModelInstanceCommand(), args);
	}
	
	public GetModelInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	protected Node toMDTModelNode(MDTInstance instance) throws IOException {
		return new InstanceDescriptorNode(instance.getInstanceDescriptor());
	}
	
	private class InstanceDescriptorNode extends DefaultNode {
		private List<Node> m_children = Lists.newArrayList();

		public InstanceDescriptorNode(InstanceDescriptor instDesc) {
			setTitle("Instance");
			setValue(instDesc.getId());
			
			String aasIdEncoded = AASUtils.encodeBase64UrlSafe(instDesc.getAasId());

			m_children.add(new TerminalNode("assetType", "", instDesc.getAssetType()));
			m_children.add(new TerminalNode("status", "", instDesc.getStatus()));
			m_children.add(new TerminalNode("aasId", "", instDesc.getAasId()));
			m_children.add(new TerminalNode("aasIdEncoded", "", aasIdEncoded));
			m_children.add(new TerminalNode("aasIdShort", "", instDesc.getAasIdShort()));
			m_children.add(new TerminalNode("assetId", "", instDesc.getGlobalAssetId()));
			m_children.add(new TerminalNode("baseEndpoint", "", instDesc.getBaseEndpoint()));
		}

		@Override
		public Iterable<? extends Node> getChildren() {
			return m_children;
		}
	}
}
