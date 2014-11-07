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

package org.springframework.util.comparator;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Comparator;

import junit.framework.TestCase;

/**
 * Tests for {@link InvertibleComparator}.
 *
 * @author Keith Donald
 * @author Chris Beams
 * @author Phillip Webb
 * @author Roy Clarkson
 */

public class InvertibleComparatorTests extends TestCase {

	private Comparator<Integer> comparator = new ComparableComparator<Integer>();

	public void testShouldNeedComparator() throws Exception {
		boolean success = false;
		try {
			new InvertibleComparator<Object>(null);
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testShouldNeedComparatorWithAscending() throws Exception {
		boolean success = false;
		try {
			new InvertibleComparator<Object>(null, true);
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testShouldDefaultToAscending() throws Exception {
		InvertibleComparator<Integer> invertibleComparator =
				new InvertibleComparator<Integer>(comparator);
		assertThat(invertibleComparator.isAscending(), is(true));
		assertThat(invertibleComparator.compare(1, 2), is(-1));
	}

	public void testShouldInvert() throws Exception {
		InvertibleComparator<Integer> invertibleComparator =
				new InvertibleComparator<Integer>(comparator);
		assertThat(invertibleComparator.isAscending(), is(true));
		assertThat(invertibleComparator.compare(1, 2), is(-1));
		invertibleComparator.invertOrder();
		assertThat(invertibleComparator.isAscending(), is(false));
		assertThat(invertibleComparator.compare(1, 2), is(1));
	}

	public void testShouldCompareAscending() throws Exception {
		InvertibleComparator<Integer> invertibleComparator =
				new InvertibleComparator<Integer>(comparator, true);
		assertThat(invertibleComparator.compare(1, 2), is(-1));
	}

	public void testShouldCompareDescending() throws Exception {
		InvertibleComparator<Integer> invertibleComparator =
				new InvertibleComparator<Integer>(comparator, false);
		assertThat(invertibleComparator.compare(1, 2), is(1));
	}

}
