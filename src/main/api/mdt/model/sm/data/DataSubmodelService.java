package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;

import mdt.model.SubmodelService;


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
	
	/**
	 * DataSubmodel에 {@link Equipment}이 포함되어 있는지 여부를 반환한다.
	 *
	 * @return Equipment이 포함되어 있으면 true, 그렇지 않으면 false.
	 */
	public boolean containsEquipment() {
		return m_data.getDataInfo().isEquipment();
	}
	
	/**
	 * DataSubmodel에 {@link Operation}이 포함되어 있는지 여부를 반환한다.
	 *
	 * @return	Operation이 포함되어 있으면 true, 그렇지 않으면 false.
	 */
	public boolean containsOperation() {
		return m_data.getDataInfo().isOperation();
	}
	
	/**
	 * DataSubmodel에서 정의된 모든 Parameter들의 컬렉션을 반환한다.
	 *
	 * @return	Parameter 컬렉션 객체.
	 */
	public ParameterCollection getParameterAll() {
		return m_data.getDataInfo().getFirstSubmodelElementEntityByClass(ParameterCollection.class);
    }
}
