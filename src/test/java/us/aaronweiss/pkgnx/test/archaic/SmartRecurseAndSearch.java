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
package us.aaronweiss.pkgnx.test.archaic;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.format.NXNode;
import us.aaronweiss.pkgnx.test.util.ResultSet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A benchmark that recurses over a file completely performing a string search along the way.
 *
 * @author Aaron Weiss
 * @version 1.0.0
 * @since 6/23/13
 * @deprecated this is now a part of {@link us.aaronweiss.pkgnx.test.suite.ModernBenchmarkSuite}.
 */
public class SmartRecurseAndSearch {
	public static final Logger logger = LoggerFactory.getLogger(SmartRecurseAndSearch.class);
	public static final String FILE_PATH = "src/test/resources/Data-do.nx";
	public static final Stopwatch timer = new Stopwatch();
	public static final int TRIALS = 0x20;

	public static void main(String[] args) throws IOException {
		NXFile file = new NXFile(FILE_PATH, NXFile.LibraryMode.MAPPED_AND_PARSED);
		ResultSet rs = new ResultSet(TRIALS);
		logger.info("[pkgnx] initiating smart Recurse and Search benchmark.");
		for (int i = 0; i < TRIALS; i++) {
			timer.start();
			recurse(file.getRoot());
			timer.stop();
			long time = timer.elapsed(TimeUnit.MICROSECONDS);
			logger.info("[RS] trial " + i + " - " + time + "");
			rs.add(time);
			timer.reset();
		}
		logger.info("[RS] " + rs.getAverage());
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
