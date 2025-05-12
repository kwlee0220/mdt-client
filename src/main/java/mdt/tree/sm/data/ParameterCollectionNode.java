package mdt.tree.sm.data;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.Tuple;
import utils.stream.KeyValueFStream;

import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterCollection;
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
		
		return KeyValueFStream.fromKeyed(params)
								.outerJoin(KeyValueFStream.fromKeyed(values))
								.map(kv -> {
									Parameter p = kv.value()._1.isEmpty() ? null : kv.value()._1.get(0);
									ParameterValue v = kv.value()._2.isEmpty() ? null : kv.value()._2.get(0);
									return Tuple.of(p,  v);
								})
								.zipWithIndex()
								.map(idxed -> new ParameterPairNode(idxed.index(), idxed.value()._1, idxed.value()._2))
								.toList();
	}
}
