package mdt.model.service;

import mdt.model.sm.data.DefaultOperation;
import mdt.model.sm.entity.SMCollectionField;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationService extends ParameterCollectionBase {
	@SMCollectionField(idShort="Operation") private DefaultOperation m_operation;
	
	public OperationService(SubmodelService service) {
		super(service, "Operation");
	}

	public DefaultOperation getOperation() {
		return m_operation;
	}

	public void setOperation(DefaultOperation operation) {
		this.m_operation = operation;
	}
}
