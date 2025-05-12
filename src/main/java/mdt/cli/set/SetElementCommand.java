package mdt.cli.set;

import java.io.File;
import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.io.IOUtils;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.SubmodelService;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.DefaultAASFile;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
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
		description = "set Element value."
	)
public class SetElementCommand extends AbstractMDTCommand {
	@Parameters(index="0", arity="1", paramLabel="element-ref", description="target SubmodelElementReference to set")
	private String m_target = null;
	
	@ArgGroup(exclusive=true)
	private ValueExprSpec m_valueExprSpec;
	static class ValueExprSpec {
		@Option(names={"--value"}, arity="1", paramLabel="expression", description="Source value specification")
		private String m_value;
	}
	
	@ArgGroup(exclusive=true)
	private JsonSpec m_jsonSpec;
	static class JsonSpec {
		@Option(names={"--json"}, paramLabel="Json file path", description="Json file path")
		private File m_jsonFile;
	}
	
	@ArgGroup(exclusive = false)
	FileElementSpec m_fileElementSpec;
	static class FileElementSpec {
		@Option(names = {"--file", "-f"}, paramLabel="file path", required=true)
		private File m_file;
		
		@Option(names = {"--path"}, paramLabel="value for File", required=false)
		private String m_path;
		
		@Option(names = {"--mimeType"}, paramLabel="contentType for File", required=false)
		private String m_mimeType;
	}

	@Override
	protected void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		ElementReference ref = ElementReferences.parseExpr(m_target);
		if ( !(ref instanceof MDTElementReference) ) {
			throw new IllegalArgumentException("Target element is not MDTElementReference: " + ref);
		}
		MDTElementReference targetRef = (MDTElementReference)ref;
		
		targetRef.activate(manager);
		if ( m_valueExprSpec != null && m_valueExprSpec.m_value != null ) {
			setWithExpr(manager, targetRef, m_valueExprSpec.m_value);
		}
		else if ( m_jsonSpec != null && m_jsonSpec.m_jsonFile != null ) {
			setWithJsonFile(manager, targetRef, m_jsonSpec.m_jsonFile);
		}
		else if ( m_fileElementSpec.m_file != null ) {
			SubmodelElement sme = targetRef.read();
			if ( !(sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File) ) {
				throw new IllegalArgumentException("Target element is not a File: " + targetRef);
			}
			
			setFile(manager, targetRef, m_fileElementSpec.m_file, m_fileElementSpec.m_path,
					m_fileElementSpec.m_mimeType);
		}
		else {
			throw new IllegalArgumentException("Value is not specified");
		}
	}
	
	private void setWithExpr(MDTInstanceManager manager, ElementReference target, String expr) throws IOException {
		MDTExprParser parser = new MDTExprParser();
		Object src = parser.parseExpr(expr).evaluate();
		if ( src instanceof MDTElementReference ref ) {
			ref.activate(manager);
			target.updateValue(ref.readValue());
		}
		else if ( src instanceof ElementValue value ) {
			target.updateValue(value);
		}
	}
	
	/**
	 * 주어진 경로 'jsonFile'의 JSON 파일에 저장된 SubmodelElement을 읽어서 target 참조에 해당하는
	 * SubmodelElement를 갱신시킨다.
	 * 만일 JSON 파일에서 SubmodelElement이 아닌 ElementValue가 저장된 경우에는 값만 갱신시킨다.
	 *
	 * @param manager	target 참조에 사용할 {@link MDTInstanceManager} 객체.
	 * @param target	target SubmodelElement의 참조
	 * @param jsonFile	갱신할 값이 저장된 JSON 파일 경로
	 * @throws IOException	갱신 과정에서 예외가 발생된 경우.
	 */
	private void setWithJsonFile(MDTInstanceManager manager, ElementReference target, File jsonFile)
		throws IOException {
		String jsonStr = IOUtils.toString(jsonFile);
		try {
			SubmodelElement newSme = MDTModelSerDe.readValue(jsonStr, SubmodelElement.class);
			target.update(newSme);
		}
		catch ( IOException e ) {
			// JSON 파일에 SubmodelElement이 아닌 ElementValue가 저장된 경우에는 값을 읽어서 갱신시킨다.
			try {
				target.updateWithValueJsonString(jsonStr);
			}
			catch ( IOException e1 ) {
				target.updateWithRawString(jsonStr);
			}
		}
	}
	
	private void setFile(MDTInstanceManager manager, MDTElementReference target, File file, String path,
						String mimeType) throws IOException {
		SubmodelService svc = target.getSubmodelService();
		DefaultAASFile mdtFile = DefaultAASFile.from(file, path, mimeType);
		svc.putFileByPath(target.getIdShortPathString(), mdtFile);
	}

	public static void main(String... args) throws Exception {
		main(new SetElementCommand(), args);
	}
}
