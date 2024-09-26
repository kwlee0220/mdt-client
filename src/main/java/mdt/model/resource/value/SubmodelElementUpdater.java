package mdt.model.resource.value;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import mdt.model.DataType;
import mdt.model.DataTypes;
import utils.KeyValue;
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Tuple;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementUpdater {
	public void update(SubmodelElement element, SubmodelElementValue value) {
		if ( element instanceof Property prop ) {
			if ( value instanceof PropertyValue pv ) {
				updateProperty(prop, pv);
			}
			else {
				String msg = String.format("Incompatible type: Property <-> %s", value.getClass().getSimpleName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( element instanceof SubmodelElementCollection smec ) {
			if ( value instanceof SubmodelElementCollectionValue smecv ) {
				updateSubmodelElementCollection(smec, smecv);
			}
			else {
				String msg = String.format("Incompatible type: SubmodelElementCollection <-> %s",
											value.getClass().getSimpleName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( element instanceof SubmodelElementList smec ) {
			if ( value instanceof SubmodelElementListValue smecl ) {
				updateSubmodelElementList(smec, smecl);
			}
			else {
				String msg = String.format("Incompatible type: SubmodelElementList <-> %s",
											value.getClass().getSimpleName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( element instanceof MultiLanguageProperty mlp ) {
			if ( value instanceof MultiLanguagePropertyValue mlpv ) {
				updateMultiLanguageProperty(mlp, mlpv);
			}
			else {
				String msg = String.format("Incompatible type: MultiLanguageProperty <-> %s",
											value.getClass().getSimpleName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( element instanceof Range rg ) {
			if ( value instanceof RangeValue rgv ) {
				updateRange(rg, rgv);
			}
			else {
				String msg = String.format("Incompatible type: Range <-> %s",
											value.getClass().getSimpleName());
				throw new IllegalArgumentException(msg);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateProperty(Property prop, PropertyValue<?> value) {
		DataType dtype = DataTypes.fromAas4jDatatype(prop.getValueType());
		prop.setValue(dtype.toValueString(value.get()));
	}
	
	private void updateSubmodelElementCollection(SubmodelElementCollection smec,
													SubmodelElementCollectionValue value) {
		smec.setIdShort(value.getIdShort());
		FStream.from(value.get())
				.innerJoin(FStream.from(smec.getValue()), KeyValue::key, SubmodelElement::getIdShort)
				.map(t -> Tuple.of(t._1.value(), t._2))
				.forEach(t -> update(t._2, t._1));
	}

	private void updateSubmodelElementList(SubmodelElementList smel, SubmodelElementListValue value) {
		smel.setIdShort(value.getIdShort());
		FStream.from(value.get())
				.zipWith(FStream.from(smel.getValue()))
				.forEach(t -> update(t._2, t._1));
	}

	private void updateMultiLanguageProperty(MultiLanguageProperty mlp, MultiLanguagePropertyValue mlpv) {
		mlp.setIdShort(mlpv.getIdShort());
		for ( LangStringTextType langText: mlpv.get() ) {
			Funcs.replaceOrInsertFirst(mlp.getValue(),
										lt -> lt.getLanguage().equals(langText.getLanguage()),
										oldLt -> langText);
		}
	}

	private void updateRange(Range rg, RangeValue rgv) {
		rg.setIdShort(rgv.getIdShort());
		rg.setMin(FOption.map(rgv.getMin(), Object::toString));
		rg.setMax(FOption.map(rgv.getMax(), Object::toString));
	}
}
