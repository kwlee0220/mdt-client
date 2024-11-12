package mdt.tree.sm.data;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.model.service.ParameterCollection;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class ParameterCollectionNode implements Node {
	protected ParameterCollection m_paramColl;
	
	protected ParameterCollectionNode(ParameterCollection coll) {
		m_paramColl = coll;
	}
	
	@Override
	public Iterable<? extends Node> getChildren() {
		List<Parameter> params = m_paramColl.getParameterList();
		List<ParameterValue> values = m_paramColl.getParameterValueList();
		
		return FStream.from(params)
						.outerJoin(FStream.from(values), Parameter::getParameterId, ParameterValue::getParameterId)
						.map(t -> {
							Parameter p = t._1.isEmpty() ? null : t._1.get(0);
							ParameterValue v = t._2.isEmpty() ? null : t._2.get(0);
							return Tuple.of(p,  v);
						})
						.zipWithIndex()
						.map(idxed -> new ParameterPairNode(idxed.index(), idxed.value()._1, idxed.value()._2))
						.toList();
	}
}
