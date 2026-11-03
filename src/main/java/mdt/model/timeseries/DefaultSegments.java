package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;

import utils.stream.FStream;

import mdt.model.ReferenceUtils;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link Segments}의 기본 구현 클래스.
 * <p>
 * idShort는 항상 {@code "Segments"}이다. AAS 모델로부터 읽을 때({@link #updateFromAasModel(SubmodelElement)})는
 * 하위의 모든 SubmodelElementCollection을 {@link DefaultLinkedSegment}로 해석한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSegments extends SubmodelElementCollectionEntity implements Segments {
	private List<? extends DefaultSegment> segments;
	
	/**
	 * 빈 Segments 객체를 생성한다.
	 */
	public DefaultSegments() {
		setIdShort("Segments");
		setSemanticId(Segments.SEMANTIC_ID_REFERENCE);
	}
	
	/**
	 * 주어진 segment 리스트로 구성된 Segments 객체를 생성한다.
	 *
	 * @param segments	segment 리스트.
	 * @throws IllegalArgumentException	{@code segments}가 {@code null}이거나 빈 리스트인 경우.
	 */
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * 각 segment에 대해, 같은 idShort의 하위 SubmodelElement가 이미 있으면 segment 내용으로 갱신한 뒤 컬렉션의
	 * 끝으로 옮기고, 없으면 segment로부터 새로 생성한 SubmodelElement를 추가한다.
	 */
	@Override
	public void updateAasModel(SubmodelElement sme) {
		super.updateAasModel(sme);
		SubmodelElementCollection smc = (SubmodelElementCollection)sme;
		
		for ( DefaultSegment segment : segments ) {
			SubmodelElement child = SubmodelUtils.findFieldById(smc, segment.getIdShort());
			
			SubmodelElement target;
			if ( child != null ) {
				segment.updateAasModel(child);
				smc.getValue().remove(child);
				target = child;
			}
			else {
				target = segment.newSubmodelElement();
			}
			smc.getValue().add(target);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 하위의 모든 SubmodelElementCollection을 segment로 읽어 segment 리스트를 구성한다. semanticId가
	 * {@link TimeSeriesSemanticIds#INTERNAL_SEGMENT}이면 {@link DefaultInternalSegment}로, 그 밖에는
	 * {@link DefaultLinkedSegment}로 읽는다.
	 *
	 * @throws IllegalArgumentException	{@code model}이 {@code null}이거나
	 * 									{@link SubmodelElementCollection}이 아닌 경우.
	 */
	@Override
	public void updateFromAasModel(SubmodelElement model) {
		Preconditions.checkArgument(model != null, "model is null");
		Preconditions.checkArgument(model instanceof SubmodelElementCollection,
									"model is not SubmodelElementCollection, but=" + model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		
		segments = FStream.from(smc.getValue())
			                .castSafely(SubmodelElementCollection.class)
							.map(segSmc -> {
								DefaultSegment segment = newSegment(segSmc);
								segment.updateFromAasModel(segSmc);
								return segment;
							})
							.toList();
	}

	// ExternalSegment는 구현 클래스가 없으므로 LinkedSegment로 읽는다(기존 동작).
	private static DefaultSegment newSegment(SubmodelElementCollection segSmc) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(segSmc.getSemanticId());
		return TimeSeriesSemanticIds.INTERNAL_SEGMENT.equals(semanticId)
				? new DefaultInternalSegment()
				: new DefaultLinkedSegment();
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
