package us.aaronweiss.editnx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * @author Aaron
 * @version 1.0
 * @since 10/11/13
 */
public class StringEncoder {
	private final static Logger logger = LoggerFactory.getLogger(StringEncoder.class);
	private static final CharsetEncoder utfEncoder = Charset.forName("UTF-8").newEncoder();

	public static byte[] encode(String string) {
		try {
			return utfEncoder.encode(CharBuffer.wrap(string)).array();
		} catch (CharacterCodingException e) {
			logger.error("Failed to encode String into bytes.", e);
		}
		return null;
	}
}
