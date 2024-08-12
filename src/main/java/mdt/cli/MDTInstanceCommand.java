package mdt.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.MDTClientConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "node_tracks_index",
		description = "format node-tracks-index table.")
public class MDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTInstanceCommand.class); 
	
	private static final String DEFAULT_INDEX_TABLE_NAME = "node_tracks_index";
	
	@Option(names={"--table-name"}, paramLabel="name", defaultValue=DEFAULT_INDEX_TABLE_NAME,
			description="table name")
	private String m_tableName;
	
	public MDTInstanceCommand() {
		setLogger(s_logger);
	}
	
	@Override
	public void run(MDTClientConfig configs) throws Exception {
	}
	
	public static final void main(String... args) throws Exception {
		MDTInstanceCommand cmd = new MDTInstanceCommand();
		
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
}
