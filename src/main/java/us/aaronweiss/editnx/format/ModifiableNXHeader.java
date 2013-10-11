package us.aaronweiss.editnx.format;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import us.aaronweiss.editnx.ModifiableNXFile;
import us.aaronweiss.editnx.util.Serializable;
import us.aaronweiss.editnx.util.StringEncoder;

/**
 * @author Aaron
 * @version 1.0
 * @since 10/11/13
 */
public class ModifiableNXHeader implements Serializable {
	/**
	 * The expected "magic" file format string.
	 */
	public static final String MAGIC = "PKG4";

	/**
	 * The default offset for all data types.
	 */
	private static final long BASE_OFFSET = 64;

	/**
	 * The size of a node in bytes.
	 */
	private static final int NODE_SIZE = 20;

	private ModifiableNXFile file;
	private String magic;
	private long nodeCount, nodeOffset;
	private long stringCount, stringOffset;
	private long bitmapCount, bitmapOffset;
	private long soundCount, soundOffset;

	public ModifiableNXHeader() {
		magic = MAGIC;
		nodeCount = 0;
		nodeOffset = BASE_OFFSET;
		stringCount = 0;
		stringOffset = BASE_OFFSET;
		bitmapCount = 0;
		bitmapOffset = BASE_OFFSET;
		soundCount = 0;
		soundOffset = BASE_OFFSET;
	}

	/**
	 * Gets the {@code ModifiableNXFile} that the header was read from.
	 *
	 * @return the header's file
	 */
	public ModifiableNXFile getFile() {
		return file;
	}

	/**
	 * Gets the total number of nodes in the file.
	 *
	 * @return total number of nodes
	 */
	public long getNodeCount() {
		return nodeCount;
	}

	/**
	 * Modifies the header according to the addition of a node.
	 */
	public void addNode() {
		nodeCount++;
		stringOffset += NODE_SIZE;
		bitmapOffset += NODE_SIZE;
		soundOffset += NODE_SIZE;
	}

	/**
	 * Gets the first offset for the node block.
	 *
	 * @return first node offset
	 */
	public long getNodeOffset() {
		return nodeOffset;
	}

	/**
	 * Gets the total number of strings in the file.
	 *
	 * @return total number of strings
	 */
	public long getStringCount() {
		return stringCount;
	}

	/**
	 * Modifies the header according to the addition of a string.
	 *
	 * @param length the length of the string being added
	 */
	public void addString(int length) {
		stringCount++;
		bitmapOffset += 4; // size of offset table entry
		bitmapOffset += 2 + length; // size of string in string data block
		soundOffset += 4; // size of offset table entry
		soundOffset += 2 + length; // size of string in string data block
	}

	/**
	 * Gets the first offset for the string block.
	 *
	 * @return first string offset
	 */
	public long getStringOffset() {
		return stringOffset;
	}

	/**
	 * Gets the total number of bitmaps in the file.
	 *
	 * @return total number of bitmaps
	 */
	public long getBitmapCount() {
		return bitmapCount;
	}

	/**
	 * Modifies the header according to the addition of a bitmap.
	 *
	 * @param length the length of the image data
	 */
	public void addBitmap(int length) {
		bitmapCount++;
		soundOffset += 4; // size of offset table entry
		soundOffset += 4 + length; // size of bitmap in image data block
	}

	/**
	 * Gets the first offset for the bitmap block.
	 *
	 * @return first bitmap offset
	 */
	public long getBitmapOffset() {
		return bitmapOffset;
	}

	/**
	 * Gets the total number of MP3 sounds in the file.
	 *
	 * @return total number of MP3s
	 */
	public long getSoundCount() {
		return soundCount;
	}

	/**
	 * Modifies the header according to the addition of new audio data.
	 *
	 * @param length the length of the audio data
	 */
	public void addSound(int length) {
		soundCount++;
	}

	/**
	 * Gets the first offset for the MP3 sound block.
	 *
	 * @return first MP3 offset
	 */
	public long getSoundOffset() {
		return soundOffset;
	}

	@Override
	public ByteBuf serialize() {
		ByteBuf ret = Unpooled.buffer(52);
		ret.writeBytes(StringEncoder.encode(magic));
		ret.writeInt((int) nodeCount);
		ret.writeLong(nodeOffset);
		ret.writeInt((int) stringCount);
		ret.writeLong(stringOffset);
		ret.writeInt((int) bitmapCount);
		ret.writeLong(bitmapOffset);
		ret.writeInt((int) soundCount);
		ret.writeLong(soundOffset);
		return ret;
	}
}
