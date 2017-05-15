/**
 * 
 */
package com.ebao.cas.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * Copy from spring security org.springframework.security.crypto.codec.Utf8
 * 
 * @author brad.wu
 * @since 0.0.1
 * @version 0.0.1
 */
public final class Utf8 {
	private static final Charset CHARSET = Charset.forName("UTF-8");

	/**
	 * Get the bytes of the String in UTF-8 encoded form.
	 */
	public static byte[] encode(CharSequence string) {
		try {
			ByteBuffer bytes = CHARSET.newEncoder().encode(CharBuffer.wrap(string));
			byte[] bytesCopy = new byte[bytes.limit()];
			System.arraycopy(bytes.array(), 0, bytesCopy, 0, bytes.limit());

			return bytesCopy;
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Encoding failed", e);
		}
	}

	/**
	 * Decode the bytes in UTF-8 form into a String.
	 */
	public static String decode(byte[] bytes) {
		try {
			return CHARSET.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Decoding failed", e);
		}
	}
}
