package mdt.test;

import java.util.concurrent.ThreadLocalRandom;

import io.reactivex.rxjava3.core.Observable;

/**
 * RxJava3 {@code Observable}의 {@code map}/{@code subscribe} 동작을 확인하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestRxJava3 {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 */
	public static void main(String[] args) {
		Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
					.map(s -> intenseCalculation((s)))
					.subscribe(System.out::println);
		Observable.range(1, 6)
					.map(s -> intenseCalculation((s)))
					.subscribe(System.out::println);
	}
	
	/**
	 * 0~3초 사이의 임의 시간 동안 대기한 뒤 입력 값을 그대로 반환한다.
	 *
	 * @param <T>	값 타입.
	 * @param value	반환할 값.
	 * @return	입력 값 {@code value}.
	 */
	public static <T> T intenseCalculation(T value) {
		sleep(ThreadLocalRandom.current().nextInt(3000));
		return value;
	}
	
	/**
	 * 주어진 시간 동안 대기한다. 인터럽트되면 stack trace를 출력하고 반환한다.
	 *
	 * @param millis	대기 시간 (밀리초).
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}