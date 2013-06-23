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
package us.aaronweiss.pkgnx.test;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.format.NXNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A complete benchmarking suite for pkgnx, compliant with the official benchmarks.
 *
 * @author Aaron Weiss
 * @version 1.1.0
 * @since 5/27/13
 */
public class BenchmarkSuite {
	public static final Logger logger = LoggerFactory.getLogger(BenchmarkSuite.class);
	public static final String FILE_PATH = "src/test/resources/Data-do.nx";
	public static final int LD_TRIALS = 16;
	public static final int SS_TRIALS = 100;
	public static final int PR_TRIALS = 16;
	public static final int RE_TRIALS = 32;
	public static final Stopwatch timer = new Stopwatch();

	/**
	 * Runs the full benchmark suite.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws IOException {
		logger.info("[pkgnx] press enter to begin benchmarking.");
		System.in.read();
		logger.info("[pkgnx] initiating full benchmark suite.");
		long Ld = Ld();
		long SS = SS();
		long PR = PR();
		long Re = Re();
		logger.info("[pkgnx] " + Ld + " " + SS + " " + PR + " " + Re);
	}

	/**
	 * Runs retep998's Ld benchmark.
	 * <p/>
	 * Ld: Load time; time taken for a single load. Time reported is the best of between 3 to 65536 runs.
	 *
	 * @return best time out of all trials
	 */
	public static long Ld() {
		logger.info("[Ld] initiating Ld benchmark for " + LD_TRIALS + " trials.");
		long total = 0;
		long best = Long.MAX_VALUE;
		for (int i = 0; i < LD_TRIALS; i++) {
			timer.start();
			try {
				NXFile file = new NXFile(FILE_PATH, NXFile.LibraryMode.MAPPED_AND_PARSED);
			} catch (IOException e) {
				logger.error("[Ld] trial failed with an exception.", e);
				return -1;
			}
			timer.stop();
			long time = timer.elapsed(TimeUnit.MICROSECONDS);
			logger.info("[Ld] trial " + i + " - " + time + "");
			total += time;
			if (time < best)
				best = time;
			timer.reset();
		}
		logger.info("[Ld] " + total + " " + (total / LD_TRIALS) + " " + best);
		return best;
	}

	/**
	 * Runs retep998's SS benchmark.
	 * <p/>
	 * SS: String search; time taken to iterate through the 1534 children of Data.wz/Map/Map/Map1/105060000.img/1/tile,
	 * access each child by name, and compare the indexed child to the iterated child.
	 *
	 * @return best time out of all trials
	 */
	public static long SS() {
		logger.info("[SS] initiating SS benchmark.");
		long total = 0;
		long best = Long.MAX_VALUE;
		NXFile file;
		try {
			file = new NXFile(FILE_PATH, NXFile.LibraryMode.MAPPED_AND_PARSED);
		} catch (Exception e) {
			logger.error("[SS] trial failed with an exception.", e);
			return -1;
		}
		for (int i = 0; i < SS_TRIALS; i++) {
			try {
				timer.start();
				NXNode trialNode = file.getRoot().getChild("Map").getChild("Map").getChild("Map1").getChild("105060000.img")
						.getChild("1").getChild("tile");
				for (NXNode child : trialNode) {
					if (!child.equals(trialNode.getChild(child.getName())))
						throw new RuntimeException("pkgnx is failing to work completely.");
				}
			} catch (Exception e) {
				logger.error("[SS] trial failed with an exception.", e);
				return -1;
			}
			timer.stop();
			long time = timer.elapsed(TimeUnit.MICROSECONDS);
			logger.info("[SS] trial " + i + " - " + time + "");
			total += time;
			if (time < best)
				best = time;
			timer.reset();
		}
		logger.info("[SS] " + total + " " + (total / SS_TRIALS) + " " + best);
		return best;
	}

	/**
	 * Runs retep998's PR benchmark.
	 * <p/>
	 * PR: Parse and Recurse; time taken to cleanly parse a file and then recurse through every single node.
	 *
	 * @return the time it took for the benchmark
	 */
	public static long PR() {
		logger.info("[PR] initiating PR benchmark for " + PR_TRIALS + " trials.");
		long total = 0;
		long best = Long.MAX_VALUE;
		for (int i = 0; i < PR_TRIALS; i++) {
			try {
				NXFile file = new NXFile(FILE_PATH);
				timer.start();
				file.parse();
				recurse(file.getRoot());
			} catch (Exception e) {
				logger.error("[PR] trial failed with an exception.", e);
				return -1;
			}
			timer.stop();
			long time = timer.elapsed(TimeUnit.MICROSECONDS);
			total += time;
			if (time < best)
				best = time;
			logger.info("[PR] " + time);
			timer.reset();
		}
		logger.info("[PR] " + total + " " + (total / PR_TRIALS) +  " " + best);
		return best;
	}

	/**
	 * Recursively accesses a node's children.
	 *
	 * @param node the node to recurse
	 */
	private static void recurse(NXNode node) {
		for (NXNode child : node)
			recurse(child);
	}

	/**
	 * Runs retep998's Re benchmark.
	 * <p/>
	 * Re: Recurse; time taken to recurse through a file. The file used is the same file as Ld and SS so some nodes may
	 * already have been parsed.
	 *
	 * @return the time it took for the benchmark
	 */
	public static long Re() {
		logger.info("[Re] initiating Re benchmark for " + RE_TRIALS + " trials.");
		long total = 0;
		long best = Long.MAX_VALUE;
		for (int i = 0; i < RE_TRIALS; i++) {
			try {
				NXFile file = new NXFile(FILE_PATH, NXFile.LibraryMode.MAPPED_AND_PARSED);
				timer.start();
				recurse(file.getRoot());
			} catch (Exception e) {
				logger.error("[Re] trial failed with an exception.", e);
				return -1;
			}
			timer.stop();
			long time = timer.elapsed(TimeUnit.MICROSECONDS);
			total += time;
			if (time < best)
				best = time;
			logger.info("[Re] " + time);
			timer.reset();
		}
		logger.info("[Re] " + total + " " + (total / RE_TRIALS) +  " " + best);
		return best;
	}
}
