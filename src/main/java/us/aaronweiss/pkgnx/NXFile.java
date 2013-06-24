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
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An memory-mapped file for reading specification-compliant NX files.
 *
 * @author Aaron Weiss
 * @version 1.2.0
 * @since 5/26/13
 */
public class NXFile {
	public static final Logger logger = LoggerFactory.getLogger(NXFile.class);
	private final SeekableLittleEndianAccessor slea;
	private boolean parsed;

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
		this(path, true);
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path} with the option to parse later.
	 *
	 * @param path              the absolute or relative path to the file
	 * @param parsedImmediately whether or not to parse all nodes immediately
	 * @throws IOException if something goes wrong in reading the file
	 */
	public NXFile(String path, boolean parsedImmediately) throws IOException {
		this(Paths.get(path), parsedImmediately);
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path} with the option to parse later.
	 *
	 * @param path              the absolute or relative path to the file
	 * @param parsedImmediately whether or not to parse the file immediately
	 * @throws IOException if something goes wrong in reading the file
	 */
	public NXFile(Path path, boolean parsedImmediately) throws IOException {
		FileChannel channel = FileChannel.open(path);
		slea = new SeekableLittleEndianAccessor(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
		if (parsedImmediately)
			parse();
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path} in the desired {@code mode}.
	 *
	 * @param path the absolute or relative path to the file
	 * @param mode the {@code LibraryMode} for handling this file
	 * @throws IOException if something goes wrong in reading the file
	 * @deprecated as of 1.2.0, users should use {@link #NXFile(String)} or {@link #NXFile(String, boolean)}
	 */
	@Deprecated
	public NXFile(String path, LibraryMode mode) throws IOException {
		this(Paths.get(path), mode);
	}

	/**
	 * Creates a new {@code NXFile} from the specified {@code path} in the desired {@code mode}.
	 *
	 * @param path the absolute or relative path to the file
	 * @param mode the {@code LibraryMode} for handling this file
	 * @throws IOException if something goes wrong in reading the file
	 * @deprecated as of 1.2.0, users should use {@link #NXFile(java.nio.file.Path)} or {@link #NXFile(java.nio.file.Path,
	 *             boolean)}
	 */
	@Deprecated
	public NXFile(Path path, LibraryMode mode) throws IOException {
		this(path, mode.isParsedImmediately());
	}

	/**
	 * Parses the file completely.
	 */
	public void parse() {
		if (parsed)
			return;
		header = new NXHeader(this, slea);
		nodes = new NXNode[(int) header.getNodeCount()];
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
	 * Gets whether or not this file has been parsed.
	 *
	 * @return whether or not this file has been parsed
	 */
	public boolean isParsed() {
		return parsed;
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
		if (path.equals("/"))
			return getRoot();
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

	/**
	 * An enumeration of possible modes for using pkgnx.
	 *
	 * @author Aaron Weiss
	 * @version 1.0.0
	 * @since 6/8/13
	 * @deprecated as of 1.2.0, the constructors using {@code LibraryMode} have been deprecated.
	 */
	@Deprecated
	public static enum LibraryMode {
		/**
		 * Fully loads file into memory and parses data on command.
		 */
		FULL_LOAD_ON_DEMAND(false, false),

		/**
		 * Parses data on command using a memory-mapped file.
		 */
		MEMORY_MAPPED(false, true),

		/**
		 * Fully loads file into memory and parses data immediately.
		 */
		PARSED_IMMEDIATELY(true, false),

		/**
		 * Parses data immediately using a memory-mapped file.
		 */
		MAPPED_AND_PARSED(true, true);
		private final boolean parsedImmediately, memoryMapped;

		/**
		 * Creates a new {@code LibraryMode} for pkgnx.
		 *
		 * @param parsedImmediately whether or not to parse on file construction
		 * @param memoryMapped      whether or not to use memory-mapped files
		 */
		private LibraryMode(boolean parsedImmediately, boolean memoryMapped) {
			this.parsedImmediately = parsedImmediately;
			this.memoryMapped = memoryMapped;
		}

		/**
		 * Gets whether or not this mode causes files to parse immediately.
		 *
		 * @return whether or not to parse on file construction
		 */
		public boolean isParsedImmediately() {
			return parsedImmediately;
		}

		/**
		 * Gets whether or not this mode uses memory mapped files.
		 *
		 * @return whether or not to use memory-mapped files
		 */
		public boolean isMemoryMapped() {
			return memoryMapped;
		}
	}
}
