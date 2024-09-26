package mdt.cli.instance;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.MDTCommand;
import mdt.cli.tree.SubmodelElementNodeFactory;
import mdt.cli.tree.SubmodelNode;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.AASUtils;
import mdt.model.MDTManager;
import mdt.model.SubmodelUtils;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.resource.value.ElementValues;
import mdt.model.resource.value.SubmodelElementValue;
import mdt.model.service.SubmodelService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "property",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get Submodel's Property information."
)
public class GetPropertyCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetPropertyCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	@Parameters(index="0", arity="1", paramLabel="submodel-element-reference",
				description="Target property reference: <mdt_id>/<submodel_idshort>/<idShortPath>")
	private String m_target = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: tree, json, or value)")
	private String m_output = "tree";

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;

	public static final void main(String... args) throws Exception {
		main(new GetPropertyCommand(), args);
	}
	
	public GetPropertyCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
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
				String outputString = switch ( m_output ) {
					case "tree" -> toDisplayTree(submodel, idShortPath, opts);
					case "json" -> toDisplayJson(submodel, idShortPath);
					case "value" -> toDisplayValue(submodel, idShortPath);
					default -> throw new IllegalArgumentException("Invalid output type: " + m_output);
				};
				
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

	private String toDisplayTree(Submodel submodel, String idShortPath, TreeOptions opts) throws Exception {
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

	private String toDisplayJson(Submodel submodel, String idShortPath) throws Exception {
		if ( idShortPath != null ) {
			SubmodelElement sme = SubmodelUtils.traverse(submodel, idShortPath);
			return AASUtils.writeJson(sme);
		}
		else {
			return AASUtils.writeJson(submodel);
		}
	}

	private String toDisplayValue(Submodel submodel, String idShortPath) throws Exception {
		if ( idShortPath != null ) {
			SubmodelElement sme = SubmodelUtils.traverse(submodel, idShortPath);
			SubmodelElementValue value = ElementValues.getValue(sme);
			return AASUtils.writeJson(value.toJsonObject());
		}
		else {
			Map<String,Object> values = Maps.newHashMap();
			for ( SubmodelElement sme: submodel.getSubmodelElements() ) {
				SubmodelElementValue value = ElementValues.getValue(sme);
				values.put(sme.getIdShort(), value.toJsonObject());
			}
			
			return AASUtils.writeJson(values);
		}
	}
}
