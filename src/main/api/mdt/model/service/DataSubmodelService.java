package mdt.model.service;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;

import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultData;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DataSubmodelService implements SubmodelService {
	@Delegate private final SubmodelService m_service;
	private final DefaultData m_data;
	
	public DataSubmodelService(SubmodelService service) {
		Submodel submodel = service.getSubmodel();
		Preconditions.checkArgument(submodel.getSemanticId().equals(Data.SEMANTIC_ID_REFERENCE),
									"Not Data Submodel, but=" + submodel); 
		
		m_service = service;
		
		m_data = new DefaultData();
		m_data.updateFromAasModel(submodel);
	}
	
	public Data getData() {
		return m_data;
	}
}
