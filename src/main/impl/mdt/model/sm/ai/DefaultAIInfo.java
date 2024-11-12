package mdt.model.sm.ai;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import mdt.model.DefaultInput;
import mdt.model.DefaultMinResourceRequirements;
import mdt.model.DefaultOutput;
import mdt.model.Input;
import mdt.model.MinResourceRequirements;
import mdt.model.Output;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultAIInfo extends SubmodelElementCollectionEntity implements AIInfo {
	@SMCollectionField(idShort="Model", adaptorClass=DefaultModel.class)
	private Model model;
	
	@SMListField(idShort="Inputs", elementClass=DefaultInput.class)
	private List<Input> inputs = Lists.newArrayList();
	
	@SMListField(idShort="Outputs", elementClass=DefaultOutput.class)
	private List<Output> outputs = Lists.newArrayList();
	
	@SMCollectionField(idShort="MinResourceRequirements", adaptorClass=DefaultMinResourceRequirements.class)
	private MinResourceRequirements minResourceRequirements;
	
	public DefaultAIInfo() {
		setIdShort("AIInfo");
	}
}