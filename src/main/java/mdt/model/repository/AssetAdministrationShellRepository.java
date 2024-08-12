package mdt.model.repository;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

import mdt.model.service.AssetAdministrationShellService;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AssetAdministrationShellRepository {
	public List<AssetAdministrationShellService> getAllAssetAdministrationShells();
	public AssetAdministrationShellService getAssetAdministrationShellById(String aasId);
	public List<AssetAdministrationShellService> getAssetAdministrationShellByAssetId(String key);
	public List<AssetAdministrationShellService> getAssetAdministrationShellByIdShort(String idShort);
	
	public AssetAdministrationShellService postAssetAdministrationShell(AssetAdministrationShell aas);
	public AssetAdministrationShellService putAssetAdministrationShellById(AssetAdministrationShell aas);
	public void deleteAssetAdministrationShellById(String id);
}
