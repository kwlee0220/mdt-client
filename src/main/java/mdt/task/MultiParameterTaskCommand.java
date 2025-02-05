package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.KeyedValueList;
import utils.Throwables;
import utils.UnitUtils;
import utils.Utilities;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferenceUtils;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MultiParameterTaskCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(MultiParameterTaskCommand.class);
	
	protected Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}
	
	@Unmatched()
	private List<String> m_unmatcheds = Lists.newArrayList();
	
	protected MultiParameterTaskCommand() {
		setLogger(s_logger);
	}
	
	public Tuple<List<Parameter>,List<Parameter>> loadParameters() {
		// Command line에서 지정된 옵션을 파싱하여 input/output parameter를 추출한다.
		// 이때, input/output parameter 관련 정보들은 unmatcheds에 포함되어 있다.
		// Input/output parameter는 다음과 같은 형식으로 지정된다.
		//   --in.<parameter-name> <element-reference> (input parameter의 경우)
		//   --out.<parameter-name> <element-reference> (output parameter의 경우)
		//   --inout.<parameter-name> <element-reference> (input/output parameter의 경우)
		//
		Map<String,String> unmatchedOptions = FStream.from(m_unmatcheds)
													.buffer(2, 2)
													.peek(b -> {
														if ( b.size() != 2 ) {
															String msg = String.format("invalid parameter specification: %s", b.get(0));
															throw new IllegalArgumentException(msg);
														}
													})
													.toMap(b -> trimHeadingDashes(b.get(0)), b -> b.get(1));
		
		KeyedValueList<String,Parameter> inputParameters = KeyedValueList.newInstance(Parameter::getName);
		KeyedValueList<String,Parameter> outputParameters = KeyedValueList.newInstance(Parameter::getName);
		
		for ( Entry<String,String> ent: unmatchedOptions.entrySet() ) {
			Tuple<String,String> tup = Utilities.split(ent.getKey(), '.', Tuple.of("in", ent.getKey()));
			String kind = tup._1;
			String varName = tup._2;
			
			ElementReference ref;
			try {
				ref = ElementReferenceUtils.parseString(ent.getValue());
			}
			catch ( Exception e ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				String msg = String.format("Failed to parse \"%s\" variable %s, ref=%s, cause=%s",
											kind, varName, ent.getValue(), cause);
				throw new IllegalArgumentException(msg);
			}
			switch ( kind.toLowerCase() ) {
				case "in":
					inputParameters.add(Parameter.of(varName, ref));
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set input parameter[{}]", kind, varName);
					}
					break;
				case "out":
					outputParameters.add(Parameter.of(varName, ref));
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set output parameter[{}]", kind, varName);
					}
					break;
				case "inout":
					inputParameters.add(Parameter.of(varName, ref));
					outputParameters.add(Parameter.of(varName, ref));
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set inoutput parameter[{}]", kind, varName);
					}
					break;
			}
		}

		return Tuple.of(inputParameters, outputParameters);
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
}
