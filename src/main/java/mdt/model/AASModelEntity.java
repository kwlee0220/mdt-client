package mdt.model;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AASModelEntity<T> {
	public T toAasModel();
	public void fromAasModel(T model);
}
