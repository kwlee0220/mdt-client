package mdt.cli.get;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.UnitUtils;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.cli.PeriodicRefreshingConsole;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTManager;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.Simulation;
import mdt.tree.node.DefaultNode;
import mdt.tree.node.DefaultNodeFactories;
import mdt.tree.node.data.ParameterCollectionNode;
import mdt.tree.node.info.MDTInfoNode;
import mdt.tree.node.op.OperationEntityNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "mdt-info",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get MDT model information for the instance."
)
public class GetMdtInfoCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetMdtInfoCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="MDTInstance id to show.")
	private String m_instanceId;
	
	@Option(names={"--info", "-i"}, description="show MDTInfo")
	private boolean m_info = false;
	
	@Option(names={"--parameters", "-p"}, description="show parameters")
	private boolean m_parameters = false;
	
	@Option(names={"--operations", "-o"}, description="show operations (AI or Simulation)")
	private boolean m_operations = false;
	
	@Option(names={"--compositions", "-c"}, description="show compositions")
	private boolean m_compositions = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new GetMdtInfoCommand(), args);
	}
	
	public GetMdtInfoCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		HttpMDTInstanceClient instance = manager.getInstance(m_instanceId);
		
		if ( m_repeat == null ) {
			try ( PrintWriter pw = new PrintWriter(System.out, true) ) {
				printOutput(instance, pw);
			}
			return;
		}

		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		PeriodicRefreshingConsole pwriter = new PeriodicRefreshingConsole(repeatInterval) {
			@Override
			protected void print(PrintWriter pw) throws Exception {
				try {
					MDTInstance instance = manager.getInstance(m_instanceId);
					printOutput(instance, pw);
				}
				catch ( InvalidResourceStatusException expected ) {
					pw.println("instance is not running: id=" + instance.getId());
				}
				catch ( Exception e ) {
					pw.printf("failed to get MDT model info: instance=%s, cause=%s%n", instance.getId(), e);
				}
			}
		};
		pwriter.setVerbose(m_verbose);
		pwriter.run();
	}

	private static final TreeOptions TREE_OPTS = new TreeOptions();
	static {
		TREE_OPTS.setStyle(TreeStyles.UNICODE_ROUNDED);
		TREE_OPTS.setMaxDepth(5);
	}
	
	private void printOutput(MDTInstance instance, PrintWriter pw) throws IOException {
		List<DefaultNode> nodes = Lists.newArrayList();
		if ( m_info ) {
			nodes.add(buildInfoNode(instance));
		}
		if ( m_parameters ) {
			nodes.add(buildParametersNode(instance));
		}
		if ( m_operations ) {
			nodes.add(buildOperationsNode(instance));
		}
		if ( m_compositions ) {
			FOption.accept(buildCompositionItemsNode(instance), nodes::add);
			FOption.accept(buildCompositionDependenciesNode(instance), nodes::add);
		}

		if ( nodes.size() > 0 ) {
			DefaultNode root = new DefaultNode("MDTInstance: " + instance.getId(), null, "") {
				@Override
				public Iterable<? extends Node> getChildren() {
					return nodes;
				}
			};
			root.setHideValue(true);
			String treeString = TextTree.newInstance(TREE_OPTS).render(root);
			pw.print(treeString);
		}
	}
	
	private DefaultNode buildInfoNode(MDTInstance instance) {
		SubmodelService infoSvc = FStream.from(instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID))
								        .findFirst()
										.getOrThrow(() -> new ResourceNotFoundException("InformationModelService"));
		try {
			SubmodelElement sme = infoSvc.getSubmodelElementByPath("MDTInfo");
			return MDTInfoNode.FACTORY.create(sme);
		}
		catch ( ResourceNotFoundException e ) {
			if ( m_verbose ) {
				System.out.println("No CompositionItems found in InformationModel.");
			}
			
			return null;
		}
	}
	
	private DefaultNode buildParametersNode(MDTInstance instance) {
		ParameterCollection paramColl = instance.getParameterCollection();
		return new ParameterCollectionNode(paramColl);
	}
	
	private DefaultNode buildOperationsNode(MDTInstance instance) {
		List<OperationEntityNode> opNodes = getOperationNodeList(instance, AI.SEMANTIC_ID, "AI");
		opNodes.addAll(getOperationNodeList(instance, Simulation.SEMANTIC_ID, "Simulation"));
		DefaultNode opListNode = new DefaultNode("Operations", null, "") {
			@Override
			public Iterable<? extends Node> getChildren() {
				return opNodes;
			}
		};
		
		return opListNode;
	}
	
	private List<OperationEntityNode> getOperationNodeList(MDTInstance instance, String semanticId, String tag) {
		return FStream.from(instance.getSubmodelServiceAllBySemanticId(semanticId))
						.map(svc -> {
							Submodel sm = svc.getSubmodel();
							SubmodelElement opElm = SubmodelUtils.traverse(sm, tag + "Info");
							OperationEntityNode node = new OperationEntityNode(sm.getIdShort(), opElm);
							node.setValueType(String.format(" (%s)", tag));
							return node;
						})
						.toList();
	}
	
	private DefaultNode buildCompositionItemsNode(MDTInstance instance) {
		SubmodelService infoSvc = FStream.from(instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID))
								        .findFirst()
										.getOrThrow(() -> new ResourceNotFoundException("InformationModelService"));
		try {
			SubmodelElement sme = infoSvc.getSubmodelElementByPath("TwinComposition.CompositionItems");
			return new DefaultNode("Components", null, "") {
				@Override
				public Iterable<? extends Node> getChildren() {
					return FStream.from(((SubmodelElementList)sme).getValue())
							.map(item -> DefaultNodeFactories.create(item))
							.toList();
				}
			};
		}
		catch ( ResourceNotFoundException e ) {
			if ( m_verbose ) {
				System.out.println("No CompositionItems found in InformationModel.");
			}
			
			return null;
		}
	}

	private DefaultNode buildCompositionDependenciesNode(MDTInstance instance) {
		SubmodelService infoSvc = FStream.from(instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID))
								        .findFirst()
										.getOrThrow(() -> new ResourceNotFoundException("InformationModelService"));
		try {
			SubmodelElement sme = infoSvc.getSubmodelElementByPath("TwinComposition.CompositionDependencies");
			return new DefaultNode("Dependencies", null, "") {
				@Override
				public Iterable<? extends Node> getChildren() {
					return FStream.from(((SubmodelElementList)sme).getValue())
							.map(item -> DefaultNodeFactories.create(item))
							.toList();
				}
			};
		}
		catch ( ResourceNotFoundException e ) {
			if ( m_verbose ) {
				System.out.println("No CompositionItems found in InformationModel.");
			}
			
			return null;
		}
	}
}
