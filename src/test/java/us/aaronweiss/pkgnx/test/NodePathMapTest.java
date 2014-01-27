package us.aaronweiss.pkgnx.test;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.test.util.NodePathMap;
import us.aaronweiss.pkgnx.test.util.ResultSet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * A simple test of performance using the {@code NodePathMap}.
 *
 * @author Aaron Weiss
 * @version 1.0.0
 * @since 7/10/13
 */
public class NodePathMapTest {
	public static final Logger logger = LoggerFactory.getLogger(NodePathMapTest.class);
	public static final String FILE_PATH = "src/test/resources/Data-do.nx";
	public static final Stopwatch timer = Stopwatch.createUnstarted();
	public static final int CONSTRUCTION_TRIALS = 0x1;
	public static final int RESOLVE_TRIALS = 0x100;
	public static final int LOOKUP_TRIALS = 0x100;
	public static final boolean WARM_UP = true;
	public static final String lookup_path = "//String/Map.img/victoria/100000000/mapName";
	public static NXFile file;
	public static NodePathMap npm;

	public static void main(String[] args) throws IOException {
		logger.info("[pkgnx] initiating the node path map test.");
		file = new EagerNXFile(FILE_PATH);
		System.out.println("Name\t75%\tM50%\tBest");
		try {
			benchmark(NodePathMapTest.class.getDeclaredMethod("construction"), CONSTRUCTION_TRIALS);
			benchmark(NodePathMapTest.class.getDeclaredMethod("lookup"), LOOKUP_TRIALS);
			benchmark(NodePathMapTest.class.getDeclaredMethod("resolve"), RESOLVE_TRIALS);
			naive_lookup();
			naive_resolve();
		} catch (NoSuchMethodException e) {
			logger.error("[pkgnx] a test appears to be missing or incorrectly named.");
		}
		logger.info("[pkgnx] testing complete. Have a nice day. :D");
	}

	/**
	 * Benchmarks a specific {@code method} {@code trials} times.
	 *
	 * @param method the method to benchmark
	 * @param trials the number of times to benchmark it
	 */
	public static void benchmark(Method method, int trials) {
		ResultSet rs = new ResultSet(trials);
		logger.info("[" + method.getName() + "] initiating " + method.getName() + " benchmark.");
		for (int i = 0; i < trials; i++) {
			timer.start();
			try {
				method.invoke(null);
			} catch (Exception e) {
				logger.error("[" + method.getName() + "] failed with an exception.", e);
			}
			timer.stop();
			rs.add(timer.elapsed(TimeUnit.NANOSECONDS));
			logger.info("[" + method.getName() + "] trial " + i + ": " + timer.elapsed(TimeUnit.NANOSECONDS));
			timer.reset();
		}
		logger.info("[" + method.getName() + "] benchmark complete");
		logger.info("[" + method.getName() + "] " + rs.get75Percentile() + "\t" + rs.getAverage() + "\t" + rs.getBest());
		System.out.println(method.getName() + "\t" + rs.get75Percentile() + "\t" + rs.getAverage() + "\t" + rs.getBest());
	}

	public static void construction() {
		npm = new NodePathMap(file.getRoot());
	}

	public static void lookup() {
		npm.get(lookup_path);
	}

	public static void resolve() {
		file.resolve(lookup_path);
	}

	public static void naive_lookup() {
		Stopwatch extra = Stopwatch.createUnstarted();
		extra.start();
		for (int i = 0; i < LOOKUP_TRIALS; i++)
			npm.get(lookup_path);
		extra.stop();
		logger.error("naive lookup average: " + (extra.elapsed(TimeUnit.NANOSECONDS) / LOOKUP_TRIALS));
	}

	public static void naive_resolve() {
		Stopwatch extra = Stopwatch.createUnstarted();
		extra.start();
		for (int i = 0; i < LOOKUP_TRIALS; i++)
			file.resolve(lookup_path);
		extra.stop();
		logger.error("naive resolve average: " + (extra.elapsed(TimeUnit.NANOSECONDS) / LOOKUP_TRIALS));
	}
}
