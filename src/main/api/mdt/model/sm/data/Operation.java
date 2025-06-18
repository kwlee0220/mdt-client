package mdt.model.sm.data;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Operation extends ParameterCollection {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/Data/Operation/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
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
}
