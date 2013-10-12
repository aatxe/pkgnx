package us.aaronweiss.editnx.format;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import us.aaronweiss.editnx.util.Serializable;
import us.aaronweiss.editnx.util.StringEncoder;
import us.aaronweiss.pkgnx.util.Decompressor;
import us.aaronweiss.pkgnx.util.SeekableLittleEndianAccessor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 * @version 1.0
 * @since 10/11/13
 */
public class ModifiableNXTables implements Serializable {
	private final ModifiableNXHeader header;
	private List<String> strings;
	private List<Bitmap> bitmaps;
	private List<AudioBuf> audios;

	public ModifiableNXTables(ModifiableNXHeader header) {
		this.header = header;
		strings = new ArrayList<String>();
		bitmaps = new ArrayList<Bitmap>();
		audios = new ArrayList<AudioBuf>();
	}

	public void addString(String string) {
		strings.add(string);
		header.addString(string.length());
	}

	public void addBitmap(BufferedImage image) {
		bitmaps.add(new Bitmap(image));
	}

	public void addAudio(ByteBuf buf) {
		audios.add(new AudioBuf(buf));
	}

	@Override
	public ByteBuf serialize() {
		ByteBuf ret = Unpooled.buffer(strings.size() * 4 + 1);
		int stringTablePos = 0;
		ret.writerIndex(strings.size() * 4);
		for (String s : strings) {
			int dataIndex = ret.writerIndex();
			ret.writeShort(s.length());
			ret.writeBytes(StringEncoder.encode(s));
			ret.markWriterIndex();
			ret.writerIndex(stringTablePos * 4);
			ret.writeLong(dataIndex);
			ret.resetWriterIndex();
			stringTablePos++;
		}
		return ret;
	}

	/**
	 * A lazy-loaded equivalent of {@code ByteBuf}.
	 *
	 * @author Aaron Weiss
	 * @version 1.0
	 * @since 5/27/13
	 */
	private static class AudioBuf {
		private final SeekableLittleEndianAccessor slea;
		private long audioOffset;
		private ByteBuf audioBuf;

		/**
		 * Creates a lazy-loaded {@code ByteBuf} for audio.
		 *
		 * @param slea
		 */
		public AudioBuf(SeekableLittleEndianAccessor slea) {
			this.slea = slea;
			audioOffset = slea.getLong();
		}

		/**
		 * Loads a {@code ByteBuf} of the desired {@code length}.
		 *
		 * @param length the length of the audio
		 * @return the audio buffer
		 */
		public ByteBuf getAudioBuf(long length) {
			if (audioBuf == null) {
				slea.seek(audioOffset);
				audioBuf = Unpooled.wrappedBuffer(slea.getBytes((int) length));
			}
			return audioBuf;
		}
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
}
