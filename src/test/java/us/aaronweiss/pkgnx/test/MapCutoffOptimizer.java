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
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXNode;
import us.aaronweiss.pkgnx.test.util.ResultSet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A complex optimization test that enables developers to determine the optimal cutoff point for using maps to store
 * node children. Specifically, this helps determine the most efficient balance between load time and search time for
 * pkgnx. Nodes with the cutoff or more children will use a map internally whereas those below the cutoff will use an
 * array and binary searching. The results, as of v1.2.0, say that 41 children is the optimal cutoff. Be careful with
 * this benchmark, it can easily take a very long time.
 *
 * @author Aaron Weiss
 * @version 1.1.0
 * @since 6/23/13
 */
public class MapCutoffOptimizer {
	private static final Logger logger = LoggerFactory.getLogger(MapCutoffOptimizer.class);
	private static final String FILE_PATH = "src/test/resources/Data-do.nx";
	private static final Stopwatch timer = new Stopwatch();
	private static final int TRIALS = 0x20;

	/**
	 * Runs the advanced Map Cutoff Optimizer.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws IOException {
		long lowest = Long.MAX_VALUE;
		int lowestId = 12;
		for (int k = 12; k <= 100; k++) {
			// n.b. this is an optimization test, you must make MIN_COUNT_FOR_MAPS non-final and public first
			// NXNode.MIN_COUNT_FOR_MAPS = k;
			ResultSet rs1 = new ResultSet(TRIALS);
			NXFile file = null;
			for (int i = 0; i < TRIALS; i++) {
				timer.start();
				file = new EagerNXFile(FILE_PATH);
				timer.stop();
				long time = timer.elapsed(TimeUnit.MICROSECONDS);
				rs1.add(time);
				timer.reset();
			}
			ResultSet rs2 = new ResultSet(TRIALS);
			for (int i = 0; i < TRIALS; i++) {
				timer.start();
				recurse(file.getRoot());
				timer.stop();
				long time = timer.elapsed(TimeUnit.MICROSECONDS);
				rs2.add(time);
				timer.reset();
			}
			long product = rs1.getAverage() * rs2.getAverage();
			if (product < lowest) {
				lowest = product;
				lowestId = k;
			}
			logger.info("[RS] " + k + " " + product);
		}
		logger.info("[RS] BEST: " + lowestId + " " + lowest);
	}

	/**
	 * Recurses through all the nodes and records information about the SS benchmark on them.
	 *
	 * @param node the node to recurse on
	 */
	private static void recurse(NXNode node) {
		SS(node);
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
	public static void SS(NXNode trialNode) {
		try {
			for (NXNode child : trialNode) {
				if (!child.equals(trialNode.getChild(child.getName())))
					throw new RuntimeException("pkgnx is failing to work completely.");
			}
		} catch (Exception e) {
			logger.error("[SS] trial failed with an exception.", e);
		}
		return;
	}
}
