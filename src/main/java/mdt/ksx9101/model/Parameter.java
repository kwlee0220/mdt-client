package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Parameter {
	public String getEntityId();
	public void setEntityId(String containerId);

	public String getParameterId();
	public void setParameterId(String parameterId);

	public String getParameterName();
	public void setParameterName(String parameterName);

	public String getParameterType();
	public void setParameterType(String typeName);

	public String getParameterGrade();
	public void setParameterGrade(String grade);

	public String getParameterUOMCode();
	public void setParameterUOMCode(String uomCode);

	public String getLSL();
	public void setLSL(String lsl);

	public String getUSL();
	public void setUSL(String usl);
	
	public String getPeriodicDataCollectionIndicator();
	public void setPeriodicDataCollectionIndicator(String indicator);
	
	public String getDataCollectionPeriod();
	public void setDataCollectionPeriod(String period);
	
//	public Object parseParameterValue(ParameterValue paramValue);
//	public ParameterValue toParameterValue(Object value);
}
