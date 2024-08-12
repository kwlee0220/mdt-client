package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.ksx9101.model.Parameter;
import mdt.ksx9101.model.ParameterValue;
import mdt.ksx9101.model.ParametricEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class ParametricEntityNode implements Node {
	abstract protected ParametricEntity getParametricEntity();
	
	@Override
	public Iterable<? extends Node> getChildren() {
		List<Parameter> params = getParametricEntity().getParameters();
		List<ParameterValue> values = getParametricEntity().getParameterValues();
		
		return FStream.from(params)
						.outerJoin(FStream.from(values), Parameter::getParameterId,
									ParameterValue::getParameterId)
						.map(t -> {
							Parameter p = t._1.isEmpty() ? null : t._1.get(0);
							ParameterValue v = t._2.isEmpty() ? null : t._2.get(0);
							return Tuple.of(p,  v);
						})
						.map(t -> new ParameterNode(t._1, t._2))
						.toList();
	}
}
