package mdt.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTManager;
import mdt.model.sm.ref.ResolvedElementReference;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "reference",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Resolve ElementReference."
)
public class ResolveElementReferenceCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ResolveElementReferenceCommand.class);

	@Parameters(index="0", arity="1", paramLabel="element-ref", description="Target element reference")
	private String m_elmRef = null;

	@Option(names={"--output", "-o"}, paramLabel="type", defaultValue="all",
			description="output type (candidnates: all, url (requestUrl), idShortPath, or submodelId)")
	private String m_output = "all";

	public static final void main(String... args) throws Exception {
		main(new ResolveElementReferenceCommand(), args);
	}
	
	public ResolveElementReferenceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		ResolvedElementReference resolved = manager.resolveElementReference(m_elmRef);
		String output = switch ( m_output ) {
			case "url" -> resolved.getRequestUrl();
			case "requestUrl" -> resolved.getRequestUrl();
			case "all" -> new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resolved);
			case "idShortPath" -> resolved.getElementPath();
			case "submodelId" -> resolved.getSubmodelId();
			default -> throw new IllegalArgumentException("Unknown output type: " + m_output);
		};
		System.out.println(output);
	}
}
