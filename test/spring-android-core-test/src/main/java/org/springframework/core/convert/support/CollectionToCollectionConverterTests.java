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

package org.springframework.core.convert.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 */
public class CollectionToCollectionConverterTests extends TestCase {

	private GenericConversionService conversionService = new GenericConversionService();


	@Override
	public void setUp() {
		conversionService.addConverter(new CollectionToCollectionConverter(conversionService));
	}


	public void testScalarList() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("9");
		list.add("37");
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = new TypeDescriptor(getClass().getField("scalarListTarget"));
		assertTrue(conversionService.canConvert(sourceType, targetType));
		try {
			conversionService.convert(list, sourceType, targetType);
		}
		catch (ConversionFailedException ex) {
			assertTrue(ex.getCause() instanceof ConverterNotFoundException);
		}
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		assertTrue(conversionService.canConvert(sourceType, targetType));
		@SuppressWarnings("unchecked")
		List<String> result = (List<String>) conversionService.convert(list, sourceType, targetType);
		assertFalse(list.equals(result));
		assertEquals(9, result.get(0));
		assertEquals(37, result.get(1));
	}

	public void testEmptyListToList() throws Exception {
		conversionService.addConverter(new CollectionToCollectionConverter(conversionService));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		List<String> list = new ArrayList<String>();
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = new TypeDescriptor(getClass().getField("emptyListTarget"));
		assertTrue(conversionService.canConvert(sourceType, targetType));
		assertEquals(list, conversionService.convert(list, sourceType, targetType));
	}

	public void testEmptyListToListDifferentTargetType() throws Exception {
		conversionService.addConverter(new CollectionToCollectionConverter(conversionService));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		List<String> list = new ArrayList<String>();
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = new TypeDescriptor(getClass().getField("emptyListDifferentTarget"));
		assertTrue(conversionService.canConvert(sourceType, targetType));
		@SuppressWarnings("unchecked")
		LinkedList<Integer> result = (LinkedList<Integer>) conversionService.convert(list, sourceType, targetType);
		assertEquals(LinkedList.class, result.getClass());
		assertTrue(result.isEmpty());
	}

	public void testCollectionToObjectInteraction() throws Exception {
		List<List<String>> list = new ArrayList<List<String>>();
		list.add(Arrays.asList("9", "12"));
		list.add(Arrays.asList("37", "23"));
		conversionService.addConverter(new CollectionToObjectConverter(conversionService));
		assertTrue(conversionService.canConvert(List.class, List.class));
		assertSame(list, conversionService.convert(list, List.class));
	}

	@SuppressWarnings("unchecked")
	public void testArrayCollectionToObjectInteraction() throws Exception {
		List<String>[] array = new List[2];
		array[0] = Arrays.asList("9", "12");
		array[1] = Arrays.asList("37", "23");
		conversionService.addConverter(new ArrayToCollectionConverter(conversionService));
		conversionService.addConverter(new CollectionToObjectConverter(conversionService));
		assertTrue(conversionService.canConvert(String[].class, List.class));
		assertEquals(Arrays.asList(array), conversionService.convert(array, List.class));
	}

	@SuppressWarnings("unchecked")
	public void testObjectToCollection() throws Exception {
		List<List<String>> list = new ArrayList<List<String>>();
		list.add(Arrays.asList("9", "12"));
		list.add(Arrays.asList("37", "23"));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		conversionService.addConverter(new ObjectToCollectionConverter(conversionService));
		conversionService.addConverter(new CollectionToObjectConverter(conversionService));
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = new TypeDescriptor(getClass().getField("objectToCollection"));
		assertTrue(conversionService.canConvert(sourceType, targetType));
		List<List<List<Integer>>> result = (List<List<List<Integer>>>) conversionService.convert(list, sourceType, targetType);
		assertEquals((Integer) 9, result.get(0).get(0).get(0));
		assertEquals((Integer) 12, result.get(0).get(1).get(0));
		assertEquals((Integer) 37, result.get(1).get(0).get(0));
		assertEquals((Integer) 23, result.get(1).get(1).get(0));
	}

	@SuppressWarnings("unchecked")
	public void testStringToCollection() throws Exception {
		List<List<String>> list = new ArrayList<List<String>>();
		list.add(Arrays.asList("9,12"));
		list.add(Arrays.asList("37,23"));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		conversionService.addConverter(new StringToCollectionConverter(conversionService));
		conversionService.addConverter(new ObjectToCollectionConverter(conversionService));
		conversionService.addConverter(new CollectionToObjectConverter(conversionService));
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = new TypeDescriptor(getClass().getField("objectToCollection"));
		assertTrue(conversionService.canConvert(sourceType, targetType));
		List<List<List<Integer>>> result = (List<List<List<Integer>>>) conversionService.convert(list, sourceType, targetType);
		assertEquals((Integer) 9, result.get(0).get(0).get(0));
		assertEquals((Integer) 12, result.get(0).get(0).get(1));
		assertEquals((Integer) 37, result.get(1).get(0).get(0));
		assertEquals((Integer) 23, result.get(1).get(0).get(1));
	}

	public void testConvertEmptyVector_shouldReturnEmptyArrayList() {
		Vector<String> vector = new Vector<String>();
		vector.add("Element");
		testCollectionConversionToArrayList(vector);
	}

	public void testConvertNonEmptyVector_shouldReturnNonEmptyArrayList() {
		Vector<String> vector = new Vector<String>();
		vector.add("Element");
		testCollectionConversionToArrayList(vector);
	}

	public void testCollectionsEmptyList() throws Exception {
		CollectionToCollectionConverter converter = new CollectionToCollectionConverter(new GenericConversionService());
		TypeDescriptor type = new TypeDescriptor(getClass().getField("list"));
		converter.convert(list, type, TypeDescriptor.valueOf(Class.forName("java.util.Collections$EmptyList")));
	}

	@SuppressWarnings("rawtypes")
	private void testCollectionConversionToArrayList(Collection<String> aSource) {
		Object myConverted = (new CollectionToCollectionConverter(new GenericConversionService())).convert(
				aSource, TypeDescriptor.forObject(aSource), TypeDescriptor.forObject(new ArrayList()));
		assertTrue(myConverted instanceof ArrayList<?>);
		assertEquals(aSource.size(), ((ArrayList<?>) myConverted).size());
	}

	public void testListToCollectionNoCopyRequired() throws NoSuchFieldException {
		List<?> input = new ArrayList<String>(Arrays.asList("foo", "bar"));
		assertSame(input, conversionService.convert(input, TypeDescriptor.forObject(input),
				new TypeDescriptor(getClass().getField("wildCardCollection"))));
	}

	public void testDifferentImpls() throws Exception {
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(new ClassPathResource("test"));
		resources.add(new FileSystemResource("test"));
		resources.add(new TestResource());
		TypeDescriptor sourceType = TypeDescriptor.forObject(resources);
		assertSame(resources, conversionService.convert(resources, sourceType, new TypeDescriptor(getClass().getField("resources"))));
	}

	public void testMixedInNulls() throws Exception {
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(new ClassPathResource("test"));
		resources.add(null);
		resources.add(new FileSystemResource("test"));
		resources.add(new TestResource());
		TypeDescriptor sourceType = TypeDescriptor.forObject(resources);
		assertSame(resources, conversionService.convert(resources, sourceType, new TypeDescriptor(getClass().getField("resources"))));
	}

	public void testAllNulls() throws Exception {
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(null);
		resources.add(null);
		TypeDescriptor sourceType = TypeDescriptor.forObject(resources);
		assertSame(resources, conversionService.convert(resources, sourceType, new TypeDescriptor(getClass().getField("resources"))));
	}

	public void testElementTypesNotConvertible() throws Exception {
		List<String> resources = new ArrayList<String>();
		resources.add(null);
		resources.add(null);
		TypeDescriptor sourceType = new TypeDescriptor(getClass().getField("strings"));
		try {
			assertEquals(resources, conversionService.convert(resources, sourceType, new TypeDescriptor(getClass().getField("resources"))));
			fail("expected ConverterNotFoundException");
		}
		catch (ConverterNotFoundException e) {
		}
	}

	public void testNothingInCommon() throws Exception {
		List<Object> resources = new ArrayList<Object>();
		resources.add(new ClassPathResource("test"));
		resources.add(3);
		TypeDescriptor sourceType = TypeDescriptor.forObject(resources);
		try {
			assertEquals(resources, conversionService.convert(resources, sourceType, new TypeDescriptor(getClass().getField("resources"))));
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}


	public ArrayList<Integer> scalarListTarget;

	public List<Integer> emptyListTarget;

	public LinkedList<Integer> emptyListDifferentTarget;

	public List<List<List<Integer>>> objectToCollection;

	public List<String> strings;

	public List list = Collections.emptyList();

	public Collection<?> wildCardCollection = Collections.emptyList();

	public List<Resource> resources;


	public static abstract class BaseResource implements Resource {

		@Override
		public InputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public boolean isReadable() {
			return false;
		}

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public URL getURL() throws IOException {
			return null;
		}

		@Override
		public URI getURI() throws IOException {
			return null;
		}

		@Override
		public File getFile() throws IOException {
			return null;
		}

		@Override
		public long contentLength() throws IOException {
			return 0;
		}

		@Override
		public long lastModified() throws IOException {
			return 0;
		}

		@Override
		public Resource createRelative(String relativePath) throws IOException {
			return null;
		}

		@Override
		public String getFilename() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}
	}


	public static class TestResource extends BaseResource {
	}

}
