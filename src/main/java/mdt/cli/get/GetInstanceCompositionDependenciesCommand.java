package mdt.cli.get;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstance;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.info.DefaultCompositionDependencies;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;

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
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'simple' or 'json')")
	private String m_output = "simple";

	public static final void main(String... args) throws Exception {
		main(new GetInstanceCompositionDependenciesCommand(), args);
	}
	
	public GetInstanceCompositionDependenciesCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManager client = (HttpMDTInstanceManager)manager.getInstanceManager();
		HttpMDTInstance instance = client.getInstance(m_parent.getInstanceId());

		SubmodelService infoSvc = FStream.from(instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID))
								        .findFirst()
										.getOrThrow(() -> new ResourceNotFoundException("InformationModelService"));
		SubmodelElement sme = infoSvc.getSubmodelElementByPath("TwinComposition.CompositionDependencies");
		if ( sme instanceof SubmodelElementList ) {
			switch ( m_output ) {
				case "simple":
					DefaultCompositionDependencies dependencies = new DefaultCompositionDependencies();
					dependencies.updateFromAasModel(sme);

					var depStream = FStream.from(dependencies.getElementAll());
					if ( m_type != null ) {
						depStream = depStream.filter(cd -> m_type.equals(cd.getDependencyType()));
					}
					depStream.tagKey(cd -> cd.getDependencyType())
							.groupByKey()
							.fstream()
							.forEach(kv -> {
								FStream.from(kv.value())
										.forEach(dep -> System.out.printf("%s: %s -> %s%n", 
																		kv.key(), dep.getSourceId(), dep.getTargetId()));
							});
					break;
				case "json":
					ElementListValue valueList = (ElementListValue)ElementValues.getValue(sme);
					if ( m_type != null ) {
						List<ElementValue> matches
									= FStream.from(valueList.getElementAll())
											.filter(v -> {
												ElementCollectionValue ecv = (ElementCollectionValue)v;
												PropertyValue depType = (PropertyValue)ecv.getField("DependencyType");
												return m_type.equals(depType.get());
											})
											.toList();
						valueList = new ElementListValue(matches);
					}
					System.out.println(MDTModelSerDe.toJsonString(valueList));
					break;
				default:
					System.err.println("Unexpected output type: " + m_output);
					System.exit(-1);
			}
		}
		else {
			System.err.println("Unexpected CompositionItems type: " + sme);
			System.exit(-1);
		}
		
		
		
		
//		SubmodelService infoSvc = Funcs.getFirstOrNull(instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID));
//		if ( infoSvc == null ) {
//			System.out.println("InformationModel is not found");
//			System.exit(0);
//		}
//		
//		DefaultInformationModel info = new DefaultInformationModel();
//		info.updateFromAasModel(infoSvc.getSubmodel());
//		
//		var depStream = FStream.from(info.getTwinComposition().getCompositionDependencies());
//		if ( m_type != null ) {
//			depStream = depStream.filter(cd -> m_type.equals(cd.getDependencyType()));
//		}
//		depStream.groupByKey(cd -> cd.getDependencyType())
//				.stream()
//				.forEach(kv -> {
//					FStream.from(kv.value())
//							.forEach(dep -> System.out.printf("%s: %s -> %s%n", 
//															kv.key(), dep.getSourceId(), dep.getTargetId()));
//				});
	}
}
