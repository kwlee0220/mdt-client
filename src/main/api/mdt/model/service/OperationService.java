package mdt.model.service;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.data.DefaultOperation;
import mdt.model.sm.entity.SMCollectionField;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class OperationService extends ParameterCollectionBase {
	@SMCollectionField(idShort="Operation") private DefaultOperation operation;
	
	public OperationService(SubmodelService service) {
		super(service, "Operation");
	}
}
