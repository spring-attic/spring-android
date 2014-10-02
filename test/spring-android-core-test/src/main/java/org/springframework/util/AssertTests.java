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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link Assert} class.
 *
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Rick Evans
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class AssertTests extends TestCase {

	public void testInstanceOf() {
		boolean success = false;
		try {
			final Set<?> set = new HashSet<Object>();
			Assert.isInstanceOf(HashSet.class, set);
			Assert.isInstanceOf(HashMap.class, set);
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testInstanceOfNoMessage() throws Exception {
		boolean success = false;
		try {
			Assert.isInstanceOf(Set.class, new Object(), null);
		}
		catch (IllegalArgumentException e) {
			assertEquals("Object of class [java.lang.Object] must be an instance "
					+ "of interface java.util.Set", e.getMessage());
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testInstanceOfMessage() throws Exception {
		boolean success = false;
		try {
			Assert.isInstanceOf(Set.class, new Object(), "Custom message.");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Custom message. Object of class [java.lang.Object] must "
					+ "be an instance of interface java.util.Set", e.getMessage());
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testIsNullDoesNotThrowExceptionIfArgumentIsNullWithMessage() {
		Assert.isNull(null, "Bla");
	}

	public void testIsNullDoesNotThrowExceptionIfArgumentIsNull() {
		Assert.isNull(null);
	}

	public void testIsNullThrowsExceptionIfArgumentIsNotNull() {
		boolean success = false;
		try {
			Assert.isNull(new Object());
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testIsTrueWithFalseExpressionThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.isTrue(false);
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testIsTrueWithTrueExpressionSunnyDay() throws Exception {
		Assert.isTrue(true);
	}

	public void testHasLengthWithNullStringThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.hasLength(null);
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testHasLengthWithEmptyStringThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.hasLength("");
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testHasLengthWithWhitespaceOnlyStringDoesNotThrowException() throws Exception {
		Assert.hasLength("\t  ");
	}

	public void testHasLengthSunnyDay() throws Exception {
		Assert.hasLength("I Heart ...");
	}

	public void testDoesNotContainWithNullSearchStringDoesNotThrowException() throws Exception {
		Assert.doesNotContain(null, "rod");
	}

	public void testDoesNotContainWithNullSubstringDoesNotThrowException() throws Exception {
		Assert.doesNotContain("A cool chick's name is Brod. ", null);
	}

	public void testDoesNotContainWithEmptySubstringDoesNotThrowException() throws Exception {
		Assert.doesNotContain("A cool chick's name is Brod. ", "");
	}

	public void testAssertNotEmptyWithNullCollectionThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.notEmpty((Collection<?>) null);
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testAssertNotEmptyWithEmptyCollectionThrowsException() throws Exception {

		boolean success = false;
		try {
			Assert.notEmpty(new ArrayList<Object>());
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testAssertNotEmptyWithCollectionSunnyDay() throws Exception {
		List<String> collection = new ArrayList<String>();
		collection.add("");
		Assert.notEmpty(collection);
	}

	public void testAssertNotEmptyWithNullMapThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.notEmpty((Map<?, ?>) null);
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testAssertNotEmptyWithEmptyMapThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.notEmpty(new HashMap<Object, Object>());
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testAssertNotEmptyWithMapSunnyDay() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("", "");
		Assert.notEmpty(map);
	}

	public void testIsInstanceofClassWithNullInstanceThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.isInstanceOf(String.class, null);
		}
		catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	public void testStateWithFalseExpressionThrowsException() throws Exception {
		boolean success = false;
		try {
			Assert.state(false);
		}
		catch (IllegalStateException e) {
			success = true;
		}
		assertTrue("Expected IllegalStateException", success);
	}

	public void testStateWithTrueExpressionSunnyDay() throws Exception {
		Assert.state(true);
	}

}
