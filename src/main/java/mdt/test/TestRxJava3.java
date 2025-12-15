package mdt.test;

import java.util.concurrent.ThreadLocalRandom;

import io.reactivex.rxjava3.core.Observable;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestRxJava3 {
	public static void main(String[] args) {
		Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
					.map(s -> intenseCalculation((s)))
					.subscribe(System.out::println);
		Observable.range(1, 6)
					.map(s -> intenseCalculation((s)))
					.subscribe(System.out::println);
	}
	
	public static <T> T intenseCalculation(T value) {
		sleep(ThreadLocalRandom.current().nextInt(3000));
		return value;
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}