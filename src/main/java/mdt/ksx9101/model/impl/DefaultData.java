package mdt.ksx9101.model.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;

import utils.stream.FStream;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Data;
import mdt.ksx9101.model.SubModelInfo;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultData implements Data {
	private static final String IDSHORT = "Data";
	
	private String id;
	private Reference semanticId;
	private SubModelInfo subModelInfo;
	private DefaultDataInfo dataInfo;

	@Override
	public Submodel toAasModel() {
		List<SubmodelElement> subElements =  List.of(
			this.subModelInfo.toAasModel(), this.dataInfo.toAasModel()
		);
		
		return new DefaultSubmodel.Builder()
					.id(this.id)
					.idShort(IDSHORT)
					.semanticId(SEMANTIC_ID_REFERENCE)
					.submodelElements(subElements)
					.build();
	}

	@Override
	public void fromAasModel(Submodel model) {
		this.id = model.getId();
		
		this.semanticId = model.getSemanticId();
//		if ( !this.semanticId.equals(SEMANTIC_ID) ) {
//			String msg = String.format("Unmatched semanticId: expected=%s, model=%s",
//										SEMANTIC_ID, this.semanticId);
//			throw new IllegalArgumentException(msg);
//		}
		
		Map<String,SubmodelElementCollection> elements = FStream.from(model.getSubmodelElements())
																.castSafely(SubmodelElementCollection.class)
																.toMap(SubmodelElement::getIdShort,
																		Function.identity());
		
		this.subModelInfo = new DefaultSubModelInfo();
		this.subModelInfo.fromAasModel(elements.get("SubModelInfo"));
		
		this.dataInfo = new DefaultDataInfo();
		this.dataInfo.fromAasModel(elements.get("DataInfo"));
	}
	
	@Override
	public String toString() {
		return String.format(IDSHORT);
	}
}
