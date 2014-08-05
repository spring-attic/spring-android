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

package org.springframework.android.test;

import junit.framework.TestCase;

/**
 * @author Roy Clarkson
 */
public class Assert extends TestCase {

	private static final String MESSAGE = "arrays are not equal";

	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		assertArrayEquals(MESSAGE, expected, actual);
	}

	public static void assertArrayEquals(String message, Object[] expected,
			Object[] actual) {
		assertNotNull(expected);
		assertNotNull(actual);
		assertEquals(expected.length, actual.length);
		if (expected.length == actual.length) {
			for (int i = 0; i < expected.length; i++) {
				assertEquals(expected[i], actual[i]);
			}
		}
	}

	public static void assertArrayEquals(String[] expected, String[] actual) {
		assertArrayEquals(MESSAGE, expected, actual);
	}

	public static void assertArrayEquals(String message, String[] expecteds,
			String[] actuals) {
		assertNotNull(expecteds);
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}

	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		assertArrayEquals(MESSAGE, expected, actual);
	}

	public static void assertArrayEquals(String message, byte[] expecteds, byte[] actuals) {
		assertNotNull(expecteds);
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}
}
