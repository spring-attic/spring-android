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

package org.springframework.core.convert.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import android.graphics.Color;
import android.os.Build;

/**
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Phillip Webb
 */
public class GenericConversionServiceTests extends TestCase {

	private GenericConversionService conversionService = new GenericConversionService();

	public void testCanConvert() {
		assertFalse(conversionService.canConvert(String.class, Integer.class));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		assertTrue(conversionService.canConvert(String.class, Integer.class));
	}

	public void testCanConvertAssignable() {
		assertTrue(conversionService.canConvert(String.class, String.class));
		assertTrue(conversionService.canConvert(Integer.class, Number.class));
		assertTrue(conversionService.canConvert(boolean.class, boolean.class));
		assertTrue(conversionService.canConvert(boolean.class, Boolean.class));
	}

	public void testCanConvertIllegalArgumentNullTargetType() {
		try {
			assertFalse(conversionService.canConvert(String.class, null));
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
		}
		try {
			assertFalse(conversionService.canConvert(TypeDescriptor.valueOf(String.class), null));
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	public void testCanConvertNullSourceType() {
		assertTrue(conversionService.canConvert(null, Integer.class));
		assertTrue(conversionService.canConvert(null, TypeDescriptor.valueOf(Integer.class)));
	}

	public void testConvert() {
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		assertEquals(new Integer(3), conversionService.convert("3", Integer.class));
	}

	public void testConvertNullSource() {
		assertEquals(null, conversionService.convert(null, Integer.class));
	}

	public void testConvertNullSourcePrimitiveTarget() {
		try {
			assertEquals(null, conversionService.convert(null, int.class));
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}

	public void testConvertNullSourcePrimitiveTargetTypeDescriptor() {
		try {
			conversionService.convert(null, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(int.class));
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}

	public void testConvertNotNullSourceNullSourceTypeDescriptor() {
		try {
			conversionService.convert("3", null, TypeDescriptor.valueOf(int.class));
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testConvertAssignableSource() {
		assertEquals(Boolean.FALSE, conversionService.convert(false, boolean.class));
		assertEquals(Boolean.FALSE, conversionService.convert(false, Boolean.class));
	}

	public void testConverterNotFound() {
		try {
			conversionService.convert("3", Integer.class);
			fail("expected ConverterNotFoundException");
		}
		catch (ConverterNotFoundException e) {
		}
	}

	@SuppressWarnings("rawtypes")
	public void testAddConverterNoSourceTargetClassInfoAvailable() {
		try {
			conversionService.addConverter(new Converter() {
				@Override
				public Object convert(Object source) {
					return source;
				}
			});
			fail("Should have failed");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	public void testSourceTypeIsVoid() {
		GenericConversionService conversionService = new GenericConversionService();
		assertFalse(conversionService.canConvert(void.class, String.class));
	}

	public void testTargetTypeIsVoid() {
		GenericConversionService conversionService = new GenericConversionService();
		assertFalse(conversionService.canConvert(String.class, void.class));
	}

	public void testConvertNull() {
		assertNull(conversionService.convert(null, Integer.class));
	}

	public void testConvertNullTargetClass() {
		try {
			assertNull(conversionService.convert("3", (Class<?>) null));
			assertNull(conversionService.convert("3", TypeDescriptor.valueOf(String.class), null));
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testConvertNullTypeDescriptor() {
		try {
			assertNull(conversionService.convert("3", TypeDescriptor.valueOf(String.class), null));
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testConvertWrongSourceTypeDescriptor() {
		try {
			conversionService.convert("3", TypeDescriptor.valueOf(Integer.class), TypeDescriptor.valueOf(Long.class));
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testConvertWrongTypeArgument() {
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		try {
			conversionService.convert("BOGUS", Integer.class);
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}

	public void testConvertSuperSourceType() {
		conversionService.addConverter(new Converter<CharSequence, Integer>() {
			@Override
			public Integer convert(CharSequence source) {
				return Integer.valueOf(source.toString());
			}
		});
		Integer result = conversionService.convert("3", Integer.class);
		assertEquals(new Integer(3), result);
	}

	// SPR-8718

//	@Test(expected=ConverterNotFoundException.class)
//	public void convertSuperTarget() {
//		conversionService.addConverter(new ColorConverter());
//		conversionService.convert("#000000", SystemColor.class);
//	}
//
//	public class ColorConverter implements Converter<String, Color> {
//		@Override
//		public Color convert(String source) { if (!source.startsWith("#")) source = "#" + source; return Color.decode(source); }
//	}

	public void testConvertObjectToPrimitive() {
		assertFalse(conversionService.canConvert(String.class, boolean.class));
		conversionService.addConverter(new StringToBooleanConverter());
		assertTrue(conversionService.canConvert(String.class, boolean.class));
		Boolean b = conversionService.convert("true", boolean.class);
		assertEquals(Boolean.TRUE, b);
		assertTrue(conversionService.canConvert(TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(boolean.class)));
		b = (Boolean) conversionService.convert("true", TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(boolean.class));
		assertEquals(Boolean.TRUE, b);
	}

	public void testConvertObjectToPrimitiveViaConverterFactory() {
		assertFalse(conversionService.canConvert(String.class, int.class));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		assertTrue(conversionService.canConvert(String.class, int.class));
		Integer three = conversionService.convert("3", int.class);
		assertEquals(3, three.intValue());
	}

	public void testGenericConverterDelegatingBackToConversionServiceConverterNotFound() {
		conversionService.addConverter(new ObjectToArrayConverter(conversionService));
		assertFalse(conversionService.canConvert(String.class, Integer[].class));
		try {
			conversionService.convert("3,4,5", Integer[].class);
			fail("expected ConverterNotFoundException");
		}
		catch (ConverterNotFoundException ex) {
		}
	}

	public void testListToIterableConversion() {
		GenericConversionService conversionService = new GenericConversionService();
		List<Object> raw = new ArrayList<Object>();
		raw.add("one");
		raw.add("two");
		Object converted = conversionService.convert(raw, Iterable.class);
		assertSame(raw, converted);
	}

	public void testListToObjectConversion() {
		GenericConversionService conversionService = new GenericConversionService();
		List<Object> raw = new ArrayList<Object>();
		raw.add("one");
		raw.add("two");
		Object converted = conversionService.convert(raw, Object.class);
		assertSame(raw, converted);
	}

	public void testMapToObjectConversion() {
		GenericConversionService conversionService = new GenericConversionService();
		Map<Object, Object> raw = new HashMap<Object, Object>();
		raw.put("key", "value");
		Object converted = conversionService.convert(raw, Object.class);
		assertSame(raw, converted);
	}

	public void testInterfaceToString() {
		GenericConversionService conversionService = new GenericConversionService();
		conversionService.addConverter(new MyBaseInterfaceConverter());
		conversionService.addConverter(new ObjectToStringConverter());
		Object converted = conversionService.convert(new MyInterfaceImplementer(), String.class);
		assertEquals("RESULT", converted);
	}

	public void testInterfaceArrayToStringArray() {
		GenericConversionService conversionService = new GenericConversionService();
		conversionService.addConverter(new MyBaseInterfaceConverter());
		conversionService.addConverter(new ArrayToArrayConverter(conversionService));
		String[] converted = conversionService.convert(new MyInterface[] {new MyInterfaceImplementer()}, String[].class);
		assertEquals("RESULT", converted[0]);
	}

	public void testObjectArrayToStringArray() {
		GenericConversionService conversionService = new GenericConversionService();
		conversionService.addConverter(new MyBaseInterfaceConverter());
		conversionService.addConverter(new ArrayToArrayConverter(conversionService));
		String[] converted = conversionService.convert(new MyInterfaceImplementer[] {new MyInterfaceImplementer()}, String[].class);
		assertEquals("RESULT", converted[0]);
	}

	public void testStringArrayToResourceArray() {
		GenericConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new MyStringArrayToResourceArrayConverter());
		Resource[] converted = conversionService.convert(new String[] {"x1", "z3"}, Resource[].class);
		assertEquals(2, converted.length);
		assertEquals("1", converted[0].getDescription());
		assertEquals("3", converted[1].getDescription());
	}

	public void testStringArrayToIntegerArray() {
		GenericConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new MyStringArrayToIntegerArrayConverter());
		Integer[] converted = conversionService.convert(new String[] {"x1", "z3"}, Integer[].class);
		assertEquals(2, converted.length);
		assertEquals(1, converted[0].intValue());
		assertEquals(3, converted[1].intValue());
	}

	public void testStringToIntegerArray() {
		GenericConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new MyStringToIntegerArrayConverter());
		Integer[] converted = conversionService.convert("x1,z3", Integer[].class);
		assertEquals(2, converted.length);
		assertEquals(1, converted[0].intValue());
		assertEquals(3, converted[1].intValue());
	}

	public void testWildcardMap() throws Exception {
		GenericConversionService conversionService = new DefaultConversionService();
		Map<String, String> input = new LinkedHashMap<String, String>();
		input.put("key", "value");
		Object converted = conversionService.convert(input, TypeDescriptor.forObject(input), new TypeDescriptor(getClass().getField("wildcardMap")));
		assertEquals(input, converted);
	}

	public void testListOfList() {
		GenericConversionService service = new DefaultConversionService();
		List<String> list1 = Arrays.asList("Foo", "Bar");
		List<String> list2 = Arrays.asList("Baz", "Boop");
		List<List<String>> list = Arrays.asList(list1, list2);
		String result = service.convert(list, String.class);
		assertNotNull(result);
		assertEquals("Foo,Bar,Baz,Boop", result);
	}

	public void testStringToString() {
		GenericConversionService service = new DefaultConversionService();
		String value = "myValue";
		String result = service.convert(value, String.class);
		assertSame(value, result);
	}

	public void testStringToObject() {
		GenericConversionService service = new DefaultConversionService();
		String value = "myValue";
		Object result = service.convert(value, Object.class);
		assertSame(value, result);
	}

	public void testIgnoreCopyConstructor() {
		GenericConversionService service = new DefaultConversionService();
		WithCopyConstructor value = new WithCopyConstructor();
		Object result = service.convert(value, WithCopyConstructor.class);
		assertSame(value, result);
	}

	public void testConvertUUID() {
		GenericConversionService service = new DefaultConversionService();
		UUID uuid = UUID.randomUUID();
		String convertToString = service.convert(uuid, String.class);
		UUID convertToUUID = service.convert(convertToString, UUID.class);
		assertEquals(uuid, convertToUUID);
	}

//	public void testPerformance1() {
//		Assume.group(TestGroup.PERFORMANCE);
//		GenericConversionService conversionService = new DefaultConversionService();
//		StopWatch watch = new StopWatch("integer->string conversionPerformance");
//		watch.start("convert 4,000,000 with conversion service");
//		for (int i = 0; i < 4000000; i++) {
//			conversionService.convert(3, String.class);
//		}
//		watch.stop();
//		watch.start("convert 4,000,000 manually");
//		for (int i = 0; i < 4000000; i++) {
//			new Integer(3).toString();
//		}
//		watch.stop();
//		System.out.println(watch.prettyPrint());
//	}

//	public void testPerformance2() throws Exception {
//		Assume.group(TestGroup.PERFORMANCE);
//		GenericConversionService conversionService = new DefaultConversionService();
//		StopWatch watch = new StopWatch("list<string> -> list<integer> conversionPerformance");
//		watch.start("convert 4,000,000 with conversion service");
//		List<String> source = new LinkedList<String>();
//		source.add("1");
//		source.add("2");
//		source.add("3");
//		TypeDescriptor td = new TypeDescriptor(getClass().getField("list"));
//		for (int i = 0; i < 1000000; i++) {
//			conversionService.convert(source, TypeDescriptor.forObject(source), td);
//		}
//		watch.stop();
//		watch.start("convert 4,000,000 manually");
//		for (int i = 0; i < 4000000; i++) {
//			List<Integer> target = new ArrayList<Integer>(source.size());
//			for (String element : source) {
//				target.add(Integer.valueOf(element));
//			}
//		}
//		watch.stop();
//		System.out.println(watch.prettyPrint());
//	}

	public static List<Integer> list;

//	public void testPerformance3() throws Exception {
//		Assume.group(TestGroup.PERFORMANCE);
//		GenericConversionService conversionService = new DefaultConversionService();
//		StopWatch watch = new StopWatch("map<string, string> -> map<string, integer> conversionPerformance");
//		watch.start("convert 4,000,000 with conversion service");
//		Map<String, String> source = new HashMap<String, String>();
//		source.put("1", "1");
//		source.put("2", "2");
//		source.put("3", "3");
//		TypeDescriptor td = new TypeDescriptor(getClass().getField("map"));
//		for (int i = 0; i < 1000000; i++) {
//			conversionService.convert(source, TypeDescriptor.forObject(source), td);
//		}
//		watch.stop();
//		watch.start("convert 4,000,000 manually");
//		for (int i = 0; i < 4000000; i++) {
//			Map<String, Integer> target = new HashMap<String, Integer>(source.size());
//			for (Map.Entry<String, String> entry : source.entrySet()) {
//				target.put(entry.getKey(), Integer.valueOf(entry.getValue()));
//			}
//		}
//		watch.stop();
//		System.out.println(watch.prettyPrint());
//	}

	public static Map<String, Integer> map;

	public void testEmptyListToArray() {
		conversionService.addConverter(new CollectionToArrayConverter(conversionService));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		List<String> list = new ArrayList<String>();
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = TypeDescriptor.valueOf(String[].class);
		assertTrue(conversionService.canConvert(sourceType, targetType));
		assertEquals(0, ((String[])conversionService.convert(list, sourceType, targetType)).length);
	}

	public void testEmptyListToObject() {
		conversionService.addConverter(new CollectionToObjectConverter(conversionService));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		List<String> list = new ArrayList<String>();
		TypeDescriptor sourceType = TypeDescriptor.forObject(list);
		TypeDescriptor targetType = TypeDescriptor.valueOf(Integer.class);
		assertTrue(conversionService.canConvert(sourceType, targetType));
		assertNull(conversionService.convert(list, sourceType, targetType));
	}

	private interface MyBaseInterface {

	}


	private interface MyInterface extends MyBaseInterface {

	}


	private static class MyInterfaceImplementer implements MyInterface {

	}


	private static class MyBaseInterfaceConverter implements Converter<MyBaseInterface, String> {

		@Override
		public String convert(MyBaseInterface source) {
			return "RESULT";
		}
	}


	private static class MyStringArrayToResourceArrayConverter implements Converter<String[], Resource[]>	{

		@Override
		public Resource[] convert(String[] source) {
			Resource[] result = new Resource[source.length];
			for (int i = 0; i < source.length; i++) {
				result[i] = new DescriptiveResource(source[i].substring(1));
			}
			return result;
		}
	}


	private static class MyStringArrayToIntegerArrayConverter implements Converter<String[], Integer[]>	{

		@Override
		public Integer[] convert(String[] source) {
			Integer[] result = new Integer[source.length];
			for (int i = 0; i < source.length; i++) {
				result[i] = Integer.parseInt(source[i].substring(1));
			}
			return result;
		}
	}


	private static class MyStringToIntegerArrayConverter implements Converter<String, Integer[]>	{

		@Override
		public Integer[] convert(String source) {
			String[] srcArray = StringUtils.commaDelimitedListToStringArray(source);
			Integer[] result = new Integer[srcArray.length];
			for (int i = 0; i < srcArray.length; i++) {
				result[i] = Integer.parseInt(srcArray[i].substring(1));
			}
			return result;
		}
	}


	public static class WithCopyConstructor {

		public WithCopyConstructor() {
		}

		public WithCopyConstructor(WithCopyConstructor value) {
		}
	}


	public static Map<String, ?> wildcardMap;

	public void testStringToArrayCanConvert() {
		conversionService.addConverter(new StringToArrayConverter(conversionService));
		assertFalse(conversionService.canConvert(String.class, Integer[].class));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		assertTrue(conversionService.canConvert(String.class, Integer[].class));
	}

	public void testStringToCollectionCanConvert() throws Exception {
		conversionService.addConverter(new StringToCollectionConverter(conversionService));
		assertTrue(conversionService.canConvert(String.class, Collection.class));
		TypeDescriptor targetType = new TypeDescriptor(getClass().getField("stringToCollection"));
		assertFalse(conversionService.canConvert(TypeDescriptor.valueOf(String.class), targetType));
		conversionService.addConverterFactory(new StringToNumberConverterFactory());
		assertTrue(conversionService.canConvert(TypeDescriptor.valueOf(String.class), targetType));
	}

	public Collection<Integer> stringToCollection;

	public void testConvertiblePairsInSet() {
		Set<GenericConverter.ConvertiblePair> set = new HashSet<GenericConverter.ConvertiblePair>();
		set.add(new GenericConverter.ConvertiblePair(Number.class, String.class));
		assert set.contains(new GenericConverter.ConvertiblePair(Number.class, String.class));
	}

	public void testConvertiblePairEqualsAndHash() {
		GenericConverter.ConvertiblePair pair = new GenericConverter.ConvertiblePair(Number.class, String.class);
		GenericConverter.ConvertiblePair pairEqual = new GenericConverter.ConvertiblePair(Number.class, String.class);
		assertEquals(pair, pairEqual);
		assertEquals(pair.hashCode(), pairEqual.hashCode());
	}

	public void testConvertiblePairDifferentEqualsAndHash() {
		GenericConverter.ConvertiblePair pair = new GenericConverter.ConvertiblePair(Number.class, String.class);
		GenericConverter.ConvertiblePair pairOpposite = new GenericConverter.ConvertiblePair(String.class, Number.class);
		assertFalse(pair.equals(pairOpposite));
		assertFalse(pair.hashCode() == pairOpposite.hashCode());
	}

	public void testConvertPrimitiveArray() {
		GenericConversionService conversionService = new DefaultConversionService();
		byte[] byteArray = new byte[] { 1, 2, 3 };
		Byte[] converted = conversionService.convert(byteArray, Byte[].class);
		assertTrue(Arrays.equals(converted, new Byte[] {1, 2, 3}));
	}

	public void testCanConvertIllegalArgumentNullTargetTypeFromClass() {
		try {
			conversionService.canConvert(String.class, null);
			fail("Did not thow IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	public void testCanConvertIllegalArgumentNullTargetTypeFromTypeDescriptor() {
		try {
			conversionService.canConvert(TypeDescriptor.valueOf(String.class), null);
			fail("Did not thow IllegalArgumentException");
		}
		catch(IllegalArgumentException ex) {
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public void testConvertHashMapValuesToList() {
		GenericConversionService conversionService = new DefaultConversionService();
		Map<String, Integer> hashMap = new LinkedHashMap<String, Integer>();
		hashMap.put("1", 1);
		hashMap.put("2", 2);
		List converted = conversionService.convert(hashMap.values(), List.class);
		assertEquals(Arrays.asList(1, 2), converted);
	}

//	public void testRemoveConvertible() {
//		conversionService.addConverter(new ColorConverter());
//		assertTrue(conversionService.canConvert(String.class, Color.class));
//		conversionService.removeConvertible(String.class, Color.class);
//		assertFalse(conversionService.canConvert(String.class, Color.class));
//	}

//	public void testConditionalConverter() {
//		GenericConversionService conversionService = new GenericConversionService();
//		MyConditionalConverter converter = new MyConditionalConverter();
//		conversionService.addConverter(new ColorConverter());
//		conversionService.addConverter(converter);
//		assertEquals(Color.BLACK, conversionService.convert("#000000", Color.class));
//		assertTrue(converter.getMatchAttempts() > 0);
//	}

//	public void testConditionalConverterFactory() {
//		GenericConversionService conversionService = new GenericConversionService();
//		MyConditionalConverterFactory converter = new MyConditionalConverterFactory();
//		conversionService.addConverter(new ColorConverter());
//		conversionService.addConverterFactory(converter);
//		assertEquals(Color.BLACK, conversionService.convert("#000000", Color.class));
//		assertTrue(converter.getMatchAttempts() > 0);
//		assertTrue(converter.getNestedMatchAttempts() > 0);
//	}

	public void testShouldNotSupportNullConvertibleTypesFromNonConditionalGenericConverter() {
		GenericConversionService conversionService = new GenericConversionService();
		GenericConverter converter = new GenericConverter() {
			@Override
			public Set<ConvertiblePair> getConvertibleTypes() {
				return null;
			}
			@Override
			public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
				return null;
			}
		};
		try {
			conversionService.addConverter(converter);
			fail("Did not throw");
		}
		catch (IllegalStateException ex) {
			assertEquals("Only conditional converters may return null convertible types", ex.getMessage());
		}
	}

	public void testConditionalConversionForAllTypes() {
		GenericConversionService conversionService = new GenericConversionService();
		MyConditionalGenericConverter converter = new MyConditionalGenericConverter();
		conversionService.addConverter(converter);
		assertEquals((Integer) 3, conversionService.convert(3, Integer.class));
//		assertThat(converter.getSourceTypes().size(), greaterThan(2));
		Iterator<TypeDescriptor> iterator = converter.getSourceTypes().iterator();
		while(iterator.hasNext()) {
			assertEquals(Integer.class, iterator.next().getType());
		}
	}

	public void testConvertOptimizeArray() {
		// SPR-9566
		GenericConversionService conversionService = new DefaultConversionService();
		byte[] byteArray = new byte[] { 1, 2, 3 };
		byte[] converted = conversionService.convert(byteArray, byte[].class);
		assertSame(byteArray, converted);
	}

	public void testConvertCannotOptimizeArray() {
		GenericConversionService conversionService = new GenericConversionService();
		conversionService.addConverter(new Converter<Byte, Byte>() {
			@Override
			public Byte convert(Byte source) {
				return (byte) (source + 1);
			}
		});
		DefaultConversionService.addDefaultConverters(conversionService);
		byte[] byteArray = new byte[] { 1, 2, 3 };
		byte[] converted = conversionService.convert(byteArray, byte[].class);
		assertNotSame(byteArray, converted);
		assertTrue(Arrays.equals(new byte[] {2, 3, 4}, converted));
	}

	public void testEnumToStringConversion() {
		conversionService.addConverter(new EnumToStringConverter(conversionService));
		String result = conversionService.convert(MyEnum.A, String.class);
		assertEquals("A", result);
	}

	public void testEnumWithInterfaceToStringConversion() {
		// SPR-9692
		// Android 2.2 has some issues with reflection
		// see https://code.google.com/p/android/issues/detail?id=6636
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			conversionService.addConverter(new EnumToStringConverter(conversionService));
			conversionService.addConverter(new MyEnumInterfaceToStringConverter<MyEnum>());
			String result = conversionService.convert(MyEnum.A, String.class);
			assertEquals("1", result);
		}
	}

	public void testConvertNullAnnotatedStringToString() throws Exception {
		DefaultConversionService.addDefaultConverters(conversionService);
		String source = null;
		TypeDescriptor sourceType = new TypeDescriptor(getClass().getField("annotatedString"));
		TypeDescriptor targetType = TypeDescriptor.valueOf(String.class);
		conversionService.convert(source, sourceType, targetType);
	}

	public void testMultipleCollectionTypesFromSameSourceType() throws Exception {
		conversionService.addConverter(new MyStringToRawCollectionConverter());
		conversionService.addConverter(new MyStringToGenericCollectionConverter());
		conversionService.addConverter(new MyStringToStringCollectionConverter());
		conversionService.addConverter(new MyStringToIntegerCollectionConverter());

		assertEquals(Collections.singleton(4),  // should be "testX" from MyStringToStringCollectionConverter, ideally
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("stringCollection"))));
		assertEquals(Collections.singleton(4),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("integerCollection"))));
		assertEquals(Collections.singleton(4),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("rawCollection"))));
		assertEquals(Collections.singleton(4),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("genericCollection"))));
		assertEquals(Collections.singleton(4),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("rawCollection"))));
		assertEquals(Collections.singleton(4),  // should be "testX" from MyStringToStringCollectionConverter, ideally
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("stringCollection"))));
	}

	public void testAdaptedCollectionTypesFromSameSourceType() throws Exception {
		conversionService.addConverter(new MyStringToStringCollectionConverter());

		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("stringCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("genericCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("rawCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("genericCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("stringCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("rawCollection"))));

		// The following is unpleasant but simply a consequence of the raw type matching algorithm in Spring 3.x
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("integerCollection"))));
	}

	public void testGenericCollectionAsSource() throws Exception {
		conversionService.addConverter(new MyStringToGenericCollectionConverter());

		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("stringCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("genericCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("rawCollection"))));

		// The following is unpleasant but a consequence of the generic collection converter above...
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("integerCollection"))));
	}

	public void testRawCollectionAsSource() throws Exception {
		conversionService.addConverter(new MyStringToRawCollectionConverter());

		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("stringCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("genericCollection"))));
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("rawCollection"))));

		// The following is unpleasant but a consequence of the raw collection converter above...
		assertEquals(Collections.singleton("testX"),
				conversionService.convert("test", TypeDescriptor.valueOf(String.class), new TypeDescriptor(getClass().getField("integerCollection"))));
	}


	@ExampleAnnotation
	public String annotatedString;

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ExampleAnnotation {
	}


	private static class MyConditionalConverter implements Converter<String, Color>, ConditionalConverter {

		private int matchAttempts = 0;

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			matchAttempts++;
			return false;
		}

		@Override
		public Color convert(String source) {
			throw new IllegalStateException();
		}

		public int getMatchAttempts() {
			return matchAttempts;
		}
	}


	private static class MyConditionalGenericConverter implements GenericConverter, ConditionalConverter {

		private List<TypeDescriptor> sourceTypes = new ArrayList<TypeDescriptor>();

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			sourceTypes.add(sourceType);
			return false;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType,
				TypeDescriptor targetType) {
			return null;
		}

		public List<TypeDescriptor> getSourceTypes() {
			return sourceTypes;
		}
	}


	private static class MyConditionalConverterFactory implements ConverterFactory<String, Color>, ConditionalConverter {

		private MyConditionalConverter converter = new MyConditionalConverter();

		private int matchAttempts = 0;

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			matchAttempts++;
			return true;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Color> Converter<String, T> getConverter(Class<T> targetType) {
			return (Converter<String, T>) converter;
		}

		public int getMatchAttempts() {
			return matchAttempts;
		}

		public int getNestedMatchAttempts() {
			return converter.getMatchAttempts();
		}
	}


	interface MyEnumInterface {

		String getCode();
	}

	public static enum MyEnum implements MyEnumInterface {

		A {
			@Override
			public String getCode() {
				return "1";
			}
		}
	}


	public static class MyStringToRawCollectionConverter implements Converter<String, Collection> {

		@Override
		public Collection convert(String source) {
			return Collections.singleton(source + "X");
		}
	}

	public static class MyStringToGenericCollectionConverter implements Converter<String, Collection<?>> {

		@Override
		public Collection<?> convert(String source) {
			return Collections.singleton(source + "X");
		}
	}

	private static class MyEnumInterfaceToStringConverter<T extends MyEnumInterface> implements Converter<T, String> {

		@Override
		public String convert(T source) {
			return source.getCode();
		}
	}

	public static class MyStringToStringCollectionConverter implements Converter<String, Collection<String>> {

		@Override
		public Collection<String> convert(String source) {
			return Collections.singleton(source + "X");
		}
	}

	public static class MyStringToIntegerCollectionConverter implements Converter<String, Collection<Integer>> {

		@Override
		public Collection<Integer> convert(String source) {
			return Collections.singleton(source.length());
		}
	}


	public Collection rawCollection;

	public Collection<?> genericCollection;

	public Collection<String> stringCollection;

	public Collection<Integer> integerCollection;

}
