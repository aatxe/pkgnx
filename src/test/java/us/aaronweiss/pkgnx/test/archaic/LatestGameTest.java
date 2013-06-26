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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.format.NXNode;

import java.io.IOException;

/**
 * A test that loads all of the modern game data and outputs some information.
 *
 * @author Aaron
 * @version 1.1.0
 * @since 6/11/13
 * @deprecated since 1.2.0, the information held within was deemed useless.
 */
@Deprecated
public class LatestGameTest {
	private static final Logger logger = LoggerFactory.getLogger(LatestGameTest.class);
	private static final String FILE_PATH = "src/test/resources/137/";

	/**
	 * Loads all of the modern game data.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws IOException {
		System.in.read();
		logger.info("[pkgnx] Loading latest game content.");
		String[] files = {"Base.nx", "Character.nx", "Effect.nx", "Etc.nx", "Item.nx",
				"Map.nx", "Mob.nx", "Morph.nx", "Npc.nx", "Quest.nx", "Reactor.nx",
				"Skill.nx", "Sound.nx", "String.nx", "TamingMob.nx", "UI.nx"};
		NXFile[] loaded = new NXFile[files.length];
		try {
			for (int i = 0; i < loaded.length; i++) {
				loaded[i] = new NXFile(FILE_PATH + files[i]);
			}
		} catch (IOException e) {
			logger.error("[pkgnx] Failed to load a file.", e);
		}
		logger.info("[pkgnx] Loading completed.");
		System.in.read();
		logger.info("[pkgnx] Initiating recursion.");
		for (NXFile file : loaded)
			if (file.getFilePath().contains("Character"))
				recurse(file.getRoot());
		logger.info("[pkgnx] Recursion complete.");
		System.in.read();
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
			logger.error("[SS] trial failed in " + trialNode.getFile().getFilePath(), e);
		}
		return;
	}
}
