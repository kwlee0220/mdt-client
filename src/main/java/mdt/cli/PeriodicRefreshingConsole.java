package mdt.cli;

import java.io.ByteArrayOutputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import utils.MovingAverage;
import utils.StopWatch;
import utils.UnitUtils;
import utils.async.PeriodicLoopExecution;
import utils.func.FOption;
import utils.func.Try;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class PeriodicRefreshingConsole extends PeriodicLoopExecution<Void> {
	private static final List<Character> QUIT_CHARACTERS = List.of('q', 'Q', '\u001B'); // 'ESC'
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	private static final float DEFAULT_MAVG_ALPHA = 0.1f;
	
	private ConsoleInputDetector m_quitDetector = new ConsoleInputDetector(QUIT_CHARACTERS);
	private final MovingAverage m_mavg = new MovingAverage(DEFAULT_MAVG_ALPHA);
	private boolean m_verbose = false;
	
	abstract protected void print(PrintWriter pw) throws Exception;
	
	public PeriodicRefreshingConsole(Duration repeatInterval) {
		super(repeatInterval);
	}

	public void setQuitCharacterCanidates(List<Character> quitChars) {
		m_quitDetector.setQuitCharacters(quitChars);
	}
	
	public void setQuitCallback(Runnable callback) {
		m_quitDetector.setCancelCallback(callback);
	}
	
	public void setVerbose(boolean verbose) {
		m_verbose = verbose;
	}
	
	public Void run() {
		Try.run(super::run);
		return null;
	}
	
	@Override
	protected void initializeLoop() throws Exception {
		final Thread currentThread = Thread.currentThread();
		setQuitCallback(currentThread::interrupt);
		
		m_quitDetector.start();
	}
	
	@Override
	protected FOption<Void> performPeriodicAction(long loopIndex) throws Exception {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos) ) {
			StopWatch watch = StopWatch.start();
			
			print(pw);
			pw.flush();
			
			String outputString = baos.toString();
			System.out.print(CLEAR_CONSOLE_CONTROL);
			System.out.print(outputString);
			
			if ( m_verbose ) {
				watch.stop();
				
				double avg = m_mavg.observe(watch.getElapsedInFloatingSeconds());
				String secStr = UnitUtils.toMillisString(Math.round(avg * 1000));
				System.out.println("elapsed: " + secStr);
			}
		}
		catch ( InterruptedIOException e ) {
			// IO 중에 중단된 경우에도 cancel된 것으로 간주함.
			return null;
		}
		
		if ( !isRunning() ) {
			return null;
		}
		else {
			return FOption.empty();
		}
	}
	
	@Override
	protected void finalizeLoop() throws Exception {
		CompletableFuture.runAsync(() -> {
			m_quitDetector.cancel(true);
		});
	}
}
