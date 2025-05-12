package mdt.cli.list;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyle;
import org.barfuin.texttree.api.style.TreeStyles;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

import utils.InternalException;
import utils.StopWatch;
import utils.UnitUtils;
import utils.Utilities;
import utils.func.FOption;
import utils.http.RESTfulIOException;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.cli.list.Nodes.InstanceNode;
import mdt.cli.list.Nodes.RootNode;
import mdt.client.instance.HttpMDTInstance;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.MDTModelService;
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
public class ListMDTInstanceCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListMDTInstanceCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'csv' (default), 'table', 'tree', or 'json')")
	private String m_output = "csv";
	
	@Option(names={"--table", "-t"}, description="'table' format output (same to '--output=table')")
	public void setTable(boolean flag) {
		m_output = "table";
	}

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\")")
	private String m_repeat = null;

	@Option(names={"--long", "-l"}, description="show detailed information. valid only for 'simple' and 'table' output")
	private boolean m_long = false;

	@Option(names={"--delimiter", "-d"}, paramLabel="delimiter",
					description="delimiter (for 'csv' output only)")
	private String m_delimiter = ListCommands.DELIM;

	@Option(names={"--pretty"}, description="pretty print (for 'json' output only)")
	private boolean m_prettyPrint = false;
	
	@Option(names={"--show-endpoint"}, description="show endpoint for running MDT instances. valid only for 'tree' output")
	private boolean m_showEndpoint = false;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	public ListMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos) ) {
				List<? extends MDTInstance> instances = (m_filter != null)
														? manager.getInstanceAllByFilter(m_filter)
														: manager.getInstanceAll();
				
                if ( m_long ) {
					if ( m_output.equals("csv") ) {
						printLongCsv(instances, pw);
					}
					else if ( m_output.equals("table") ) {
                        printLongTable(instances, pw);
                    }
					else if ( m_output.equals("tree") ) {
						printTree(instances, pw);
					}
					else {
						printJson(instances, pw);
					}
                }
                else {
					if ( m_output.equals("csv") ) {
						printShortCsv(instances, pw);
					}
					else if ( m_output.equals("table") ) {
                        printShortTable(instances, pw);
                    }
					else if ( m_output.equals("tree") ) {
						printTree(instances, pw);
					}
                    else {
						printJson(instances, pw);
                    }
                }
				pw.flush();
				String outputString = baos.toString();
				if ( repeatInterval == null ) {
					System.out.print(outputString);
					break;
				}
				else {
					System.out.print(CLEAR_CONSOLE_CONTROL);
					System.out.print(outputString);
				}
				if ( m_verbose ) {
					System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
				}
			}
			catch ( RESTfulIOException e ) {
				System.out.println("fails to list MDTInstances: " + e.getCause());
			}
			
			if ( repeatInterval == null ) {
				break;
			}
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}
	
	private void printShortCsv(List<? extends MDTInstance> instances, PrintWriter pw) {
		int seqNo = 1;
		for ( MDTInstance inst : instances ) {
			pw.append(""+seqNo).append(m_delimiter);
			pw.append(inst.getId()).append(m_delimiter);
			pw.append(inst.getStatus().toString());
			pw.println();
			++seqNo;
		}
	}
	
	private void printShortTable(List<? extends MDTInstance> instances, PrintWriter pw) {
		Table table = new Table(3);
		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" STATUS ");
		
		int seqNo = 1;
		for ( MDTInstance inst : instances ) {
			table.addCell(String.format("%3d", seqNo));
			table.addCell(inst.getId());
			table.addCell(""+inst.getStatus());
			++seqNo;
		}
		pw.println(table.render());
	}
	
	private void printLongCsv(List<? extends MDTInstance> instances, PrintWriter pw) {
		int seqNo = 1;
		for ( MDTInstance inst : instances ) {
			Object[] cells = toLongColumns(seqNo, inst, "%d");
			String line = FStream.of(cells).map(Object::toString).join(m_delimiter);
			pw.println(line);
			++seqNo;
		}
	}
	
	private void printLongTable(List<? extends MDTInstance> instances, PrintWriter pw) {
		Table table = new Table(7);
		table.setColumnWidth(4, 10, 35);
		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" ASSET_TYPE ");
		table.addCell(" ASSET_ID ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" STATUS ");
		table.addCell(" ENDPOINT ");
		
		int seqNo = 1;
		for ( MDTInstance inst : instances ) {
			Object[] cells = toLongColumns(seqNo, inst, "%3d");
			FStream.of(cells).map(Object::toString).forEach(table::addCell);
			++seqNo;
		}
		pw.println(table.render());
	}

	public void printJson(List<? extends MDTInstance> instances, PrintWriter pw) {
		ArrayNode array = MDTModelSerDe.MAPPER.createArrayNode();
		try {
			FStream.from(instances)
			        .mapOrThrow(inst -> MDTModelService.of(inst).toJsonNode())
			        .collect(array, ArrayNode::add);
			
			String json;
			if ( m_prettyPrint ) {
				json = MDTModelSerDe.MAPPER.writerWithDefaultPrettyPrinter()
											.writeValueAsString(array) + System.lineSeparator();
			}
			else {
				json = MDTModelSerDe.MAPPER.writeValueAsString(array) + System.lineSeparator();
			}
			pw.println(json);
		}
		catch ( Exception e ) {
			throw new InternalException("fails to serialize MDTInstances to JSON, cause=" + e);
		}
    }

	private void printTree(List<? extends MDTInstance> instances, PrintWriter pw) {
		Nodes.s_showEndpoint = m_showEndpoint;
		
		// take a snapshot
		Map<String,InstanceNode> nodes = FStream.from(instances)
												.castSafely(HttpMDTInstance.class)
												.map(InstanceNode::new)
												.tagKey(InstanceNode::getId)
												.toMap();

		// 초기 구조를 구축한다.
		RootNode root = new RootNode();
		List<InstanceNode> runningNodes = Lists.newArrayList();
		for ( InstanceNode node: nodes.values() ) {
			if ( node.getStatus() == MDTInstanceStatus.RUNNING ) {
				runningNodes.add(node);
			}
			root.addChild(node);
		}
		
		FStream.from(runningNodes)
				.forEach(node -> {
					for ( HttpMDTInstance comp: node.getInstance().getTargetOfDependency("contain") ) {
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
		pw.println(TextTree.newInstance(opts).render(root));
	}

	private Object[] toLongColumns(int seqNo, MDTInstance instance, String format) {
		List<String> outputs = Lists.newArrayList();
		FStream.from(instance.getInstanceSubmodelDescriptorAll())
				.map(isdesc -> SubmodelUtils.getShortSubmodelSemanticId(isdesc.getSemanticId()))
				.tagKey(n -> n)
				.groupByKey()
				.switcher()
				.ifCase("Info").consume(grp -> outputs.add("Info"))
				.ifCase("Data").consume(grp -> outputs.add("Data"))
				.otherwise().forEach((k, grp) -> outputs.add(String.format("%s(%d)", k, grp.size())));
		
		String submodelIdCsv = FStream.from(outputs).join(',');
		
		String serviceEndpoint = ObjectUtils.defaultIfNull(instance.getBaseEndpoint(), "");
		return new Object[] {
			String.format(format, seqNo),
			instance.getId(),
			FOption.getOrElse(instance.getAssetType(), ""),
			FOption.getOrElse(instance.getGlobalAssetId(), ""),
			submodelIdCsv,
			instance.getStatus(),
			serviceEndpoint
		};
	}
}
