package org.springframework.android.test;

import junit.framework.TestCase;

public class Assert extends TestCase {
	
	private static final String MESSAGE = "arrays are not equal";
	
	public static void assertArrayEquals(Object[] expecteds, Object[] actuals)  {
		assertArrayEquals(MESSAGE, expecteds, actuals);
	}
	
	public static void assertArrayEquals(String message, Object[] expecteds, Object[] actuals)  {
		assertNotNull(expecteds);
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++ ) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}
	
	public static void assertArrayEquals(String[] expecteds, String[] actuals)  {
		assertArrayEquals(MESSAGE, expecteds, actuals);
	}
	
	public static void assertArrayEquals(String message, String[] expecteds, String[] actuals)  {
		assertNotNull(expecteds);
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++ ) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}
	
	public static void assertArrayEquals(byte[] expecteds, byte[] actuals)  {
		assertArrayEquals(MESSAGE, expecteds, actuals);
	}
	
	public static void assertArrayEquals(String message, byte[] expecteds, byte[] actuals)  {
		assertNotNull(expecteds);
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++ ) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}
}
