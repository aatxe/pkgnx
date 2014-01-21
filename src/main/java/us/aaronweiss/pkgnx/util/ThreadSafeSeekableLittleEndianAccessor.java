/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 Aaron Weiss
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
package us.aaronweiss.pkgnx.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

/**
 * @author Aaron
 * @version 1.0
 * @since 1/21/14
 */
public class ThreadSafeSeekableLittleEndianAccessor extends SeekableLittleEndianAccessor {
	private final ThreadLocal<ByteBuf> localBuf;

	/**
	 * Creates an immutable, thread-safe {@code SeekableLittleEndianAccessor} from an array of bytes.
	 *
	 * @param bytes the array to use
	 */
	public ThreadSafeSeekableLittleEndianAccessor(byte[] bytes) {
		this(Unpooled.wrappedBuffer(bytes));
	}

	/**
	 * Creates an immutable, thread-safe {@code SeekableLittleEndianAccessor} from an NIO {@code ByteBuffer}.
	 *
	 * @param buf the buffer to use
	 */
	public ThreadSafeSeekableLittleEndianAccessor(ByteBuffer buf) {
		this(Unpooled.wrappedBuffer(buf));
	}

	/**
	 * Creates an immutable, thread-safe {@code SeekableLittleEndianAccessor} wrapping a {@code ByteBuf}.
	 *
	 * @param buf the buffer to wrap
	 */
	public ThreadSafeSeekableLittleEndianAccessor(final ByteBuf buf) {
		super(buf);
		localBuf = new ThreadLocal<ByteBuf>() {
			@Override
			protected ByteBuf initialValue() {
				return buf.duplicate();
			}
		};
	}

	@Override
	public ByteBuf getBuf() {
		return localBuf.get();
	}

	@Override
	public void skip(int length) {
		localBuf.get().skipBytes(length);
	}

	@Override
	public void seek(int offset) {
		localBuf.get().readerIndex(offset);
	}

	@Override
	public void mark() {
		localBuf.get().markReaderIndex();
	}

	@Override
	public void reset() {
		localBuf.get().resetReaderIndex();
	}

	@Override
	public byte getByte() {
		return localBuf.get().readByte();
	}

	@Override
	public short getUnsignedByte() {
		return localBuf.get().readUnsignedByte();
	}

	@Override
	public short getShort() {
		return localBuf.get().readShort();
	}

	@Override
	public int getUnsignedShort() {
		return localBuf.get().readUnsignedShort();
	}

	@Override
	public int getInt() {
		return localBuf.get().readInt();
	}

	@Override
	public long getUnsignedInt() {
		return localBuf.get().readUnsignedInt();
	}

	@Override
	public long getLong() {
		return localBuf.get().readLong();
	}

	@Override
	public float getFloat() {
		return localBuf.get().readFloat();
	}

	@Override
	public double getDouble() {
		return localBuf.get().readDouble();
	}

	@Override
	public byte[] getBytes(int length) {
		byte[] ret = new byte[length];
		localBuf.get().readBytes(ret);
		return ret;
	}

	@Override
	public ByteBuf getBuf(int length) {
		ByteBuf ret = Unpooled.buffer(length);
		localBuf.get().readBytes(ret);
		return ret;
	}
}
