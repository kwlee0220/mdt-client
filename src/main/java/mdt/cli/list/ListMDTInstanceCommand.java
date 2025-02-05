package mdt.cli.list;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyle;
import org.barfuin.texttree.api.style.TreeStyles;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.Utilities;
import utils.stream.FStream;

import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.SimpleListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.cli.list.Nodes.InstanceNode;
import mdt.cli.list.Nodes.RootNode;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.sm.SubmodelUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "instances",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all MDTInstances."
)
public class ListMDTInstanceCommand extends AbstractListCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListMDTInstanceCommand.class);

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;

	@Option(names={"--long", "-l"}, description="show detailed information")
	private boolean m_long = false;
	
	@Option(names={"--show-endpoint"}, description="show endpoint for running MDT instances")
	private boolean m_showEndpoint = false;
	
	public ListMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public String buildListString(String delim) {
		return collect(new SimpleListCollector(delim));
	}

	@Override
	public String buildTreeString() {
		Nodes.s_showEndpoint = m_showEndpoint;
		
		List<? extends MDTInstance> instances = (m_filter != null)
												? getMDTInstanceManager().getInstanceAllByFilter(m_filter)
												: getMDTInstanceManager().getInstanceAll();
		
		// take a snapshot
		Map<String,InstanceNode> nodes = FStream.from(instances)
												.castSafely(HttpMDTInstanceClient.class)
												.map(InstanceNode::new)
												.toMap(InstanceNode::getId);

		// 초기 구조를 구축한다.
		RootNode root = new RootNode();
		List<InstanceNode> runningNodes = Lists.newArrayList();
		for ( InstanceNode node: nodes.values() ) {
			if ( node.getStatus() == MDTInstanceStatus.RUNNING ) {
				runningNodes.add(node);
			}
			root.addChild(node);
		}
//		for ( StatusGroupNode group: root.m_statusGroupNodes.values() ) {
//			System.out.println(group);
//		}
//		System.out.println("*********************************************");
		
		FStream.from(runningNodes)
				.forEach(node -> {
					for ( HttpMDTInstanceClient comp: node.getInstance().getAllTargetInstances("contain") ) {
						InstanceNode depNode = nodes.get(comp.getId());
						if ( depNode != null && depNode.getStatus() == MDTInstanceStatus.RUNNING ) {
							node.addChild(depNode);
							root.removeChild(depNode);
						}
					}
				});

		TreeOptions opts = new TreeOptions();
		TreeStyle style = Utilities.isWindowsOS() ? TreeStyles.WIN_TREE : TreeStyles.UNICODE_ROUNDED;
		opts.setStyle(style);
		opts.setMaxDepth(5);
		return TextTree.newInstance(opts).render(root);
	}

	@Override
	public String buildTableString() {
		return m_long ? buildLongTableString() : buildShortTableString();
	}
	public String buildShortTableString() {
		Table table = new Table(3);

		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" STATUS ");
		
		return collect(new TableCollector(table));
	}
	public String buildLongTableString() {
		Table table = new Table(6);
		table.setColumnWidth(2, 20, 70);
		table.setColumnWidth(3, 10, 35);

		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_ID ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" STATUS ");
		table.addCell(" ENDPOINT ");
		
		return collect(new TableCollector(table));
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									? getMDTInstanceManager().getInstanceAllByFilter(m_filter)
									: getMDTInstanceManager().getInstanceAll();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			collector.collectLine(toColumns(seqNo, inst));
			++seqNo;
		}
		
		return collector.getFinalString();
	}

	private Object[] toColumns(int seqNo, MDTInstance instance) {
		return m_long ? toLongColumns(seqNo, instance) : toShortColumns(seqNo, instance);
	}

	private Object[] toShortColumns(int seqNo, MDTInstance instance) {
		return new Object[] {
			String.format("%3d", seqNo),
			instance.getId(),
			instance.getStatus()
		};
	}

	private Object[] toLongColumns(int seqNo, MDTInstance instance) {
		List<String> outputs = Lists.newArrayList();
		FStream.from(instance.getInstanceSubmodelDescriptorAll())
				.map(isdesc -> SubmodelUtils.getShortSubmodelSemanticId(isdesc.getSemanticId()))
				.groupByKey(n -> n)
				.switcher()
				.ifCase("Info").consume(grp -> outputs.add("Info"))
				.ifCase("Data").consume(grp -> outputs.add("Data"))
				.otherwise().forEach((k, grp) -> outputs.add(String.format("%s(%d)", k, grp.size())));
		
		String submodelIdCsv = FStream.from(outputs).join(',');
		
		String serviceEndpoint = ObjectUtils.defaultIfNull(instance.getBaseEndpoint(), "");
		return new Object[] {
			String.format("%3d", seqNo),
			instance.getId(),
			instance.getAasId(),
			submodelIdCsv,
			instance.getStatus(),
			serviceEndpoint
		};
	}
}
