/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util.comparator;

import java.util.Comparator;

import junit.framework.TestCase;

/**
 * Tests for {@link ComparableComparator}.
 *
 * @author Keith Donald
 * @author Chris Beams
 * @author Phillip Webb
 * @author Roy Clarkson
 */
public class ComparableComparatorTests extends TestCase {

	public void testComparableComparator() {
		Comparator<String> c = new ComparableComparator<String>();
		String s1 = "abc";
		String s2 = "cde";
		assertTrue(c.compare(s1, s2) < 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testShouldNeedComparable() {
		boolean success = false;
		try {
			Comparator c = new ComparableComparator();
			Object o1 = new Object();
			Object o2 = new Object();
			c.compare(o1, o2);
		}
		catch (ClassCastException e) {
			success = true;
		}
		assertTrue("Expected ClassCastException", success);
	}

}
