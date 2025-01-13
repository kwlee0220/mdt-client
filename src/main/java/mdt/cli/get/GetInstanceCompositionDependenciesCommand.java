package mdt.cli.get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.service.SubmodelService;
import mdt.model.sm.info.DefaultInformationModel;
import mdt.model.sm.info.InformationModel;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "dependencies",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get composition items of an instance"
)
public class GetInstanceCompositionDependenciesCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceCompositionDependenciesCommand.class);
	
	@ParentCommand GetInstanceCommand m_parent;

	@Option(names={"--type"}, paramLabel="dependency-type",
					description="CompositionDependency type")
	private String m_type;

	public static final void main(String... args) throws Exception {
		main(new GetInstanceCompositionDependenciesCommand(), args);
	}
	
	public GetInstanceCompositionDependenciesCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		HttpMDTInstanceClient instance = client.getInstance(m_parent.getInstanceId());
		
		SubmodelService infoSvc = Funcs.getFirstOrNull(instance.getAllSubmodelServiceBySemanticId(InformationModel.SEMANTIC_ID));
		if ( infoSvc == null ) {
			System.out.println("InformationModel is not found");
			System.exit(0);
		}
		
		DefaultInformationModel info = new DefaultInformationModel();
		info.updateFromAasModel(infoSvc.getSubmodel());
		
		var depStream = FStream.from(info.getTwinComposition().getCompositionDependencies());
		if ( m_type != null ) {
			depStream = depStream.filter(cd -> m_type.equals(cd.getDependencyType()));
		}
		depStream.groupByKey(cd -> cd.getDependencyType())
				.stream()
				.forEach(kv -> {
					FStream.from(kv.value())
							.forEach(dep -> System.out.printf("%s: %s -> %s%n", 
															kv.key(), dep.getSource(), dep.getTarget()));
				});
	}
}
