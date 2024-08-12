package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.UnitUtils;
import utils.func.KeyValue;
import utils.stream.FStream;

import mdt.cli.MDTCommand;
import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.instance.MDTInstanceManager;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTTaskCommand<T extends MDTTask> extends MDTCommand {
	protected MDTInstanceManager m_manager;
	
	abstract protected T newTask();
	
	protected Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\"")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}
	
	@Unmatched()
	private List<String> m_unmatcheds = Lists.newArrayList();

	@Override
	public void run(MDTClientConfig configs) {
		m_manager = (HttpMDTInstanceManagerClient)createMDTInstanceManager(configs);
		
		// 모든 port 및 option 정보는 unmatcheds에 포함되어 있다.
		Map<String,String> unmatchedOptions = FStream.from(m_unmatcheds)
													.buffer(2, 2)
													.toMap(b -> trimHeadingDashes(b.get(0)), b -> b.get(1));
		
		Map<String,Port> inputPorts = Maps.newHashMap();
		Map<String,Port> inoutPorts = Maps.newHashMap();
		Map<String,Port> outputPorts = Maps.newHashMap();
		FStream.from(unmatchedOptions)
				.filter(kv -> Port.isPortOptionName(kv.key()))
				.map(kv -> Port.from(m_manager, kv.key(), kv.value()))
				.forEach(port -> {
					if ( port.isInputPort() ) {
						inputPorts.put(port.getName(), port);
					}
					else if ( port.isOutputPort() ) {
						outputPorts.put(port.getName(), port);
					}
					else if ( port.isInoutPort() ) {
						inoutPorts.put(port.getName(), port);
					}
				});
		
		Map<String,String> options = FStream.from(unmatchedOptions)
											.filter(kv -> !Port.isPortOptionName(kv.key()))
											.toMap(KeyValue::key, KeyValue::value);
		
		T task = newTask();
		task.setMDTInstanceManager(m_manager);
		
		try {
			task.run(inputPorts, inoutPorts, outputPorts, options);
		}
		catch ( CancellationException e ) {
			System.err.println("MDTTask cancelled: " + e.getMessage());
		}
		catch ( TimeoutException e ) {
			System.err.println("MDTTask cancelled due to timeout: " + e.getMessage());
		}
		catch ( InterruptedException e ) {
			System.err.println("MDTTask execution is interrupted: " + e.getMessage());
		}
		catch ( ExecutionException e ) {
			System.err.println("MDTTask execution failed: " + e.getCause());
		}
	}
	
	private String trimHeadingDashes(String optName) {
		if ( optName.startsWith("--") ) {
			return optName.substring(2);
		}
		else if ( optName.startsWith("-") ) {
			return optName.substring(1);
		}
		else {
			throw new IllegalArgumentException("Invalid option name: " + optName);
		}
	}

	@SuppressWarnings("deprecation")
	protected static final void main(MDTTaskCommand<?> task, String... args) throws Exception {
		CommandLine commandLine = new CommandLine(task).setUsageHelpWidth(100);
		commandLine = commandLine.setStopAtUnmatched(true)
								.setUnmatchedArgumentsAllowed(true)
								.setUnmatchedOptionsArePositionalParams(true);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				task.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
}
