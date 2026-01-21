package mdt.cli.get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.SubmodelBasedElementReference;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "element",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get SubmodelElement information."
)
public class GetElementCommand extends AbstractGetElementCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetElementCommand.class);
	
	@Parameters(index="0", paramLabel="instance", description="MDTInstance id to show.")
	private String m_instanceId;
	
	@Parameters(index="1", paramLabel="submodel", defaultValue="*", description="Target submodel idShort")
	private String m_smIdShort;
	
	@Parameters(index="2", paramLabel="path", defaultValue="*", description="Target SubmodelElement idShortPath")
	private String m_path;

	public static final void main(String... args) throws Exception {
		main(new GetElementCommand(), args);
	}
	
	public GetElementCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		String elmRef = String.format("%s:%s:%s", m_instanceId, m_smIdShort, m_path);
		ElementReference smeRef = ElementReferences.parseExpr(elmRef);
		
		((SubmodelBasedElementReference)smeRef).activate(manager);
		
		run(manager, smeRef);
	}
}
