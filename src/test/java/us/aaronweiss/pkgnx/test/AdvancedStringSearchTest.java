/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 Aaron Weiss
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
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.NXNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * An advanced benchmark for pkgnx that enables viewers to see how fast node child access is on nodes with all different
 * numbers of children.
 *
 * @author Aaron Weiss
 * @version 1.1.0
 * @since 6/21/13
 */
public class AdvancedStringSearchTest {
	private static final Logger logger = LoggerFactory.getLogger(AdvancedStringSearchTest.class);
	private static final String FILE_PATH = "src/test/resources/Data-do.nx";
	private static final boolean MATHEMATICA_OUTPUT = true;
	private static final int SS_TRIALS = 64;
	private static final Stopwatch timer = Stopwatch.createUnstarted();

	private static Result[] results = new Result[1535];

	/**
	 * Runs the advanced String Search benchmark.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws IOException {
		NXFile file = new EagerNXFile(FILE_PATH);
		for (int i = 0; i < results.length; i++)
			results[i] = new Result();
		recurse(file.getRoot());
		if (MATHEMATICA_OUTPUT)
			System.out.print("data = {");
		for (int i = 0; i < results.length; i++) {
			if (results[i].totalRuns > 0) {
				if (i > 0)
					results[i].totalTime /= i;
				if (MATHEMATICA_OUTPUT) {
					System.out.print("{" + i + ", " + ((results[i].totalTime / results[i].totalRuns)) + "}" + ((i != results.length - 1) ? ", " : ""));
				} else {
					logger.info("[pkgnx] " + i + ": avg " + (results[i].totalTime / results[i].totalRuns));
				}
			}
		}
		if (MATHEMATICA_OUTPUT) {
			System.out.println("}");
			System.out.println("Fit[%, {1, x, x^2}, x]");
			System.out.println("Show[ListPlot[data], Plot[%, {x, 0, 1535}]]");
		} else {
			double total = 0;
			int runs = 0;
			for (int i = 0; i <= 10; i++) {
				total += results[i].totalTime;
				runs += results[i].totalRuns;
			}
			int n = results.length - 1;
			logger.info("[pkgnx] ratio: " + (results[n].totalTime / results[n].totalRuns) / (total / runs));
		}
	}

	/**
	 * Recurses through all the nodes and records information about the SS benchmark on them.
	 *
	 * @param node the node to recurse on
	 */
	private static void recurse(NXNode node) {
		int cc = node.getChildCount();
		for (int i = 0; i < SS_TRIALS; i++) {
			results[cc].totalTime += SS(node);
			results[cc].totalRuns++;
		}
		for (NXNode child : node) {
			recurse(child);
		}
	}

	/**
	 * Runs retep998's SS benchmark on a specific node.
	 * <p/>
	 * SS: String search; time taken to iterate through the  children of a {@code trialNode}, access each child by name,
	 * and compare the indexed child to the iterated child.
	 *
	 * @param trialNode the node to perform the SS trial on.
	 * @return time for the trial
	 */
	public static long SS(NXNode trialNode) {
		try {
			timer.start();
			for (NXNode child : trialNode) {
				if (!child.equals(trialNode.getChild(child.getName())))
					throw new RuntimeException("pkgnx is failing to work completely.");
			}
		} catch (Exception e) {
			timer.stop();
			timer.reset();
			logger.error("[SS] trial failed with an exception.", e);
			return -1;
		}
		timer.stop();
		long time = timer.elapsed(TimeUnit.NANOSECONDS);
		timer.reset();
		return time;
	}

	/**
	 * A benchmark result.
	 *
	 * @author Aaron Weiss
	 * @version 1.0.1
	 * @since 6/21/13
	 */
	public static class Result {
		public double totalTime = 0;
		public int totalRuns = 0;
	}
}
