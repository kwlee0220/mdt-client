package mdt.cli.get;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.SubmodelElementReference;
import mdt.model.sm.SubmodelElementReferences;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;
import mdt.tree.sm.SubmodelElementNodeFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "element",
	aliases = {"property", "sme"},
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get Submodel's Property information."
)
public class GetSubmodelElementCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetSubmodelElementCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	@Parameters(index="0", arity="1", paramLabel="sme-reference",
				description="Target submodel-element reference: <mdt_id>/<submodel_idshort>/<idShortPath>")
	private String m_target = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: tree, json, or value)")
	private String m_output = "tree";

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;

	public static final void main(String... args) throws Exception {
		main(new GetSubmodelElementCommand(), args);
	}
	
	public GetSubmodelElementCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		
		SubmodelElementReference smeRef = SubmodelElementReferences.parseString(m_target);
		if ( smeRef instanceof MDTInstanceManagerAwareReference aware ) {
			aware.activate(manager);
		}
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();

			try {
				SubmodelElement target = smeRef.read();
				String outputString = switch ( m_output ) {
					case "tree" -> toDisplayTree(target, opts);
					case "json" -> toDisplayJson(target);
					case "value" -> toDisplayValue(target);
					default -> throw new IllegalArgumentException("Invalid output type: " + m_output);
				};
				
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.println(outputString);
				System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			}
			catch ( Exception e ) {
//				System.out.print(CLEAR_CONSOLE_CONTROL);
				System.out.println("" + e);
			}
			
			if ( repeatInterval == null ) {
				break;
			}
			
			long remains = repeatInterval.toMillis() - watch.getElapsedInMillis();
			if ( remains > 50 ) {
				TimeUnit.MILLISECONDS.sleep(repeatInterval.toMillis());
			}
		}
	}

	private String toDisplayTree(SubmodelElement target, TreeOptions opts) throws Exception {
		Node topNode = SubmodelElementNodeFactory.toNode("", target);
		return TextTree.newInstance(opts).render(topNode);
	}

	private String toDisplayJson(SubmodelElement target) throws Exception {
		return MDTModelSerDe.toJsonString(target);
	}

	private String toDisplayValue(SubmodelElement target) throws Exception {
		SubmodelElementValue value = ElementValues.getValue(target);
		return MDTModelSerDe.toJsonString(value);
	}
}
