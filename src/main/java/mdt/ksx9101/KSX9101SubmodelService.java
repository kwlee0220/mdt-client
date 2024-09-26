package mdt.ksx9101;

import java.util.Set;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.stream.FStream;

import mdt.ksx9101.model.Data;
import mdt.ksx9101.model.InformationModel;
import mdt.ksx9101.model.impl.DefaultData;
import mdt.ksx9101.model.impl.DefaultInformationModel;
import mdt.ksx9101.model.impl.DefaultMDTEntityFactory;
import mdt.model.SubmodelElementEntity;
import mdt.model.service.SubmodelService;

import lombok.experimental.Delegate;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class KSX9101SubmodelService implements SubmodelService {
	private static final DefaultMDTEntityFactory FACTORY = new DefaultMDTEntityFactory();
	
	@Delegate private final SubmodelService m_service;
	private final String m_mountPointIdShortPath;
	
	public KSX9101SubmodelService(SubmodelService service, String mountPointIdShortPath) {
		m_service = service;
		m_mountPointIdShortPath = mountPointIdShortPath;
	}
	
	public KSX9101SubmodelService(SubmodelService service) {
		m_service = service;
		m_mountPointIdShortPath = "DataInfo";
	}
	
	public static final InformationModel asInformationModelSubmodel(Submodel submodel) {
		if ( !"InformationModel".equals(submodel.getIdShort()) ) {
			return null;
		}
		
		Set<String> idShorts = FStream.from(submodel.getSubmodelElements())
										.castSafely(SubmodelElementCollection.class)
										.map(SubmodelElement::getIdShort)
										.toSet();
		
		if ( idShorts.containsAll(Set.of("MDTInfo", "TwinComposition")) ) {
			InformationModel infoModel = new DefaultInformationModel();
			infoModel.fromAasModel(submodel);
			return infoModel;
		}
		else {
			return null;
		}
	}
	
	public static final Data asDataSubmodel(Submodel submodel) {
		if ( !"Data".equals(submodel.getIdShort()) ) {
			return null;
		}
		
		Set<String> idShorts = FStream.from(submodel.getSubmodelElements())
										.castSafely(SubmodelElementCollection.class)
										.map(SubmodelElement::getIdShort)
										.toSet();
		
		if ( idShorts.containsAll(Set.of("SubModelInfo", "DataInfo")) ) {
			Data dataSubmodel = new DefaultData();
			dataSubmodel.fromAasModel(submodel);
			return dataSubmodel;
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends TopLevelEntity> T getEntity(Class<T> entityClass) {
		String idShortPath = String.format("%s.%s", m_mountPointIdShortPath, entityClass.getSimpleName());
		SubmodelElement model = m_service.getSubmodelElementByPath(idShortPath);
		
		SubmodelElementEntity adaptor = FACTORY.newInstance(entityClass.getName());
		adaptor.fromAasModel(model);
		
		return (T)adaptor;
	}
}
