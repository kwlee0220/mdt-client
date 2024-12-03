package mdt.model;

import mdt.aas.ShellRegistry;
import mdt.aas.SubmodelRegistry;
import mdt.aas.SubmodelRepository;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ServiceFactory {
	public ShellRegistry getAssetAdministrationShellRegistry(String endpoint);
	public SubmodelRegistry getSubmodelRegistry(String endpoint);

	public SubmodelRepository getSubmodelRepository(String endpoint);

	public AssetAdministrationShellService getAssetAdministrationShellService(String endpoint);
	public SubmodelService getSubmodelService(String endpoint);
}
