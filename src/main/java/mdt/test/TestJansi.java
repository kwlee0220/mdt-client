package mdt.test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestJansi {
	public static final void main(String... args) throws Exception {
		System.out.println("sdfsdfsdfs");
		System.out.println(ansi().eraseScreen().fg(RED).a("Hello").fg(GREEN).a(" World").reset());
		System.out.println(ansi().cursor(1, 1));
		System.out.println(ansi().cursorUp(1));
		System.out.println("1111111111");
	}
}
