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
package us.aaronweiss.pkgnx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.format.NXHeader;
import us.aaronweiss.pkgnx.format.NXNode;
import us.aaronweiss.pkgnx.format.nodes.NXAudioNode;
import us.aaronweiss.pkgnx.format.nodes.NXBitmapNode;
import us.aaronweiss.pkgnx.format.nodes.NXStringNode;
import us.aaronweiss.pkgnx.util.NodeParser;
import us.aaronweiss.pkgnx.util.SeekableLittleEndianAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * An object for reading PKG4 NX files.
 *
 * @author Aaron Weiss
 * @version 1.0
 * @since 5/26/13
 */
public class NXFile {
	public static final Logger logger = LoggerFactory.getLogger(NXFile.class);
	private final SeekableLittleEndianAccessor slea;
	private boolean parsed = false;

	private NXHeader header;
	private NXNode[] nodes;

	/**
	 * Creates a new {@code NXFile} from the specified {@code path}.
	 *
	 * @param path the absolute or relative path to the file
	 * @throws IOException if something goes wrong in reading the file
	 */
	public NXFile(String path) throws IOException {
		this(Paths.get(path));
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path}.
	 *
	 * @param path the absolute or relative path to the file
	 * @throws IOException if something goes wrong in reading the file
	 */
	public NXFile(Path path) throws IOException {
		this(path, false);
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path} with the option to parse later.
	 *
	 * @param path             the absolute or relative path to the file
	 * @param parseImmediately whether or not to parse all nodes immediately
	 * @throws IOException if something goes wrong in reading the file
	 */
	public NXFile(String path, boolean parseImmediately) throws IOException {
		this(Paths.get(path), parseImmediately);
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path} with the option to parse later.
	 *
	 * @param path             the absolute or relative path to the file
	 * @param parseImmediately whether or not to parse the file immediately
	 * @throws IOException if something goes wrong in reading the file
	 */
	public NXFile(Path path, boolean parseImmediately) throws IOException {
		slea = new SeekableLittleEndianAccessor(Files.readAllBytes(path));

		if (parseImmediately)
			parse();
	}

	/**
	 * Parses the file completely.
	 */
	public void parse() {
		if (parsed)
			return;
		header = new NXHeader(this, slea);
		logger.debug("String Count : " + getHeader().getStringCount());
		logger.debug("String Offset: " + getHeader().getStringOffset());
		NXStringNode.populateStringTable(header, slea);
		NXBitmapNode.populateBitmapsTable(header, slea);
		NXAudioNode.populateAudioBufTable(header, slea);
		populateNodesTable();
		populateNodeChildren();
		parsed = true;
	}

	/**
	 * Populates the node table by parsing all nodes.
	 */
	private void populateNodesTable() {
		slea.seek(header.getNodeOffset());
		nodes = new NXNode[(int) header.getNodeCount()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = NodeParser.parseNode(header, slea);
		}
	}

	/**
	 * Populates the children of all nodes.
	 */
	private void populateNodeChildren() {
		for (NXNode node : nodes) {
			node.populateChildren();
		}
	}

	/**
	 * Gets the {@code NXHeader} of this file.
	 *
	 * @return this file's header
	 */
	public NXHeader getHeader() {
		return header;
	}

	/**
	 * Gets an array of all of the {@code NXNode}s in this file.
	 *
	 * @return an array of all the nodes in this file
	 */
	public NXNode[] getNodes() {
		return nodes;
	}

	/**
	 * Gets the root {@code NXNode} of the file.
	 *
	 * @return the file's root node
	 */
	public NXNode getRoot() {
		return nodes[0];
	}

	/**
	 * Resolves the desired {@code path} to an {@code NXNode}.
	 *
	 * @param path the path to the node
	 * @return the desired node
	 */
	public NXNode resolve(String path) {
		return resolve(path.split("/"));
	}

	/**
	 * Resolves the desired {@code path} to an {@code NXNode}.
	 *
	 * @param path the path to the node
	 * @return the desired node
	 */
	public NXNode resolve(String[] path) {
		NXNode cursor = getRoot();
		for (int i = 0; i < path.length; i++) {
			if (cursor == null)
				return null;
			cursor = cursor.getChild(path[i]);
		}
		return cursor;
	}
}
