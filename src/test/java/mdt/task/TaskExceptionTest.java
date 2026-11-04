package mdt.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;


/**
 * {@link TaskException#getMessage()}가 원인을 한 번만 보여 주는지 확인한다(mdt-client API 검토 D3, 2026-11-03).
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskExceptionTest {
	private static final IOException CAUSE = new IOException("x");

	@Test
	public void causeOnlyConstructorDoesNotRepeatCause() {
		assertEquals("java.io.IOException: x", new TaskException(CAUSE).getMessage());
	}

	@Test
	public void detailsAndCauseAppendCause() {
		assertEquals("fails to execute, cause=java.io.IOException: x",
					new TaskException("fails to execute", CAUSE).getMessage());
	}

	@Test
	public void causeAlreadyInDetailsIsNotRepeated() {
		assertEquals("Failed to set value: cause=java.io.IOException: x",
					new TaskException("Failed to set value: cause=" + CAUSE, CAUSE).getMessage());
	}

	@Test
	public void withoutCause() {
		assertEquals("bad", new TaskException("bad").getMessage());
		assertNull(new TaskException((Throwable)null).getMessage());
	}
}
