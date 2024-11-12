package mdt.model.sm.entity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AASModelEntity<T> {
	public void updateAasModel(T model);
	public void updateFromAasModel(T model);
}
