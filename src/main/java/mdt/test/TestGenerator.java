package mdt.test;

import utils.stream.Generator;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestGenerator {
	public static final void main(String... args) throws Exception {
		Generator<String> gen = new Generator<String>(5) {
			@Override
			public void run() throws Throwable {
				this.yield("a");
				this.yield("b");
				this.yield("c");
			}
		};
		
		gen.forEach(System.out::println);
		
	}
}
