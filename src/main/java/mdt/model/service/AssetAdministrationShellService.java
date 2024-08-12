package mdt.model.service;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AssetAdministrationShellService {
	public AssetAdministrationShell getAssetAdministrationShell();
	public AssetAdministrationShell putAssetAdministrationShell(AssetAdministrationShell aas);

	public List<Reference> getAllSubmodelReferences();
	public Reference postSubmodelReference(Reference ref);
	public void deleteSubmodelReference(String submodelId);
	
	public AssetInformation getAssetInformation();
	public AssetInformation putAssetInformation(AssetInformation assetInfo);
}
