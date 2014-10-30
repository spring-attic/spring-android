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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.android.test.Assert;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;

import android.graphics.Color;

/**
 * @author Keith Donald
 * @author Juergen Hoeller
 */
public class DefaultConversionTests extends TestCase {

	private final DefaultConversionService conversionService = new DefaultConversionService();


	public void testStringToCharacter() {
		assertEquals(Character.valueOf('1'), conversionService.convert("1", Character.class));
	}

	public void testStringToCharacterEmptyString() {
		assertEquals(null, conversionService.convert("", Character.class));
	}

	public void testStringToCharacterInvalidString() {
		try {
			conversionService.convert("invalid", Character.class);
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}

	public void testCharacterToString() {
		assertEquals("3", conversionService.convert('3', String.class));
	}

	public void testStringToBooleanTrue() {
		assertEquals(Boolean.valueOf(true), conversionService.convert("true", Boolean.class));
		assertEquals(Boolean.valueOf(true), conversionService.convert("on", Boolean.class));
		assertEquals(Boolean.valueOf(true), conversionService.convert("yes", Boolean.class));
		assertEquals(Boolean.valueOf(true), conversionService.convert("1", Boolean.class));
		assertEquals(Boolean.valueOf(true), conversionService.convert("TRUE", Boolean.class));
		assertEquals(Boolean.valueOf(true), conversionService.convert("ON", Boolean.class));
		assertEquals(Boolean.valueOf(true), conversionService.convert("YES", Boolean.class));
	}

	public void testStringToBooleanFalse() {
		assertEquals(Boolean.valueOf(false), conversionService.convert("false", Boolean.class));
		assertEquals(Boolean.valueOf(false), conversionService.convert("off", Boolean.class));
		assertEquals(Boolean.valueOf(false), conversionService.convert("no", Boolean.class));
		assertEquals(Boolean.valueOf(false), conversionService.convert("0", Boolean.class));
		assertEquals(Boolean.valueOf(false), conversionService.convert("FALSE", Boolean.class));
		assertEquals(Boolean.valueOf(false), conversionService.convert("OFF", Boolean.class));
		assertEquals(Boolean.valueOf(false), conversionService.convert("NO", Boolean.class));
	}

	public void testStringToBooleanEmptyString() {
		assertEquals(null, conversionService.convert("", Boolean.class));
	}

	public void testStringToBooleanInvalidString() {
		try {
			conversionService.convert("invalid", Boolean.class);
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}

	public void testBooleanToString() {
		assertEquals("true", conversionService.convert(true, String.class));
	}

	public void testStringToByte() throws Exception {
		assertEquals(Byte.valueOf("1"), conversionService.convert("1", Byte.class));
	}

	public void testByteToString() {
		assertEquals("65", conversionService.convert(new String("A").getBytes()[0], String.class));
	}

	public void testStringToShort() {
		assertEquals(Short.valueOf("1"), conversionService.convert("1", Short.class));
	}

	public void testShortToString() {
		short three = 3;
		assertEquals("3", conversionService.convert(three, String.class));
	}

	public void testStringToInteger() {
		assertEquals(Integer.valueOf("1"), conversionService.convert("1", Integer.class));
	}

	public void testIntegerToString() {
		assertEquals("3", conversionService.convert(3, String.class));
	}

	public void testStringToLong() {
		assertEquals(Long.valueOf("1"), conversionService.convert("1", Long.class));
	}

	public void testLongToString() {
		assertEquals("3", conversionService.convert(3L, String.class));
	}

	public void testStringToFloat() {
		assertEquals(Float.valueOf("1.0"), conversionService.convert("1.0", Float.class));
	}

	public void testFloatToString() {
		assertEquals("1.0", conversionService.convert(new Float("1.0"), String.class));
	}

	public void testStringToDouble() {
		assertEquals(Double.valueOf("1.0"), conversionService.convert("1.0", Double.class));
	}

	public void testDoubleToString() {
		assertEquals("1.0", conversionService.convert(new Double("1.0"), String.class));
	}

	public void testStringToBigInteger() {
		assertEquals(new BigInteger("1"), conversionService.convert("1", BigInteger.class));
	}

	public void testBigIntegerToString() {
		assertEquals("100", conversionService.convert(new BigInteger("100"), String.class));
	}

	public void testStringToBigDecimal() {
		assertEquals(new BigDecimal("1.0"), conversionService.convert("1.0", BigDecimal.class));
	}

	public void testBigDecimalToString() {
		assertEquals("100.00", conversionService.convert(new BigDecimal("100.00"), String.class));
	}

	public void testStringToNumber() {
		assertEquals(new BigDecimal("1.0"), conversionService.convert("1.0", Number.class));
	}

	public void testStringToNumberEmptyString() {
		assertEquals(null, conversionService.convert("", Number.class));
	}

	public void testStringToEnum() throws Exception {
		assertEquals(Foo.BAR, conversionService.convert("BAR", Foo.class));
	}

	public void testStringToEnumWithSubclass() throws Exception {
		assertEquals(SubFoo.BAZ, conversionService.convert("BAZ", SubFoo.BAR.getClass()));
	}

	public void testStringToEnumEmptyString() {
		assertEquals(null, conversionService.convert("", Foo.class));
	}

	public void testEnumToString() {
		assertEquals("BAR", conversionService.convert(Foo.BAR, String.class));
	}

	public static enum Foo {
		BAR, BAZ
	}

	public static enum SubFoo {

		BAR {
			@Override
			String s() {
				return "x";
			}
		},
		BAZ {
			@Override
			String s() {
				return "y";
			}
		};

		abstract String s();
	}

	public void testStringToLocale() {
		assertEquals(Locale.ENGLISH, conversionService.convert("en", Locale.class));
	}

	public void testStringToString() {
		String str = "test";
		assertSame(str, conversionService.convert(str, String.class));
	}

	public void testNumberToNumber() {
		assertEquals(Long.valueOf(1), conversionService.convert(Integer.valueOf(1), Long.class));
	}

	public void testNumberToNumberNotSupportedNumber() {
		try {
			conversionService.convert(Integer.valueOf(1), CustomNumber.class);
			fail("expected ConversionFailedException");
		}
		catch(ConversionFailedException e) {
		}
	}

	@SuppressWarnings("serial")
	public static class CustomNumber extends Number {

		@Override
		public double doubleValue() {
			return 0;
		}

		@Override
		public float floatValue() {
			return 0;
		}

		@Override
		public int intValue() {
			return 0;
		}

		@Override
		public long longValue() {
			return 0;
		}

	}

	public void testNumberToCharacter() {
		assertEquals(Character.valueOf('A'), conversionService.convert(Integer.valueOf(65), Character.class));
	}

	public void testCharacterToNumber() {
		assertEquals(new Integer(65), conversionService.convert('A', Integer.class));
	}

	// collection conversion

	public void testConvertArrayToCollectionInterface() {
		List<?> result = conversionService.convert(new String[] { "1", "2", "3" }, List.class);
		assertEquals("1", result.get(0));
		assertEquals("2", result.get(1));
		assertEquals("3", result.get(2));
	}

	public List<Integer> genericList = new ArrayList<Integer>();

	public void testConvertArrayToCollectionGenericTypeConversion() throws Exception {
		List<Integer> result = (List<Integer>) conversionService.convert(new String[] { "1", "2", "3" }, TypeDescriptor
				.valueOf(String[].class), new TypeDescriptor(getClass().getDeclaredField("genericList")));
		assertEquals(new Integer("1"), result.get(0));
		assertEquals(new Integer("2"), result.get(1));
		assertEquals(new Integer("3"), result.get(2));
	}

//	public void testSpr7766() throws Exception {
//		ConverterRegistry registry = (conversionService);
//		registry.addConverter(new ColorConverter());
//		List<Color> colors = (List<Color>) conversionService.convert(new String[] { "ffffff", "#000000" }, TypeDescriptor.valueOf(String[].class), new TypeDescriptor(new MethodParameter(getClass().getMethod("handlerMethod", List.class), 0)));
//		assertEquals(2, colors.size());
//		assertEquals(Color.WHITE, colors.get(0));
//		assertEquals(Color.BLACK, colors.get(1));
//	}

//	public class ColorConverter implements Converter<String, Color> {
//		@Override
//		public Color convert(String source) { if (!source.startsWith("#")) source = "#" + source; return Color.parseColor(source); }
//	}

	public void handlerMethod(List<Color> color) {

	}

	public void testConvertArrayToCollectionImpl() {
		LinkedList<?> result = conversionService.convert(new String[] { "1", "2", "3" }, LinkedList.class);
		assertEquals("1", result.get(0));
		assertEquals("2", result.get(1));
		assertEquals("3", result.get(2));
	}

	public void testConvertArrayToAbstractCollection() {
		try {
			conversionService.convert(new String[] { "1", "2", "3" }, AbstractList.class);
			fail("expected ConversionFailedException");
		}
		catch (ConversionFailedException e) {
		}
	}

	public static enum FooEnum {
		BAR, BAZ
	}

	public void testConvertArrayToString() {
		String result = conversionService.convert(new String[] { "1", "2", "3" }, String.class);
		assertEquals("1,2,3", result);
	}

	public void testConvertArrayToStringWithElementConversion() {
		String result = conversionService.convert(new Integer[] { 1, 2, 3 }, String.class);
		assertEquals("1,2,3", result);
	}

	public void testConvertEmptyArrayToString() {
		String result = conversionService.convert(new String[0], String.class);
		assertEquals("", result);
	}

	public void testConvertStringToArray() {
		String[] result = conversionService.convert("1,2,3", String[].class);
		assertEquals(3, result.length);
		assertEquals("1", result[0]);
		assertEquals("2", result[1]);
		assertEquals("3", result[2]);
	}

	public void testConvertStringToArrayWithElementConversion() {
		Integer[] result = conversionService.convert("1,2,3", Integer[].class);
		assertEquals(3, result.length);
		assertEquals(new Integer(1), result[0]);
		assertEquals(new Integer(2), result[1]);
		assertEquals(new Integer(3), result[2]);
	}

	public void testConvertStringToPrimitiveArrayWithElementConversion() {
		int[] result = conversionService.convert("1,2,3", int[].class);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(2, result[1]);
		assertEquals(3, result[2]);
	}

	public void testConvertEmptyStringToArray() {
		String[] result = conversionService.convert("", String[].class);
		assertEquals(0, result.length);
	}

	public void testConvertArrayToObject() {
		Object[] array = new Object[] { 3L };
		Object result = conversionService.convert(array, Long.class);
		assertEquals(3L, result);
	}

	public void testConvertArrayToObjectWithElementConversion() {
		String[] array = new String[] { "3" };
		Integer result = conversionService.convert(array, Integer.class);
		assertEquals(new Integer(3), result);
	}

	public void testConvertArrayToObjectAssignableTargetType() {
		Long[] array = new Long[] { 3L };
		Long[] result = (Long[]) conversionService.convert(array, Object.class);
		Assert.assertArrayEquals(array, result);
	}

	public void testConvertObjectToArray() {
		Object[] result = conversionService.convert(3L, Object[].class);
		assertEquals(1, result.length);
		assertEquals(3L, result[0]);
	}

	public void testConvertObjectToArrayWithElementConversion() {
		Integer[] result = conversionService.convert(3L, Integer[].class);
		assertEquals(1, result.length);
		assertEquals(new Integer(3), result[0]);
	}

	public void testConvertCollectionToArray() {
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		String[] result = conversionService.convert(list, String[].class);
		assertEquals("1", result[0]);
		assertEquals("2", result[1]);
		assertEquals("3", result[2]);
	}

	public void testConvertCollectionToArrayWithElementConversion() {
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		Integer[] result = conversionService.convert(list, Integer[].class);
		assertEquals(new Integer(1), result[0]);
		assertEquals(new Integer(2), result[1]);
		assertEquals(new Integer(3), result[2]);
	}

	public void testConvertCollectionToString() {
		List<String> list = Arrays.asList(new String[] { "foo", "bar" });
		String result = conversionService.convert(list, String.class);
		assertEquals("foo,bar", result);
	}

	public void testConvertCollectionToStringWithElementConversion() throws Exception {
		List<Integer> list = Arrays.asList(new Integer[] { 3, 5 });
		String result = (String) conversionService.convert(list,
				new TypeDescriptor(getClass().getField("genericList")), TypeDescriptor.valueOf(String.class));
		assertEquals("3,5", result);
	}

	public void testConvertStringToCollection() {
		List result = conversionService.convert("1,2,3", List.class);
		assertEquals(3, result.size());
		assertEquals("1", result.get(0));
		assertEquals("2", result.get(1));
		assertEquals("3", result.get(2));
	}

	public void testConvertStringToCollectionWithElementConversion() throws Exception {
		List result = (List) conversionService.convert("1,2,3", TypeDescriptor.valueOf(String.class),
				new TypeDescriptor(getClass().getField("genericList")));
		assertEquals(3, result.size());
		assertEquals(new Integer(1), result.get(0));
		assertEquals(new Integer(2), result.get(1));
		assertEquals(new Integer(3), result.get(2));
	}

	public void testConvertEmptyStringToCollection() {
		Collection result = conversionService.convert("", Collection.class);
		assertEquals(0, result.size());
	}

	public void testConvertCollectionToObject() {
		List<Long> list = Collections.singletonList(3L);
		Long result = conversionService.convert(list, Long.class);
		assertEquals(new Long(3), result);
	}

	public void testConvertCollectionToObjectWithElementConversion() {
		List<String> list = Collections.singletonList("3");
		Integer result = conversionService.convert(list, Integer.class);
		assertEquals(new Integer(3), result);
	}

	public void testConvertCollectionToObjectAssignableTarget() throws Exception {
		Collection<String> source = new ArrayList<String>();
		source.add("foo");
		Object result = conversionService.convert(source, new TypeDescriptor(getClass().getField("assignableTarget")));
		assertEquals(source, result);
	}

	public void testConvertCollectionToObjectWithCustomConverter() throws Exception {
		List<String> source = new ArrayList<String>();
		source.add("A");
		source.add("B");
		conversionService.addConverter(new Converter<List, ListWrapper>() {
			@Override
			public ListWrapper convert(List source) {
				return new ListWrapper(source);
			}
		});
		ListWrapper result = conversionService.convert(source, ListWrapper.class);
		assertSame(source, result.getList());
	}

	public void testConvertObjectToCollection() {
		List<String> result = (List<String>) conversionService.convert(3L, List.class);
		assertEquals(1, result.size());
		assertEquals(3L, result.get(0));
	}

	public void testConvertObjectToCollectionWithElementConversion() throws Exception {
		List<Integer> result = (List<Integer>) conversionService.convert(3L, TypeDescriptor.valueOf(Long.class),
				new TypeDescriptor(getClass().getField("genericList")));
		assertEquals(1, result.size());
		assertEquals(new Integer(3), result.get(0));
	}

	public void testConvertArrayToArray() {
		Integer[] result = conversionService.convert(new String[] { "1", "2", "3" }, Integer[].class);
		assertEquals(new Integer(1), result[0]);
		assertEquals(new Integer(2), result[1]);
		assertEquals(new Integer(3), result[2]);
	}

	public void testConvertArrayToPrimitiveArray() {
		int[] result = conversionService.convert(new String[] { "1", "2", "3" }, int[].class);
		assertEquals(1, result[0]);
		assertEquals(2, result[1]);
		assertEquals(3, result[2]);
	}

	public void testConvertArrayToArrayAssignable() {
		int[] result = conversionService.convert(new int[] { 1, 2, 3 }, int[].class);
		assertEquals(1, result[0]);
		assertEquals(2, result[1]);
		assertEquals(3, result[2]);
	}

	public void testConvertCollectionToCollection() throws Exception {
		Set<String> foo = new LinkedHashSet<String>();
		foo.add("1");
		foo.add("2");
		foo.add("3");
		List<Integer> bar = (List<Integer>) conversionService.convert(foo, TypeDescriptor.forObject(foo),
				new TypeDescriptor(getClass().getField("genericList")));
		assertEquals(new Integer(1), bar.get(0));
		assertEquals(new Integer(2), bar.get(1));
		assertEquals(new Integer(3), bar.get(2));
	}

	public void testConvertCollectionToCollectionNull() throws Exception {
		List<Integer> bar = (List<Integer>) conversionService.convert(null,
				TypeDescriptor.valueOf(LinkedHashSet.class), new TypeDescriptor(getClass().getField("genericList")));
		assertNull(bar);
	}

	public void testConvertCollectionToCollectionNotGeneric() throws Exception {
		Set<String> foo = new LinkedHashSet<String>();
		foo.add("1");
		foo.add("2");
		foo.add("3");
		List bar = (List) conversionService.convert(foo, TypeDescriptor.valueOf(LinkedHashSet.class), TypeDescriptor
				.valueOf(List.class));
		assertEquals("1", bar.get(0));
		assertEquals("2", bar.get(1));
		assertEquals("3", bar.get(2));
	}

	public void testConvertCollectionToCollectionSpecialCaseSourceImpl() throws Exception {
		Map map = new LinkedHashMap();
		map.put("1", "1");
		map.put("2", "2");
		map.put("3", "3");
		Collection values = map.values();
		List<Integer> bar = (List<Integer>) conversionService.convert(values,
				TypeDescriptor.forObject(values), new TypeDescriptor(getClass().getField("genericList")));
		assertEquals(3, bar.size());
		assertEquals(new Integer(1), bar.get(0));
		assertEquals(new Integer(2), bar.get(1));
		assertEquals(new Integer(3), bar.get(2));
	}

	public void testCollection() {
		List<String> strings = new ArrayList<String>();
		strings.add("3");
		strings.add("9");
		List<Integer> integers = (List<Integer>) conversionService.convert(strings, TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Integer.class)));
		assertEquals(new Integer(3), integers.get(0));
		assertEquals(new Integer(9), integers.get(1));
	}

	public Map<Integer, FooEnum> genericMap = new HashMap<Integer, FooEnum>();

	public void testConvertMapToMap() throws Exception {
		Map<String, String> foo = new HashMap<String, String>();
		foo.put("1", "BAR");
		foo.put("2", "BAZ");
		Map<String, FooEnum> map = (Map<String, FooEnum>) conversionService.convert(foo,
				TypeDescriptor.forObject(foo), new TypeDescriptor(getClass().getField("genericMap")));
		assertEquals(FooEnum.BAR, map.get(1));
		assertEquals(FooEnum.BAZ, map.get(2));
	}

	public void testMap() {
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("3", "9");
		strings.put("6", "31");
		Map<Integer, Integer> integers = (Map<Integer, Integer>) conversionService.convert(strings, TypeDescriptor.map(Map.class, TypeDescriptor.valueOf(Integer.class), TypeDescriptor.valueOf(Integer.class)));
		assertEquals(new Integer(9), integers.get(3));
		assertEquals(new Integer(31), integers.get(6));
	}

	public void testConvertPropertiesToString() {
		Properties foo = new Properties();
		foo.setProperty("1", "BAR");
		foo.setProperty("2", "BAZ");
		String result = conversionService.convert(foo, String.class);
		assertTrue(result.contains("1=BAR"));
		assertTrue(result.contains("2=BAZ"));
	}

	public void testConvertStringToProperties() {
		Properties result = conversionService.convert("a=b\nc=2\nd=", Properties.class);
		assertEquals(3, result.size());
		assertEquals("b", result.getProperty("a"));
		assertEquals("2", result.getProperty("c"));
		assertEquals("", result.getProperty("d"));
	}

	public void testConvertStringToPropertiesWithSpaces() {
		Properties result = conversionService.convert("   foo=bar\n   bar=baz\n    baz=boop", Properties.class);
		assertEquals("bar", result.get("foo"));
		assertEquals("baz", result.get("bar"));
		assertEquals("boop", result.get("baz"));
	}

	// generic object conversion

	public void testConvertObjectToStringValueOfMethodPresent() {
		assertEquals("123456789", conversionService.convert(ISBN.valueOf("123456789"), String.class));
	}

	public void testConvertObjectToStringStringConstructorPresent() {
		assertEquals("123456789", conversionService.convert(new SSN("123456789"), String.class));
	}

	public void testConvertObjectToStringNotSupported() {
		assertFalse(conversionService.canConvert(TestEntity.class, String.class));
	}

	public void testConvertObjectToObjectValueOfMethod() {
		assertEquals(ISBN.valueOf("123456789"), conversionService.convert("123456789", ISBN.class));
	}

	public void testConvertObjectToObjectConstructor() {
		assertEquals(new SSN("123456789"), conversionService.convert("123456789", SSN.class));
		assertEquals("123456789", conversionService.convert(new SSN("123456789"), String.class));
	}

	public void testConvertObjectToObjectNoValueOFMethodOrConstructor() {
		try {
			conversionService.convert(new Long(3), SSN.class);
			fail("expected ConverterNotFoundException");
		}
		catch(ConverterNotFoundException e) {
		}
	}

	public void testConvertObjectToObjectFinderMethod() {
		TestEntity e = conversionService.convert(1L, TestEntity.class);
		assertEquals(new Long(1), e.getId());
	}

	public void testConvertObjectToObjectFinderMethodWithNull() {
		TestEntity e = (TestEntity) conversionService.convert(null, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(TestEntity.class));
		assertNull(e);
	}

	public void testConvertObjectToObjectFinderMethodWithIdConversion() {
		TestEntity e = conversionService.convert("1", TestEntity.class);
		assertEquals(new Long(1), e.getId());
	}

	public void testConvertCharArrayToString() throws Exception {
		String converted = conversionService.convert(new char[] { 'a', 'b', 'c' }, String.class);
		assertThat(converted, equalTo("a,b,c"));
	}

	public void testConvertStringToCharArray() throws Exception {
		char[] converted = conversionService.convert("a,b,c", char[].class);
		assertThat(converted, equalTo(new char[] { 'a', 'b', 'c' }));
	}

	public void testConvertStringToCustomCharArray() throws Exception {
		conversionService.addConverter(new Converter<String, char[]>() {
			@Override
			public char[] convert(String source) {
				return source.toCharArray();
			}
		});
		char[] converted = conversionService.convert("abc", char[].class);
		assertThat(converted, equalTo(new char[] { 'a', 'b', 'c' }));
	}

	public void testMultidimensionalArrayToListConversionShouldConvertEntriesCorrectly() {
		String[][] grid = new String[][] { new String[] { "1", "2", "3", "4" }, new String[] { "5", "6", "7", "8" },
				new String[] { "9", "10", "11", "12" } };
		List<String[]> converted = conversionService.convert(grid, List.class);
		String[][] convertedBack = conversionService.convert(converted, String[][].class);
		Assert.assertArrayEquals(grid, convertedBack);
	}


	public static class TestEntity {

		private Long id;

		public TestEntity(Long id) {
			this.id = id;
		}

		public Long getId() {
			return id;
		}

		public static TestEntity findTestEntity(Long id) {
			return new TestEntity(id);
		}
	}


	private static class ListWrapper {

		private List<?> list;

		public ListWrapper(List<?> list) {
			this.list = list;
		}

		public List<?> getList() {
			return list;
		}
	}


	public Object assignableTarget;


	private static class SSN {

		private String value;

		public SSN(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SSN)) {
				return false;
			}
			SSN ssn = (SSN) o;
			return this.value.equals(ssn.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public String toString() {
			return value;
		}
	}


	private static class ISBN {

		private String value;

		private ISBN(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ISBN)) {
				return false;
			}
			ISBN isbn = (ISBN) o;
			return this.value.equals(isbn.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public String toString() {
			return value;
		}

		public static ISBN valueOf(String value) {
			return new ISBN(value);
		}
	}

}
