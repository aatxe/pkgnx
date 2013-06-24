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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.test.suite.BenchmarkSuite;

import java.io.IOException;

/**
 * @author Aaron
 * @version 1.0
 * @since 6/11/13
 */
public class LatestGameTest {
	public static final Logger logger = LoggerFactory.getLogger(BenchmarkSuite.class);
	public static final String FILE_PATH = "src/test/resources/";

	/**
	 * Runs the full benchmark suite.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) {
		logger.info("[pkgnx] Loading latest game content.");
		String[] files = {"Base.nx", "Character.nx", "Effect.nx", "Etc.nx", "Item.nx",
				"Map.nx", "Mob.nx", "Morph.nx", "Npc.nx", "Quest.nx", "Reactor.nx",
				"Skill.nx", "Sound.nx", "String.nx", "TamingMob.nx", "UI.nx"};
		try {
			for (String file : files) {
				NXFile nx = new NXFile(FILE_PATH + file, NXFile.LibraryMode.MAPPED_AND_PARSED);
			}
		} catch (IOException e) {
			logger.error("[pkgnx] Failed to load a file.", e);
		}
		Runtime rt = Runtime.getRuntime();
		logger.info("[pkgnx] Loading completed in " + "μs for " + ((rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)) + " MB.");
	}
}
