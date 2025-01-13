package mdt.model.instance;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing the parameter information of an MDT instance. Provides
 * methods to access the ID, type, and value of the parameter.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = ParameterInfoUtils.Serializer.class)
@JsonDeserialize(using = ParameterInfoUtils.Deserializer.class)
public interface ParameterInfo {
	/**
	 * Gets the ID of the parameter.
	 * 
	 * @return the ID of the parameter
	 */
	public String getId();

	/**
	 * Gets the type of the parameter.
	 * The type of the parameter can be one of the following:
	 * <ul>
	 * <li>For Property data type</li>
	 * 	<ul>
	 *		<li>xs:string</li>
	 *      <li>xs:int</li>
	 *      <li>xs:integer</li>
	 *      <li>xs:long</li>
	 *      <li>xs:short</li>
	 *      <li>xs:double</li>
	 *      <li>xs:boolean</li>
	 *      <li>xs:dateTime</li>
	 *      <li>xs:date</li>
	 *      <li>xs:time</li>
	 *      <li>xs:unsignedByte</li>
	 *      <li>xs:unsignedInt</li>
	 *      <li>xs:unsignedLong</li>
	 *      <li>xs:unsignedShort</li>
	 *      <li>xs:duration</li>
	 *      <li>xs:hexBinary</li>
	 *      <li>xs:base64Binary</li>
	 *      <li>xs:anyURI</li>
	 *      <li>xs:negativeInteger</li>
	 *      <li>xs:nonNegativeInteger</li>
	 *      <li>xs:nonPositiveInteger</li>
	 *      <li>xs:positiveInteger</li>
	 *	</ul>
	 * <li>File</li>
	 * <li>Reference</li>
	 * <li>Range</li>
	 * </ul>
	 * 
	 * @return the type of the parameter
	 */
	public String getType();

	/**
	 * Gets the value of the parameter.
	 * 
	 * @return the value of the parameter
	 */
	public Object getValue();
	
	public String toValueString();
	
	public void serialize(JsonGenerator gen) throws IOException;
}