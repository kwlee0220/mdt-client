package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;

import utils.Indexed;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSegments extends SubmodelElementCollectionEntity implements Segments {
	private List<? extends DefaultSegment> segments;
	
	public DefaultSegments() {
		setIdShort("Segments");
		setSemanticId(Segments.SEMANTIC_ID_REFERENCE);
	}
	
	public DefaultSegments(List<? extends DefaultSegment> segments) {
		Preconditions.checkArgument(segments != null, "segment is null");
		Preconditions.checkArgument(segments.size() >= 1, "segment is empty");
		
		setIdShort("Segments");
		setSemanticId(Segments.SEMANTIC_ID_REFERENCE);
		
		this.segments = segments;
	}

	@Override
	public String getIdShort() {
		return "Segments";
	}

	@Override
	public void updateAasModel(SubmodelElement sme) {
		super.updateAasModel(sme);
		SubmodelElementCollection smc = (SubmodelElementCollection)sme;
		
		for ( DefaultSegment segment : segments ) {
			FOption<Indexed<SubmodelElement>> ochild = SubmodelUtils.findFieldById(smc, segment.getIdShort());
			
			SubmodelElement target;
			if ( ochild.isPresent() ) {
				SubmodelElement segSmc = smc.getValue().remove(ochild.get().index());
				target = ochild.get().value();
			}
			else {
				target = segment.newSubmodelElement();
			}
			smc.getValue().add(target);
		}
	}

	@Override
	public void updateFromAasModel(SubmodelElement model) {
		Preconditions.checkArgument(model != null, "model is null");
		Preconditions.checkArgument(model instanceof SubmodelElementCollection,
									"model is not SubmodelElementCollection, but=" + model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		
		segments = FStream.from(smc.getValue())
			                .castSafely(SubmodelElementCollection.class)
							.map(segSmc -> {
								DefaultLinkedSegment segment = new DefaultLinkedSegment();
								segment.updateFromAasModel(segSmc);
								return segment;
							})
							.toList();
	}

//	@Override
//	public SubmodelElementCollection newSubmodelElement() {
//		SubmodelElementCollection smc = new DefaultSubmodelElementCollection.Builder()
//												.idShort(getIdShort())
//												.semanticId(Segments.SEMANTIC_ID_REFERENCE)
//												.build();
//		updateAasModel(smc);
//		return smc;
//	}
}
