/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import android.util.Base64;

/**
 * A simple utility class for Base64 encoding and decoding that delegates to Android's
 * {@link android.util.Base64} class.
 *
 * @author Juergen Hoeller
 * @author Roy Clarkson
 * @since 1.0
 */
public abstract class Base64Utils {

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private static final Base64Delegate delegate = new AndroidBase64Delegate();


	/**
	 * Base64-encode the given byte array.
	 * @param src the original byte array (may be {@code null})
	 * @return the encoded byte array (or {@code null} if the input was {@code null})
	 */
	public static byte[] encode(byte[] src) {
		return delegate.encode(src);
	}

	/**
	 * Base64-encode the given byte array to a String.
	 * @param src the original byte array (may be {@code null})
	 * @return the encoded byte array as a UTF-8 String
	 * (or {@code null} if the input was {@code null})
	 */
	public static String encodeToString(byte[] src) {
		if (src == null) {
			return null;
		}
		if (src.length == 0) {
			return "";
		}
		String result;
		try {
			result = new String(delegate.encode(src), DEFAULT_CHARSET.displayName());
		} catch (UnsupportedEncodingException e) {
			// should not happen, UTF-8 is always supported
			throw new IllegalStateException(e);
		}
		return result;
	}

	/**
	 * Base64-decode the given byte array.
	 * @param src the encoded byte array (may be {@code null})
	 * @return the original byte array (or {@code null} if the input was {@code null})
	 */
	public static byte[] decode(byte[] src) {
		return delegate.decode(src);
	}

	/**
	 * Base64-decode the given byte array from an UTF-8 String.
	 * @param src the encoded UTF-8 String (may be {@code null})
	 * @return the original byte array (or {@code null} if the input was {@code null})
	 * @deprecated in favor of {@link #decodeFromString(String)}
	 */
	@Deprecated
	public static byte[] decode(String src) {
		return decodeFromString(src);
	}

	/**
	 * Base64-decode the given byte array from an UTF-8 String.
	 * @param src the encoded UTF-8 String (may be {@code null})
	 * @return the original byte array (or {@code null} if the input was {@code null})
	 * @since 2.0
	 */
	public static byte[] decodeFromString(String src) {
		if (src == null) {
			return null;
		}
		if (src.length() == 0) {
			return new byte[0];
		}
		byte[] result;
		try {
			result = delegate.decode(src.getBytes(DEFAULT_CHARSET.displayName()));
		} catch (UnsupportedEncodingException e) {
			// should not happen, UTF-8 is always supported
			throw new IllegalStateException(e);
		}
		return result;
	}


	private interface Base64Delegate {

		byte[] encode(byte[] src);

		byte[] decode(byte[] src);
	}


	private static class AndroidBase64Delegate implements Base64Delegate {

		public byte[] encode(byte[] src) {
			if (src == null || src.length == 0) {
				return src;
			}
			return Base64.encode(src, Base64.DEFAULT | Base64.NO_WRAP);
		}

		public byte[] decode(byte[] src) {
			if (src == null || src.length == 0) {
				return src;
			}
			return Base64.decode(src, Base64.DEFAULT | Base64.NO_WRAP);
		}
	}

}