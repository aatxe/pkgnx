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
 * @version 1.0.0
 * @since 6/23/13
 */
public class ResultSet {
	public static final int DROP_RATIO_PERCENT = 50;
	private final long[] times;
	private final int drop;
	private int writer = 0;

	public ResultSet(int length) {
		times = new long[length];
		drop = (int) ((double) length / DROP_RATIO_PERCENT) / 2;
	}

	public void add(long time) {
		times[writer++] = time;
	}

	public long getAverage() {
		Arrays.sort(times);
		long total = 0;
		for (int i = drop; i < times.length - drop; i++) {
			total += times[i];
		}
		return total / (times.length - drop * 2);
	}
}