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
package us.aaronweiss.pkgnx.test.util;

import java.util.Arrays;

/**
 * A basic set of results from a benchmark that yields smart averages.
 *
 * @author Aaron Weiss
 * @version 1.1.0
 * @since 6/23/13
 */
public class ResultSet {
	public static final int DROP_RATIO_PERCENT = 50;
	private final long[] times;
	private final int drop;
	private int writer = 0;

	/**
	 * Creates a result set of the specified {@code length}.
	 *
	 * @param length the length of the results
	 */
	public ResultSet(int length) {
		times = new long[length];
		drop = (int) ((double) length / DROP_RATIO_PERCENT) / 2;
	}

	/**
	 * Adds a new result to the set.
	 *
	 * @param time the benchmark result to add
	 */
	public void add(long time) {
		times[writer++] = time;
	}

	/**
	 * Gets the 50% mean of the result set.
	 *
	 * @return the average of the median 50% of data entries
	 */
	public long getAverage() {
		if (times.length == 0)
			return -1;
		Arrays.sort(times);
		long total = 0;
		for (int i = drop; i < times.length - drop; i++) {
			total += times[i];
		}
		return total / (times.length - drop * 2);
	}

	/**
	 * Gets the best result from the result set.
	 *
	 * @return the best result
	 */
	public long getBest() {
		if (times.length == 0)
			return -1;
		Arrays.sort(times);
		return times[0];
	}

	/**
	 * Gets the "worst case-scenario" 75th percentile result from the set.
	 *
	 * @return the 75th percentile result
	 */
	public long get75Percentile() {
		if (times.length == 0)
			return -1;
		Arrays.sort(times);
		return times[times.length * 3 / 4];
	}
}