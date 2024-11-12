package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import utils.KeyedValueList;
import utils.Throwables;
import utils.UnitUtils;
import utils.Utilities;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.cli.MDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.SubmodelElementReference;
import mdt.model.sm.SubmodelElementReferences;

import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MultiParameterTaskCommand<T extends MDTTask> extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(MultiParameterTaskCommand.class);
	
	protected MDTInstanceManager m_manager;
	
	protected Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\"")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}
	
	@Unmatched()
	private List<String> m_unmatcheds = Lists.newArrayList();
	
	protected abstract T newTask(KeyedValueList<String,Parameter> parameters,
								Set<String> outputParameterNames) throws TaskException;
	
	protected MultiParameterTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		m_manager = manager.getInstanceManager();
		
		// 모든 SubmodelElement reference 및 option 정보는 unmatcheds에 포함되어 있다.
		Map<String,String> unmatchedOptions = FStream.from(m_unmatcheds)
													.buffer(2, 2)
													.toMap(b -> trimHeadingDashes(b.get(0)), b -> b.get(1));
		
		KeyedValueList<String,Parameter> parameters = KeyedValueList.newInstance(Parameter::getName);
		Set<String> outputParamNames = Sets.newHashSet();
		
		for ( Entry<String,String> ent: unmatchedOptions.entrySet() ) {
			Tuple<String,String> tup = Utilities.split(ent.getKey(), '.', Tuple.of("in", ent.getKey()));
			String kind = tup._1;
			String varName = tup._2;
			
			SubmodelElementReference ref;
			try {
				ref = SubmodelElementReferences.parseString(ent.getValue());
			}
			catch ( Exception e ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				String msg = String.format("Failed to parse \"%s\" variable %s, ref=%s, cause=%s",
											kind, varName, ent.getValue(), cause);
				throw new IllegalArgumentException(msg);
			}
			switch ( kind.toLowerCase() ) {
				case "in":
					parameters.add(Parameter.of(varName, ref));
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set input parameter[{}]", kind, varName);
					}
					break;
				case "inout":
				case "out":
					parameters.add(Parameter.of(varName, ref));
					outputParamNames.add(varName);
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set inoutput parameter[{}]", kind, varName);
					}
					break;
			}
		}

		// 정의된 모든 port들을 활성화시킨다.
		FStream.from(parameters)
				.map(Parameter::getReference)
				.castSafely(MDTInstanceManagerAwareReference.class)
				.forEach(ref -> ref.activate(m_manager));
		T task = newTask(parameters, outputParamNames);
		task.run(m_manager);
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
	protected static final void main(MultiParameterTaskCommand<?> task, String... args) throws Exception {
		CommandLine commandLine = new CommandLine(task)
									.setUsageHelpWidth(110)
									.setStopAtUnmatched(true)
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
