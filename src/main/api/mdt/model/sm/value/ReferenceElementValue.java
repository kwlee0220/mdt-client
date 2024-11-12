package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import com.fasterxml.jackson.core.JsonGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@AllArgsConstructor
public final class ReferenceElementValue implements DataElementValue {
	private Reference reference;
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		String serialized = ReferenceHelper.toString(this.reference);
		gen.writeString(serialized);
	}
}
