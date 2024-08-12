package mdt.model.instance;

import mdt.model.registry.AASRegistry;
import mdt.model.registry.SubmodelRegistry;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManagerClient extends MDTInstanceManager {
	public AASRegistry getAssetAdministrationShellRegistry();
	public SubmodelRegistry getSubmodelRegistry();
}
