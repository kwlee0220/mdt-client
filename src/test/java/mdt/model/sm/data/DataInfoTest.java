package mdt.model.sm.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import mdt.model.ResourceNotFoundException;


/**
 * {@link DataInfo#getEquipment()}·{@link DataInfo#getOperation()}가 엔티티가 없을 때 의미 있는 메시지의
 * {@link ResourceNotFoundException}을 그대로 던지는지 확인한다(mdt-client API 검토 D6, 2026-11-03).
 * <p>
 * 2026-11-03 이전에는 원래 예외를 버리고 {@code "Equipment()"}·{@code "Operation()"}로 다시 던졌다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DataInfoTest {
	private static final DataInfo EMPTY = List::of;

	@Test
	public void missingEquipmentNamesTheType() {
		ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, EMPTY::getEquipment);
		assertEquals("DataInfo entity(type=Equipment)", e.getMessage());
	}

	@Test
	public void missingOperationNamesTheType() {
		ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, EMPTY::getOperation);
		assertEquals("DataInfo entity(type=Operation)", e.getMessage());
	}
}
