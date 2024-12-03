package mdt.cli.get;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.service.SubmodelService;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.info.DefaultInformationModel;
import mdt.model.sm.simulation.DefaultSimulation;
import mdt.tree.MDTInstanceNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "ksx9101",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get KSX-9101 Properties information."
)
public class GetKSX9101Command extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetKSX9101Command.class);

	@Parameters(index="0", paramLabel="id", description="MDTInstance id")
	private String m_mdtId;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;

	public static final void main(String... args) throws Exception {
		main(new GetKSX9101Command(), args);
	}
	
	public GetKSX9101Command() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				HttpMDTInstanceClient inst = client.getInstance(m_mdtId);
				
				// 획득한 MDTInstance가 KSX9101를 지원하는지 간략히 확인.
				// 확인은 MDTInstance가 'InformationModel'과 'Data' idShort를 갖는
				// Submodel을 포함하는지 여부로 판단한다.
				Set<String> ids = FStream.from(inst.getAllInstanceSubmodelDescriptors())
										.map(InstanceSubmodelDescriptor::getIdShort)
										.toSet();
				if ( !ids.containsAll(Set.of("InformationModel", "Data")) ) {
					throw new IllegalArgumentException("Not KSX9101 MDTInstance: id=" + m_mdtId);
				}
				
				Map<String,Submodel> submodels = FStream.from(inst.getAllSubmodelServices())
														.map(SubmodelService::getSubmodel)
														.toMap(Submodel::getIdShort);
				
				Submodel submodel;
				MDTInstanceNode.Builder builder = MDTInstanceNode.builder().mdtId(m_mdtId);
				
				submodel = submodels.get("InformationModel");
				if ( submodel != null ) {
					DefaultInformationModel info = DefaultInformationModel.from(submodel);
					builder = builder.informationModelSubmodel(info);
				}
				submodel = submodels.get("Data");
				if ( submodel != null ) {
					builder = builder.dataSubmodel(DefaultData.from(submodel));
				}
				submodel = submodels.get("Simulation");
				if ( submodel != null ) {
					DefaultSimulation simulation = DefaultSimulation.from(submodel);
					builder = builder.simulationSubmodel(simulation);
				}
				MDTInstanceNode root = builder.build();
				
				System.out.print("\033[2J\033[1;1H");
				System.out.print(TextTree.newInstance(opts).render(root));
			}
			catch ( Throwable e ) {
				System.out.print("\033[2J\033[1;1H");
				System.out.println("" + e);
			}
			System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			
			if ( repeatInterval == null ) {
				break;
			}

			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !remains.isNegative() && !remains.isZero() ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}
}
