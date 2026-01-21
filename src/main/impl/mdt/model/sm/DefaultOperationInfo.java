package mdt.model.sm;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultInput;
import mdt.model.DefaultOutput;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperationInfo extends SubmodelElementCollectionEntity
										implements OperationEntity {
	@SMListField(idShort="Inputs", elementClass=DefaultInput.class)
	private List<Input> inputs = Lists.newArrayList();
	
	@SMListField(idShort="Outputs", elementClass=DefaultOutput.class)
	private List<Output> outputs = Lists.newArrayList();
	
	protected DefaultOperationInfo(String idShort) {
		setIdShort(idShort);
	}
}
