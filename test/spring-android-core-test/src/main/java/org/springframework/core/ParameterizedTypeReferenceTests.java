/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.android.test.Assert;

/**
 * Test fixture for {@link ParameterizedTypeReference}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Roy Clarkson
 */
public class ParameterizedTypeReferenceTests extends TestCase {

	public void testMap() throws NoSuchMethodException {
		Type mapType = getClass().getMethod("mapMethod").getGenericReturnType();
		ParameterizedTypeReference<Map<Object,String>> mapTypeReference = new ParameterizedTypeReference<Map<Object,String>>() {};

		//A simple type equality comparison isn't working on Android, so a deeper comparison is required 
		Type[] expected = ((ParameterizedType) mapType).getActualTypeArguments();
		Type[] actual = ((ParameterizedType) mapTypeReference.getType()).getActualTypeArguments();
		Assert.assertArrayEquals(expected, actual);
	}

	public void testList() throws NoSuchMethodException {
		Type listType = getClass().getMethod("listMethod").getGenericReturnType();
		ParameterizedTypeReference<List<String>> listTypeReference = new ParameterizedTypeReference<List<String>>() {};
		assertTrue(listType.getClass().isAssignableFrom(listTypeReference.getType().getClass()));
		assertTrue(listTypeReference.getType().getClass().isAssignableFrom(listType.getClass()));

		//A simple type equality comparison isn't working on Android, so a deeper comparison is required
		Type[] expected = ((ParameterizedType) listType).getActualTypeArguments();
		Type[] actual = ((ParameterizedType) listTypeReference.getType()).getActualTypeArguments();
		Assert.assertArrayEquals(expected, actual);
	}

	public void testString() {
		ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
		assertEquals(String.class, typeReference.getType());
	}

	public static Map<Object, String> mapMethod() {
		return null;
	}

	public static List<String> listMethod() {
		return null;
	}

}
