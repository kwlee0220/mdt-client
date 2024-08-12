package mdt.ksx9101.model;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionDependency {
	public String getSource();
	public void setSource(String src);
	
	public String getTarget();
	public void setTarget(String tar);
	
	public String getDependencyType();
	public void setDependencyType(String type);
}
