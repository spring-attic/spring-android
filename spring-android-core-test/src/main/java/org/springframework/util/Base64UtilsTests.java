package org.springframework.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.springframework.android.test.Assert;

import android.test.AndroidTestCase;

public class Base64UtilsTests extends AndroidTestCase {
	
	private static final String UTF8 = Charset.forName("UTF-8").displayName();
	
	private static final String SOURCE = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
	
	private static final String ENCODED = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";

	public void testEncodeBytes() throws UnsupportedEncodingException {
		byte[] expected = ENCODED.getBytes();
		byte[] actual = Base64Utils.encode(SOURCE.getBytes(UTF8));
		Assert.assertArrayEquals(expected, actual);
	}

	public void testEncodeToString() throws UnsupportedEncodingException {
		String actual = Base64Utils.encodeToString(SOURCE.getBytes(UTF8));
		assertEquals(ENCODED, actual);
	}

	public void testDecodeBytes() throws UnsupportedEncodingException {
		byte[] expected = SOURCE.getBytes(UTF8);
		byte[] actual = Base64Utils.decode(ENCODED.getBytes());
		Assert.assertArrayEquals(expected, actual);
	}

	public void testDecodeString() throws UnsupportedEncodingException {
		byte[] expected = SOURCE.getBytes(UTF8);
		byte[] actual = Base64Utils.decode(ENCODED);
		Assert.assertArrayEquals(expected, actual);
	}

}
