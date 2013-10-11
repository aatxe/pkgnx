package us.aaronweiss.editnx.util;

import io.netty.buffer.ByteBuf;

/**
 * @author Aaron
 * @version 1.0
 * @since 10/11/13
 */
public interface Serializable {
	/**
	 * Serializes this object down into a {@code ByteBuf}.
	 *
	 * @return the serialized data
	 */
	public ByteBuf serialize();
}
