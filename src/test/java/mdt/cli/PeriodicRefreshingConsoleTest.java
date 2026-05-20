package mdt.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utils.func.FOption;


/**
 * {@link PeriodicRefreshingConsole}의 출력 포맷 단위 테스트.
 * <p>
 * 전체 async 라이프사이클을 돌리는 대신 protected {@code performPeriodicAction(long)}을
 * 같은 패키지 서브클래스에서 직접 호출하여 출력만 검증한다. 이렇게 하면 JLine terminal을
 * 시작하는 {@code initializeLoop()}을 회피할 수 있어 비-TTY (CI) 환경에서도 안정적이다.
 */
public class PeriodicRefreshingConsoleTest {
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	private PrintStream m_originalOut;
	private ByteArrayOutputStream m_capturedOut;

	@Before
	public void redirectStdout() {
		m_originalOut = System.out;
		m_capturedOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(m_capturedOut, true, StandardCharsets.UTF_8));
	}

	@After
	public void restoreStdout() {
		System.setOut(m_originalOut);
	}

	private String captured() {
		return m_capturedOut.toString(StandardCharsets.UTF_8);
	}

	/**
	 * 한 주기 분량의 출력이 ANSI clear 시퀀스로 시작해야 한다.
	 */
	@Test
	public void outputStartsWithAnsiClearSequence() throws Exception {
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> pw.print("hello"));
		console.performPeriodicAction(0);

		Assert.assertTrue("output should start with ANSI clear sequence: " + captured(),
							captured().startsWith(CLEAR_CONSOLE_CONTROL));
	}

	/**
	 * {@link PeriodicRefreshingConsole#print(PrintWriter)}에 쓴 내용이 출력에 포함되어야 한다.
	 */
	@Test
	public void outputContainsPrintedContent() throws Exception {
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> pw.print("hello world"));
		console.performPeriodicAction(0);

		Assert.assertTrue("captured output should contain printed content: " + captured(),
							captured().contains("hello world"));
	}

	/**
	 * {@code print()}는 한 주기마다 정확히 한 번 호출되어야 한다.
	 */
	@Test
	public void printIsCalledOncePerIteration() throws Exception {
		AtomicInteger printCount = new AtomicInteger();
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> {
			printCount.incrementAndGet();
			pw.print("x");
		});

		console.performPeriodicAction(0);
		console.performPeriodicAction(1);
		console.performPeriodicAction(2);

		Assert.assertEquals(3, printCount.get());
	}

	/**
	 * verbose 모드에서는 출력 끝에 {@code elapsed:} 라인이 추가되어야 한다.
	 */
	@Test
	public void verboseModeAppendsElapsedLine() throws Exception {
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> pw.print("body"));
		console.setVerbose(true);
		console.performPeriodicAction(0);

		Assert.assertTrue("verbose output should include 'elapsed:': " + captured(),
							captured().contains("elapsed:"));
	}

	/**
	 * 기본 (verbose 비활성) 모드에서는 {@code elapsed:} 라인이 없어야 한다.
	 */
	@Test
	public void nonVerboseModeOmitsElapsedLine() throws Exception {
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> pw.print("body"));
		console.performPeriodicAction(0);

		Assert.assertFalse("non-verbose output should not include 'elapsed:': " + captured(),
							captured().contains("elapsed:"));
	}

	/**
	 * UTF-8 본문 (한글 등)이 출력 시 손상되지 않아야 한다.
	 */
	@Test
	public void utf8ContentIsPreserved() throws Exception {
		String korean = "안녕하세요 👋";
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> pw.print(korean));
		console.performPeriodicAction(0);

		Assert.assertTrue("UTF-8 content should be preserved: " + captured(),
							captured().contains(korean));
	}

	/**
	 * {@code performPeriodicAction}은 정상 진행 시 {@link FOption#empty()}를 반환해야 한다.
	 */
	@Test
	public void performPeriodicActionReturnsEmptyOnNormalIteration() throws Exception {
		PeriodicRefreshingConsole console = new RecordingConsole(pw -> pw.print("ok"));
		FOption<Void> result = console.performPeriodicAction(0);

		Assert.assertTrue("result should be empty FOption", result.isAbsent());
	}

	// --- helpers ---

	/**
	 * 테스트용 {@link PeriodicRefreshingConsole} 서브클래스.
	 * {@code print()} 호출을 외부에서 주입한 {@link CheckedPrinter}로 위임한다.
	 */
	private static class RecordingConsole extends PeriodicRefreshingConsole {
		private final CheckedPrinter m_printer;

		RecordingConsole(CheckedPrinter printer) {
			super(Duration.ofMillis(50));
			m_printer = printer;
		}

		@Override
		protected void print(PrintWriter pw) {
			m_printer.print(pw);
		}
	}

	@FunctionalInterface
	private interface CheckedPrinter {
		void print(PrintWriter pw);
	}
}
