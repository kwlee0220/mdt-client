package mdt.tree.sm;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;

import lombok.experimental.UtilityClass;

import utils.func.FOption;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public final class DataElementNodes  {
	public static TerminalNode fromProperty(String prefix, String id, Property prop) {
		return new TerminalNode() {
			@Override
			public String getText() {
				if ( prop.getValueType() != null ) {
					return String.format("%s%s (%s): %s", prefix, id, prop.getValueType(), prop.getValue());
				}
				else {
					return String.format("%s%s: %s", prefix, id, prop.getValue());
				}
			}
		};
	}
	
	public static TerminalNode fromFile(String prefix, String id, File aasFile) {
		return new TerminalNode() {
			@Override
			public String getText() {
				String path = FOption.getOrElse(aasFile.getValue(), "None");
				return String.format("%s%s (FILE): %s (%s)",
									prefix, id, path, aasFile.getContentType());
			}
		};
	}
}
