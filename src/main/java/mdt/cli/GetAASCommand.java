package mdt.cli;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.DescriptorUtils;
import mdt.model.instance.MDTInstance;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.service.AssetAdministrationShellService;
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
@Command(name = "aas", description = "get AssetAdministrationShell information.")
public class GetAASCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetAASCommand.class);

	@Parameters(index="0", arity="1", paramLabel="id", description="AssetAdministrationShell id to show")
	private String m_aasId = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'table', 'json' or 'env')")
	private String m_output = "table";
	
	public GetAASCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mgr = this.createMDTInstanceManager(configs);
		
		HttpMDTInstanceClient instance = null;
		try {
			instance = mgr.getInstanceByAasId(m_aasId);
		}
		catch ( ResourceNotFoundException expected ) {
			List<MDTInstance> instList = mgr.getAllInstancesByAasIdShort(m_aasId);
			if ( instList.size() == 1 ) {
				instance = (HttpMDTInstanceClient)instList.get(0);
			}
			else  {
				throw new ResourceNotFoundException("AssetAdministrationShell", "aasId=" + m_aasId);
			}
		}
		
//		AssetAdministrationShellService aasSvc = new HttpAASServiceClient(instance.getHttpClient(),
//					"https://localhost:10501/api/v3.0/shells/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvYWFzL-uCtO2VqOqwgOyhsOumvQ==");
		AssetAdministrationShellService aasSvc = instance.getAssetAdministrationShellService();
		AssetAdministrationShell aas = aasSvc.getAssetAdministrationShell();
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsSimple(aas, instance);
		}
		else if ( m_output.equalsIgnoreCase("json") ) {
			displayAsJson(aas);
		}
		else if ( m_output.equalsIgnoreCase("env") ) {
			displayEnvironment(instance, aas);
		}
		else {
			System.err.println("Unknown output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		GetAASCommand cmd = new GetAASCommand();

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
	
	private void displayAsJson(AssetAdministrationShell aas) throws SerializationException {
		JsonSerializer ser = new JsonSerializer();
		System.out.println(ser.write(aas));
	}
	
	private void displayEnvironment(HttpMDTInstanceClient instance, AssetAdministrationShell aas)
		throws SerializationException {	
		List<Submodel> submodels = FStream.from(instance.getAllSubmodelServices())
											.map(SubmodelService::getSubmodel)
											.toList();
		Environment env = new DefaultEnvironment.Builder()
								.assetAdministrationShells(aas)
								.submodels(submodels)
								.build();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(env);
		System.out.println(jsonStr);
	}
	
	private void displayAsSimple(AssetAdministrationShell aas, HttpMDTInstanceClient instance) {
		Table table = new Table(2);

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + aas.getId());
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(aas.getIdShort()) + " ");
		
		table.addCell(" GLOBAL_ASSET_ID ");
			table.addCell(" " + getOrEmpty(aas.getAssetInformation().getGlobalAssetId()) + " ");
		table.addCell(" INSTANCE "); table.addCell(" " + instance.getId());
		
		List<LangStringNameType> names = aas.getDisplayName();
		if ( names != null ) {
			String displayName = FStream.from(names)
										.map(name -> name.getText())
										.join(". ");
			table.addCell(" DISPLAY_NAME "); table.addCell(" " + displayName);
		}
		else {
			table.addCell(" DISPLAY_NAME "); table.addCell("");
		}
		
		FStream.from(aas.getSubmodels())
				.flatMapIterable(Reference::getKeys)
				.map(Key::getValue)
				.zipWithIndex()
				.forEach(tup -> {
					table.addCell(String.format(" SUB_MODEL_REF_[%02d] ", tup.index()));
					table.addCell(" " + tup.value() + " ");
				});
		String aasEp = DescriptorUtils.toAASServiceEndpointString(instance.getEndpoint(), aas.getId());
		table.addCell(" ENDPOINT "); table.addCell(" " + getOrEmpty(aasEp));
		
		System.out.println(table.render());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
