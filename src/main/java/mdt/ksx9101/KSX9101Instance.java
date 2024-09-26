package mdt.ksx9101;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.ksx9101.model.Data;
import mdt.ksx9101.model.InformationModel;
import mdt.ksx9101.model.impl.DefaultInformationModel;
import mdt.model.instance.MDTInstance;
import mdt.model.service.SubmodelService;

import lombok.experimental.Delegate;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class KSX9101Instance implements MDTInstance {
	@Delegate private final MDTInstance m_instance;
	private SubmodelService m_infoModelSubmodelService;
	private KSX9101DataService m_dataSubmodelService;
	
	public KSX9101Instance(MDTInstance instance) {
		m_instance = instance;
	}
	
	public SubmodelService getInformationModelService() {
		if ( m_infoModelSubmodelService == null ) {
			m_infoModelSubmodelService = m_instance.getSubmodelServiceBySemanticId(InformationModel.SEMANTIC_ID);
		}
		
		return m_infoModelSubmodelService;
	}
	
	public InformationModel getInformationModel() {
		Submodel sm = getInformationModelService().getSubmodel();
		DefaultInformationModel model = new DefaultInformationModel();
		model.fromAasModel(sm);
		
		return model;
	}
	
	public KSX9101DataService getDataSubmodelService() {
		if ( m_dataSubmodelService == null ) {
			SubmodelService dataSvc = m_instance.getSubmodelServiceBySemanticId(Data.SEMANTIC_ID);
			
			InformationModel info = getInformationModel();
			switch ( info.getMDTInfo().getAssetType() ) {
				case "Machine":
					m_dataSubmodelService = new KSX9101EquipmentService(dataSvc);
					break;
				case "Process":
					m_dataSubmodelService = new KSX9101OperationService(dataSvc);
					break;
				default:
					throw new IllegalArgumentException("Invalid AssetType: " + info.getMDTInfo().getAssetType());
			}
		}
		
		return m_dataSubmodelService;
	}
}
