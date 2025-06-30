package mdt.task.builtin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import utils.KeyedValueList;
import utils.Throwables;
import utils.Tuple;
import utils.Utilities;

import mdt.cli.AbstractMDTCommand;
import mdt.model.expr.LiteralExpr;
import mdt.model.expr.MDTElementReferenceExpr;
import mdt.model.expr.MDTExpr;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Unmatched;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "test")
public abstract class MultiVariablesCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(MultiVariablesCommand.class);
	
	@Unmatched()
	private List<String> m_unmatcheds = Lists.newArrayList();
	
	protected MultiVariablesCommand() {
		setLogger(s_logger);
	}
	
	@Getter
	@AllArgsConstructor
	@Accessors(prefix = "m_")
	public static class UnmatchedOption {
		private final String m_type;
		private final String m_name;
		private final String m_value;
		
		@Override
		public String toString() {
			return String.format("%s.%s=%s", m_type, m_name, m_value);
		}
	}
	
	protected List<UnmatchedOption> collectUnmatchedOptions() {
		List<UnmatchedOption> unmatchedOptions = Lists.newArrayList();
		List<String> remains = Lists.newArrayList(m_unmatcheds);
		while ( remains.size() > 0 ) {
			String optName = remains.remove(0);
			
			optName = trimHeadingDashes(optName);
			Tuple<String, String> tup = Utilities.split(optName, '.', Tuple.of(null, optName));
			if ( remains.size() == 0 || remains.get(0).startsWith("-") ) {
				// 옵션의 value가 지정되지 않은 경우
				unmatchedOptions.add(new UnmatchedOption(tup._1, tup._2, null));
			}
			else {
				unmatchedOptions.add(new UnmatchedOption(tup._1, tup._2, remains.removeFirst()));
			}
		}
		
		return unmatchedOptions;
	}
	
	protected void loadTaskVariablesFromArguments(MDTInstanceManager manager, TaskDescriptor descriptor) {
		// Command line에서 지정된 옵션을 파싱하여 input/output parameter를 추출한다.
		// 이때, input/output parameter 관련 정보들은 unmatcheds에 포함되어 있다.
		// Input/output parameter는 다음과 같은 형식으로 지정된다.
		//   --in.<parameter-name> <element-reference> (input parameter의 경우)
		//   --out.<parameter-name> <element-reference> (output parameter의 경우)
		//   --inout.<parameter-name> <element-reference> (input/output parameter의 경우)
		//
		List<UnmatchedOption> unmatchedOptions = collectUnmatchedOptions();
//		Map<String,String> unmatchedOptions
//				= FStream.from(m_unmatcheds)
//						.buffer(2, 2)
//						.peek(b -> {
//							if ( b.size() != 2 ) {
//								String msg = String.format("invalid variable specification: %s", b.get(0));
//								throw new IllegalArgumentException(msg);
//							}
//						})
//						// 첫번째 항목  앞에 붙은 '-'는 제거하여 key로 사용하고,
//						// 두번째 항묵을 value로 사용한다. 
//						.toKeyValueStream(b -> trimHeadingDashes(b.get(0)), b -> b.get(1))
//						.toMap();
		
		for ( UnmatchedOption unmatchedOpt: collectUnmatchedOptions() ) {
			Variable var;
			MDTExpr expr = MDTExprParser.parseExpr(unmatchedOpt.getValue());
			if ( expr instanceof MDTElementReferenceExpr refExpr ) {
				try {
					MDTElementReference ref = refExpr.evaluate();
					ref.activate(manager);
					var = Variables.newInstance(unmatchedOpt.getName(), "", ref);
				}
				catch ( Exception e ) {
					Throwable cause = Throwables.unwrapThrowable(e);
					String msg = String.format("Failed to parse %s variable(\"%s\"), ref=%s, cause=%s",
												unmatchedOpt.getType(), unmatchedOpt.getName(), unmatchedOpt.getValue(),
												cause);
					throw new IllegalArgumentException(msg);
				}
			}
			else if ( expr instanceof LiteralExpr lit ) {
				var = Variables.newInstance(unmatchedOpt.getName(), "", lit.evaluate());
			}
			else {
				throw new IllegalArgumentException("Unexpected variable expression: name=" + unmatchedOpt.getName()
													+ ", expr=" + unmatchedOpt.getValue());
			}
			
			String kind = unmatchedOpt.getType();
			if ( kind == null ) {
				throw new IllegalArgumentException("unexpected option: " + unmatchedOpt);
			}
			switch ( kind.toLowerCase() ) {
				case "in":
					updateTaskVariable(descriptor.getInputVariables(), var);
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set input parameter variable[{}]", kind, unmatchedOpt.getName());
					}
					break;
				case "out":
					updateTaskVariable(descriptor.getOutputVariables(), var);
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set output parameter variable[{}]", kind, unmatchedOpt.getName());
					}
					break;
				case "inout":
					updateTaskVariable(descriptor.getInputVariables(), var);
					updateTaskVariable(descriptor.getOutputVariables(), var);
					if ( getLogger().isDebugEnabled() ) {
						getLogger().debug("set inoutput parameter variable[{}]", kind, unmatchedOpt.getName());
					}
					break;
				case "opt":
				case "opton":
					descriptor.addLabel(unmatchedOpt.getName(), unmatchedOpt.getValue());
					break;
				default:
					throw new AssertionError("invalid kind: " + kind);
			}
		}
	}
	
	private static void updateTaskVariable(KeyedValueList<String, Variable> varList, Variable paramVar) {
		Variable taskVar = varList.getOfKey(paramVar.getName());
		if ( taskVar != null ) {
			varList.replace(paramVar);
		}
		else {
			varList.add(paramVar);
		}
	}
	
	private static String trimHeadingDashes(String optName) {
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
