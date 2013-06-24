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
package us.aaronweiss.pkgnx.format.nodes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.format.NXHeader;
import us.aaronweiss.pkgnx.format.NXNode;
import us.aaronweiss.pkgnx.util.Decompressor;
import us.aaronweiss.pkgnx.util.SeekableLittleEndianAccessor;

import java.awt.image.BufferedImage;

/**
 * An {@code NXNode} representing a {@code Bitmap} as a {@code BufferedImage}.
 *
 * @author Aaron Weiss
 * @version 1.1.2
 * @since 5/27/13
 */
public class NXBitmapNode extends NXNode {
	private static Bitmap[] bitmaps;
	private final long bitmapIndex;
	private final int width, height;

	/**
	 * Creates a new {@code NXBitmapNode}.
	 *
	 * @param name       the name of the node
	 * @param file       the file the node is from
	 * @param childIndex the index of the first child of the node
	 * @param childCount the number of children
	 * @param slea       the {@code SeekableLittleEndianAccessor} to read from
	 */
	public NXBitmapNode(String name, NXFile file, long childIndex, int childCount, SeekableLittleEndianAccessor slea) {
		super(name, file, childIndex, childCount);
		bitmapIndex = slea.getUnsignedInt();
		width = slea.getUnsignedShort();
		height = slea.getUnsignedShort();
	}

	@Override
	public Object get() {
		return getImage();
	}

	/**
	 * Gets the value of this node as a {@code BufferedImage}.
	 *
	 * @return the node value
	 */
	public BufferedImage getImage() {
		if (bitmaps.length == 0)
			return null;
		return bitmaps[(int) bitmapIndex].getImage(width, height);
	}

	/**
	 * Populates the lazy-loaded table for {@code Bitmap}s.
	 *
	 * @param header the header corresponding to the file
	 * @param slea   the {@code SeekableLittleEndianAccessor} to read from
	 */
	public static void populateBitmapsTable(NXHeader header, SeekableLittleEndianAccessor slea) {
		bitmaps = new Bitmap[(int) header.getBitmapCount()];
		slea.seek(header.getBitmapOffset());
		for (int i = 0; i < bitmaps.length; i++)
			bitmaps[i] = new Bitmap(slea);
	}

	/**
	 * A lazy-loaded equivalent of {@code BufferedImage}.
	 *
	 * @author Aaron Weiss
	 * @version 1.0
	 * @since 5/27/13
	 */
	private static class Bitmap {
		private final SeekableLittleEndianAccessor slea;
		private final long bitmapOffset;

		/**
		 * Creates a lazy-loaded {@code BufferedImage}.
		 *
		 * @param slea
		 */
		public Bitmap(SeekableLittleEndianAccessor slea) {
			this.slea = slea;
			bitmapOffset = slea.getLong();
		}

		/**
		 * Loads a {@code BufferedImage} of the desired {@code width} and {@code height}.
		 *
		 * @param width  the width of the image
		 * @param height the height of the image
		 * @return the loaded image
		 */
		public BufferedImage getImage(int width, int height) {
			slea.seek(bitmapOffset);
			ByteBuf image = Unpooled.wrappedBuffer(Decompressor.decompress(slea.getBytes((int) slea.getUnsignedInt()), width * height * 4));
			BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					int b = image.readUnsignedByte();
					int g = image.readUnsignedByte();
					int r = image.readUnsignedByte();
					int a = image.readUnsignedByte();
					ret.setRGB(w, h, (a << 24) | (r << 16) | (g << 8) | b);
				}
			}
			return ret;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		else if (!(obj instanceof NXBitmapNode))
			return false;
		else
			return ((NXNode) obj).getName().equals(getName()) &&
					((NXNode) obj).getChildCount() == getChildCount() &&
					((NXNode) obj).getFirstChildIndex() == getFirstChildIndex() &&
					((NXBitmapNode) obj).bitmapIndex == bitmapIndex &&
					((NXBitmapNode) obj).height == height &&
					((NXBitmapNode) obj).width == width;
	}

}
