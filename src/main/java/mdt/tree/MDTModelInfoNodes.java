package mdt.tree;

import java.io.IOException;
import java.util.List;

import org.barfuin.texttree.api.Node;

import com.google.common.collect.Lists;

import utils.Tuple;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTModelService;
import mdt.model.instance.MDTOperationDescriptor;
import mdt.model.instance.MDTParameterDescriptor;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.NamedValueType;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTModelInfoNodes {
	public static CompositeNode newInstanceMDTModelNode(MDTInstance instance) throws IOException {
		List<Node> children = Lists.newArrayList();
		
		MDTModelService infoSvc = MDTModelService.of(instance);
		
		TitleUpdatableNode parameters = newParameterListNode(infoSvc);
		children.add(parameters);
		
		CompositeNode operations = newOperationListNode(instance);
		children.add(operations);
		
		String title = String.format("Instance(%s)", instance.getId());
		return new CompositeNode(title, children);
	}
	
	public static TitleUpdatableNode newParameterListNode(MDTModelService model) throws IOException {
		List<DefaultNode> paramNodeList = FStream.from(model.getParameterDescriptorAll())
										        .mapOrThrow(desc -> newParameterNode(model, desc))
										        .toList();
		return new ArrayNode("Parameters", paramNodeList);
	}
	
	public static DefaultNode newParameterNode(MDTModelService model, MDTParameterDescriptor desc) throws IOException {
		ElementValue paramValue = model.readParameterValue(desc.getName());
		String title = String.format("%s (%s): %s", desc.getName(), desc.getValueType(), paramValue);
        return new DefaultNode(title);
	}
	
	public static CompositeNode newOperationListNode(MDTInstance instance) throws IOException {
		List<? extends TitleUpdatableNode> opInfoNodes
									= FStream.from(instance.getInstanceDescriptor().getMDTOperationDescriptorAll())
											.mapOrThrow(desc -> newOperationNode(instance, desc))
											.toList();
		return new CompositeNode("Operations", opInfoNodes);
	}

	public static TitleUpdatableNode newOperationNode(MDTInstance instance, MDTOperationDescriptor opDesc)
		throws IOException {
		Tuple<List<ElementValue>, List<ElementValue>> inOutArgs
													= readMDTOperationArgumentValueAll(instance, opDesc.getName());
		
		List<DefaultNode> inArgs = FStream.zip(opDesc.getInputArguments(), inOutArgs._1)
											.mapOrThrow(tup -> newArgumentNode(tup._1, tup._2))
											.toList();
		ArrayNode inArgsNode = new ArrayNode("Inputs", inArgs);
		
		List<DefaultNode> outArgs = FStream.zip(opDesc.getOutputArguments(), inOutArgs._2)
											.mapOrThrow(tup -> newArgumentNode(tup._1, tup._2))
											.toList();
		ArrayNode outArgsNode = new ArrayNode("Outputs", outArgs);

		String title = String.format("%s(%s)", opDesc.getName(), opDesc.getOperationType());
		List<TitleUpdatableNode> inoutArgsNode = List.of(inArgsNode, outArgsNode);
		return new CompositeNode(title, inoutArgsNode);
	}
	
	private static DefaultNode newArgumentNode(NamedValueType arg, ElementValue value) throws IOException {
		String valStr = FOption.mapOrElse(value, ElementValue::toString, "<unknown>");
		String title = String.format("%s (%s): %s", arg.getName(), arg.getValueType(), valStr);
		return new DefaultNode(title);
	}
	
	private static Tuple<List<ElementValue>, List<ElementValue>>
	readMDTOperationArgumentValueAll(MDTInstance instance, String opName) throws IOException {
		DefaultSubmodelReference submodelRef = DefaultSubmodelReference.ofIdShort(instance.getId(), opName);
		
		MDTArgumentReference inArgsRef = MDTArgumentReference.builder()
															.submodelReference(submodelRef)
															.kind(MDTArgumentKind.INPUT)
															.argument("*")
															.build();
		inArgsRef.activate(instance.getInstanceManager());
		
		List<ElementValue> inArgValues = ((ElementListValue)inArgsRef.readValue()).getElementAll();
		
		MDTArgumentReference outArgsRef = MDTArgumentReference.builder()
																.submodelReference(submodelRef)
																.kind(MDTArgumentKind.OUTPUT)
																.argument("*")
																.build();
		outArgsRef.activate(instance.getInstanceManager());
		List<ElementValue> outArgValues = ((ElementListValue) outArgsRef.readValue()).getElementAll();
		
		return Tuple.of(inArgValues, outArgValues);
    }
}
