package mdt.cli;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.tree.SubmodelElementNodeFactory;
import mdt.cli.tree.SubmodelNode;
import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.SubmodelUtils;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.service.SubmodelService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "property", description = "get Submodel's Property information.")
public class GetPropertyCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetPropertyCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	@Parameters(index="0", arity="1", paramLabel="target",
				description="Target property reference: <mdt_id>/<submodel_idshort>/<idShortPath>")
	private String m_target = null;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	public GetPropertyCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient client = this.createMDTInstanceManager(configs);
		
		SubmodelElementReference smeRef = SubmodelElementReference.parseString(client, m_target);
		SubmodelService submodelSvc = smeRef.getSubmodelService();
		String idShortPath = smeRef.getIdShortPath();
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();

			try {
				Submodel submodel = submodelSvc.getSubmodel();
				String outputString = toDisplayString(submodel, idShortPath, opts);
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.println(outputString);
			}
			catch ( Exception e ) {
				System.out.print("\033[2J\033[1;1H");
				System.out.println("" + e);
			}
			System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			
			if ( repeatInterval == null ) {
				break;
			}
			
			long remains = repeatInterval.toMillis() - watch.getElapsedInMillis();
			if ( remains > 50 ) {
				TimeUnit.MILLISECONDS.sleep(repeatInterval.toMillis());
			}
		}
	}

	public static final void main(String... args) throws Exception {
		GetPropertyCommand cmd = new GetPropertyCommand();

		CommandLine commandLine = new CommandLine(cmd).setUsageHelpWidth(100);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				cmd.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}


	private String toDisplayString(Submodel submodel, String idShortPath, TreeOptions opts) throws Exception {
		Node topNode;
		if ( idShortPath != null ) {
			SubmodelElement sme = SubmodelUtils.traverse(submodel, idShortPath);
			topNode = SubmodelElementNodeFactory.toNode("", sme);
		}
		else {
			topNode = new SubmodelNode(submodel);
		}
		
		return TextTree.newInstance(opts).render(topNode);
	}
}
