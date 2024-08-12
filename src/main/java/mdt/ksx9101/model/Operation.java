package mdt.ksx9101.model;

import java.util.List;

import com.google.common.base.Preconditions;

import utils.func.FOption;

import mdt.ksx9101.TopLevelEntity;
import mdt.model.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Operation extends ParametricEntity, TopLevelEntity {
	public String getOperationId();
	public void setOperationId(String id);
	
	public String getOperationName();
	public void setOperationName(String name);

	public String getOperationType();
	public void setOperationType(String operationType);

	public String getUseIndicator();
	public void setUseIndicator(String useIndicator);
	
	public List<ProductionOrder> getProductionOrders();
	public void setProductionOrders(List<ProductionOrder> orders);
	
//	public String getOperationStartDateTime();
//	public void setOperationStartDateTime(String dtStr);
//	
//	public String getOperationEndDateTime();
//	public void setOperationEndDateTime(String dtStr);
//	
//	public String getEventDateTime();
//	public void setEventDateTime(String dtStr);

	public default void update(String idShortPath, Object value) {
		List<String> pathSegs = SubmodelUtils.parseIdShortPath(idShortPath).toList();
		
		String seg0 = pathSegs.get(0);
		Preconditions.checkArgument("OperationParameters".equals(seg0),
									"'OperationParameters' is expected, but={}", seg0);
		
		String seg1 = pathSegs.get(1);
		ParameterValue pvalue;
		try {
			int ordinal = Integer.parseInt(seg1);
			pvalue = getParameterValues().get(ordinal);
		}
		catch ( NumberFormatException e ) {
			pvalue = getParameterValue(seg1).orElse(null);
		}
		FOption.accept(pvalue, pv -> pv.setParameterValue((String)value));
	}
}
