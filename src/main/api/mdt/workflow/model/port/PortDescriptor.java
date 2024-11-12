package mdt.workflow.model.port;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import utils.Named;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonDeserialize(using = PortDescriptorDeserializer.class)
public interface PortDescriptor extends Named {
	public void setName(String name);
	
	public String getDescription();
	public void setDescription(String desc);
	
	public PortType getPortType();
	
	public String toStringExpr();
}
