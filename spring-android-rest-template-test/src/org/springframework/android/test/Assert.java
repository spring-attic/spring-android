package org.springframework.android.test;

import junit.framework.TestCase;

public class Assert extends TestCase {
	
	public static void assertArrayEquals(java.lang.String message, Object[] expecteds, Object[] actuals)  {
		assertNotNull(expecteds);
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++ ) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}
	
	public static void assertArrayEquals(java.lang.String message, byte[] expecteds, byte[] actuals)  {
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
