package mdt.cli;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.annotation.concurrent.GuardedBy;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.Guard;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ConsoleInputDetector extends AbstractThreadedExecution<Void> implements CancellableWork {
	private static final Duration TIMEOUT = Duration.ofSeconds(1);
	
	private List<Character> m_quitCharacters;
	private Runnable m_cancelCallback = null;
	
	private final Guard m_guard = Guard.create();
	static enum State { NOT_STARTED, RUNNING, STOP_REQUESTED, STOPPED }
	@GuardedBy("m_guard") private State m_state = State.NOT_STARTED;
	@GuardedBy("m_guard") private char m_input = '\0';
	
	public ConsoleInputDetector(List<Character> stopChars) {
		m_quitCharacters = stopChars;
	}
	
	public void setQuitCharacters(List<Character> stopChars) {
		m_quitCharacters = stopChars;
	}
	
	public void setCancelCallback(Runnable callback) {
		m_cancelCallback = callback;
		
		this.whenFinished(result -> {
			if ( m_cancelCallback != null ) {
				try {
					m_cancelCallback.run();
				}
				catch ( Throwable ignored ) { }
			}
		});
	}
	
	public char getDetectedInput() {
		return m_guard.get(() -> m_input);
	}
	
	@Override
	protected Void executeWork() throws InterruptedException, CancellationException, Exception {
		Terminal terminal = TerminalBuilder.terminal();
		terminal.enterRawMode();
		
		while ( continueWork() ) {
			char input = (char)terminal.reader().read();
			
			if ( m_quitCharacters.contains(input) ) {
				m_guard.run(() -> m_input = input);
				break;
			}
		}
		
		return null;
	}

	@Override
	public boolean cancelWork() {
		boolean cancelled = m_guard.getChecked(() -> {
			switch ( m_state ) {
				case NOT_STARTED:
				case STOPPED:
					return true;
				case RUNNING:
					m_state = State.STOP_REQUESTED;
					return false;
				case STOP_REQUESTED:
					return false;
				default:
					throw new IllegalStateException("Unknown state: " + m_state);
			}
		});
		
		if ( !cancelled ) {
			try {
				cancelled = m_guard.awaitCondition(() -> m_state == State.STOPPED, TIMEOUT).andReturn();
			}
			catch ( InterruptedException e ) {
				cancelled = false;
			}
		}
		
		return cancelled;
	}
	
	private boolean continueWork() {
		return m_guard.get(() -> {
			switch ( m_state ) {
				case NOT_STARTED:
					m_state = State.RUNNING;
					return true;
				case RUNNING:
					return true;
				case STOP_REQUESTED:
					m_state = State.STOPPED;
					return false;
				default:
					return false;
			}
		});
	}
	
	public static void main(String... args) throws Exception {
		ConsoleInputDetector detector = new ConsoleInputDetector(List.of('q', 'Q'));
		detector.start();
		
		System.out.println("Press 'q' or 'Q' to stop...");
		while ( detector.isRunning() ) {
			Thread.sleep(100);
			System.out.print(".");
		}
		
		char input = detector.getDetectedInput();
		System.out.println("Detected input: " + input);
	}
}
