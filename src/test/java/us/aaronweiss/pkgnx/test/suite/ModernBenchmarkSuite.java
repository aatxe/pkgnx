/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2013 Aaron Weiss
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package us.aaronweiss.pkgnx.test.suite;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXException;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.NXNode;
import us.aaronweiss.pkgnx.nodes.NXBitmapNode;
import us.aaronweiss.pkgnx.test.util.ResultSet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * A more modern set of benchmarks aimed at providing real world data about library usage. These are compliant with the
 * official benchmark specification designed by Peter Atashian, Aaron Weiss, and angelsl. Do note that the number of
 * trials for each benchmark has a limit described in the specification. See the specification for more details.
 *
 * @author Aaron Weiss
 * @version 1.0.1
 * @since 6/24/13
 */
public class ModernBenchmarkSuite {
	public static final Logger logger = LoggerFactory.getLogger(ModernBenchmarkSuite.class);
	public static final String FILE_PATH = "src/test/resources/Data.nx";
	public static final Stopwatch timer = new Stopwatch();
	public static final int LD_TRIALS = 0x100;
	public static final int RE_TRIALS = 0x100;
	public static final int LR_TRIALS = 0x100;
	public static final int SA_TRIALS = 0x100;
	public static final int DE_TRIALS = 0x10;
	private static NXFile file;

	/**
	 * Performs the complete modern benchmarking suite.
	 *
	 * @param args none
	 * @throws IOException if {@code System.in} doesn't work
	 */
	public static void main(String[] args) throws IOException {
		logger.info("[pkgnx] press enter to start benchmarking.");
		System.in.read();
		logger.info("[pkgnx] initiating the full modern benchmarking suite.");
		System.out.println("Name\t75%\tM50%\tBest");
		try {
			benchmark(ModernBenchmarkSuite.class.getDeclaredMethod("Ld"), LD_TRIALS);
			benchmark(ModernBenchmarkSuite.class.getDeclaredMethod("Re"), RE_TRIALS);
			benchmark(ModernBenchmarkSuite.class.getDeclaredMethod("LR"), LR_TRIALS);
			benchmark(ModernBenchmarkSuite.class.getDeclaredMethod("SA"), SA_TRIALS);
			benchmark(ModernBenchmarkSuite.class.getDeclaredMethod("De"), DE_TRIALS);
		} catch (NoSuchMethodException e) {
			logger.error("[pkgnx] a benchmark appears to be missing or incorrectly named.");
		}
		logger.info("[pkgnx] benchmarking complete. Have a nice day. :D");
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
			rs.add(timer.elapsed(TimeUnit.MICROSECONDS));
			logger.info("[" + method.getName() + "] trial " + i + ": " + timer.elapsed(TimeUnit.MICROSECONDS));
			timer.reset();
		}
		logger.info("[" + method.getName() + "] benchmark complete");
		logger.info("[" + method.getName() + "] " + rs.get75Percentile() + "\t" + rs.getAverage() + "\t" + rs.getBest());
		System.out.println(method.getName() + "\t" + rs.get75Percentile() + "\t" + rs.getAverage() + "\t" + rs.getBest());
	}

	public static void Ld() throws IOException {
		file = new EagerNXFile(FILE_PATH);
	}

	public static void Re() throws IOException {
		RecurseHelper(file.getRoot());
	}

	public static void LR() throws IOException {
		file = new EagerNXFile(FILE_PATH);
		RecurseHelper(file.getRoot());
	}

	public static void SA() {
		StringRecurseHelper(file.getRoot());
	}

	public static void De() {
		DecompressHelper(file.getRoot());
	}

	public static void RecurseHelper(NXNode n) {
		for (NXNode c : n)
			RecurseHelper(c);
	}

	public static void StringRecurseHelper(NXNode n) {
		for (NXNode c : n) {
			if (n.getChild(c.getName()) == c)
				StringRecurseHelper(c);
			else
				throw new NXException("Equality test failed in SA benchmark.");
		}
	}

	public static void DecompressHelper(NXNode n) {
		if (n instanceof NXBitmapNode)
			((NXBitmapNode) n).getImage();
		for (NXNode c : n)
			DecompressHelper(c);
	}
}
