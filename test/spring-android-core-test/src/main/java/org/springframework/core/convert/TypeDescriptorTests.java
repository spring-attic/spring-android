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

package org.springframework.core.convert;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.core.MethodParameter;

/**
 * @author Keith Donald
 * @author Andy Clement
 * @author Phillip Webb
 */
@SuppressWarnings("rawtypes")
public class TypeDescriptorTests extends TestCase {

	public List<String> listOfString;

	public List<List<String>> listOfListOfString = new ArrayList<List<String>>();

	public List<List> listOfListOfUnknown = new ArrayList<List>();

	public int[] intArray;

	public List<String>[] arrayOfListOfString;

	public List<Integer> listField = new ArrayList<Integer>();

	public Map<String, Integer> mapField = new HashMap<String, Integer>();

	public Map<String, List<Integer>> nestedMapField = new HashMap<String, List<Integer>>();

	public void testParameterPrimitive() throws Exception {
		TypeDescriptor desc = new TypeDescriptor(new MethodParameter(getClass().getMethod("testParameterPrimitive", int.class), 0));
		assertEquals(int.class, desc.getType());
		assertEquals(Integer.class, desc.getObjectType());
		assertEquals("int", desc.getName());
		assertEquals("int", desc.toString());
		assertTrue(desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertFalse(desc.isCollection());
		assertFalse(desc.isMap());
	}

	public void testParameterPrimitive(int primitive) {

	}

	public void testParameterScalar() throws Exception {
		TypeDescriptor desc = new TypeDescriptor(new MethodParameter(getClass().getMethod("testParameterScalar", String.class), 0));
		assertEquals(String.class, desc.getType());
		assertEquals(String.class, desc.getObjectType());
		assertEquals("java.lang.String", desc.getName());
		assertEquals("java.lang.String", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertFalse(desc.isCollection());
		assertFalse(desc.isArray());
		assertFalse(desc.isMap());
	}

	public void testParameterScalar(String value) {

	}

	public void testParameterList() throws Exception {
		MethodParameter methodParameter = new MethodParameter(getClass().getMethod("testParameterList", List.class), 0);
		TypeDescriptor desc = new TypeDescriptor(methodParameter);
		assertEquals(List.class, desc.getType());
		assertEquals(List.class, desc.getObjectType());
		assertEquals("java.util.List", desc.getName());
		assertEquals("java.util.List<java.util.List<java.util.Map<java.lang.Integer, java.lang.Enum>>>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertTrue(desc.isCollection());
		assertFalse(desc.isArray());
		assertEquals(List.class, desc.getElementTypeDescriptor().getType());
		assertEquals(TypeDescriptor.nested(methodParameter, 1), desc.getElementTypeDescriptor());
		assertEquals(TypeDescriptor.nested(methodParameter, 2), desc.getElementTypeDescriptor().getElementTypeDescriptor());
		assertEquals(TypeDescriptor.nested(methodParameter, 3), desc.getElementTypeDescriptor().getElementTypeDescriptor().getMapValueTypeDescriptor());
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getElementTypeDescriptor().getMapKeyTypeDescriptor().getType());
		assertEquals(Enum.class, desc.getElementTypeDescriptor().getElementTypeDescriptor().getMapValueTypeDescriptor().getType());
		assertFalse(desc.isMap());
	}

	public void testParameterList(List<List<Map<Integer, Enum<?>>>> list) {

	}

	public void testParameterListNoParamTypes() throws Exception {
		MethodParameter methodParameter = new MethodParameter(getClass().getMethod("testParameterListNoParamTypes", List.class), 0);
		TypeDescriptor desc = new TypeDescriptor(methodParameter);
		assertEquals(List.class, desc.getType());
		assertEquals(List.class, desc.getObjectType());
		assertEquals("java.util.List", desc.getName());
		assertEquals("java.util.List<?>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertTrue(desc.isCollection());
		assertFalse(desc.isArray());
		assertNull(desc.getElementTypeDescriptor());
		assertFalse(desc.isMap());
	}

	public void testParameterListNoParamTypes(List list) {

	}

	public void testParameterArray() throws Exception {
		MethodParameter methodParameter = new MethodParameter(getClass().getMethod("testParameterArray", Integer[].class), 0);
		TypeDescriptor desc = new TypeDescriptor(methodParameter);
		assertEquals(Integer[].class, desc.getType());
		assertEquals(Integer[].class, desc.getObjectType());
		assertEquals("java.lang.Integer[]", desc.getName());
		assertEquals("java.lang.Integer[]", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertFalse(desc.isCollection());
		assertTrue(desc.isArray());
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
		assertEquals(TypeDescriptor.valueOf(Integer.class), desc.getElementTypeDescriptor());
		assertFalse(desc.isMap());
	}

	public void testParameterArray(Integer[] array) {

	}

	public void testParameterMap() throws Exception {
		MethodParameter methodParameter = new MethodParameter(getClass().getMethod("testParameterMap", Map.class), 0);
		TypeDescriptor desc = new TypeDescriptor(methodParameter);
		assertEquals(Map.class, desc.getType());
		assertEquals(Map.class, desc.getObjectType());
		assertEquals("java.util.Map", desc.getName());
		assertEquals("java.util.Map<java.lang.Integer, java.util.List<java.lang.String>>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertFalse(desc.isCollection());
		assertFalse(desc.isArray());
		assertTrue(desc.isMap());
		assertEquals(TypeDescriptor.nested(methodParameter, 1), desc.getMapValueTypeDescriptor());
		assertEquals(TypeDescriptor.nested(methodParameter, 2), desc.getMapValueTypeDescriptor().getElementTypeDescriptor());
		assertEquals(Integer.class, desc.getMapKeyTypeDescriptor().getType());
		assertEquals(List.class, desc.getMapValueTypeDescriptor().getType());
		assertEquals(String.class, desc.getMapValueTypeDescriptor().getElementTypeDescriptor().getType());
	}

	public void testParameterMap(Map<Integer, List<String>> map) {

	}

	public void testParameterAnnotated() throws Exception {
		TypeDescriptor t1 = new TypeDescriptor(new MethodParameter(getClass().getMethod("testAnnotatedMethod", String.class), 0));
		assertEquals(String.class, t1.getType());
		assertEquals(1, t1.getAnnotations().length);
		assertNotNull(t1.getAnnotation(ParameterAnnotation.class));
		assertTrue(t1.hasAnnotation(ParameterAnnotation.class));
		assertEquals(123, t1.getAnnotation(ParameterAnnotation.class).value());
	}

	@Target({ElementType.PARAMETER})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ParameterAnnotation {
		int value();
	}

	public void testAnnotatedMethod(@ParameterAnnotation(123) String parameter) {

	}

	public void testPropertyComplex() throws Exception {
		Property property = new Property(getClass(), getClass().getMethod("getComplexProperty"), getClass().getMethod("setComplexProperty", Map.class));
		TypeDescriptor desc = new TypeDescriptor(property);
		assertEquals(String.class, desc.getMapKeyTypeDescriptor().getType());
		assertEquals(Integer.class, desc.getMapValueTypeDescriptor().getElementTypeDescriptor().getElementTypeDescriptor().getType());
	}

	public Map<String, List<List<Integer>>> getComplexProperty() {
		return null;
	}

	public void setComplexProperty(Map<String, List<List<Integer>>> complexProperty) {

	}

	public void testPropertyGenericType() throws Exception {
		GenericType<Integer> genericBean = new IntegerType();
		Property property = new Property(getClass(), genericBean.getClass().getMethod("getProperty"), genericBean.getClass().getMethod("setProperty", Integer.class));
		TypeDescriptor desc = new TypeDescriptor(property);
		assertEquals(Integer.class, desc.getType());
	}

	public void testPropertyTypeCovariance() throws Exception {
		GenericType<Number> genericBean = new NumberType();
		Property property = new Property(getClass(), genericBean.getClass().getMethod("getProperty"), genericBean.getClass().getMethod("setProperty", Number.class));
		TypeDescriptor desc = new TypeDescriptor(property);
		assertEquals(Integer.class, desc.getType());
	}

	public void testPropertyGenericTypeList() throws Exception {
		GenericType<Integer> genericBean = new IntegerType();
		Property property = new Property(getClass(), genericBean.getClass().getMethod("getListProperty"), genericBean.getClass().getMethod("setListProperty", List.class));
		TypeDescriptor desc = new TypeDescriptor(property);
		assertEquals(List.class, desc.getType());
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
	}

	public interface GenericType<T> {
		T getProperty();

		void setProperty(T t);

		List<T> getListProperty();

		void setListProperty(List<T> t);

 	}

	public class IntegerType implements GenericType<Integer> {

		@Override
		public Integer getProperty() {
			return null;
		}

		@Override
		public void setProperty(Integer t) {
		}

		@Override
		public List<Integer> getListProperty() {
			return null;
		}

		@Override
		public void setListProperty(List<Integer> t) {
		}
	}

	public class NumberType implements GenericType<Number> {

		@Override
		public Integer getProperty() {
			return null;
		}

		@Override
		public void setProperty(Number t) {
		}

		@Override
		public List<Number> getListProperty() {
			return null;
		}

		@Override
		public void setListProperty(List<Number> t) {
		}
	}

	public void testPropertyGenericClassList() throws Exception {
		IntegerClass genericBean = new IntegerClass();
		Property property = new Property(genericBean.getClass(), genericBean.getClass().getMethod("getListProperty"), genericBean.getClass().getMethod("setListProperty", List.class));
		TypeDescriptor desc = new TypeDescriptor(property);
		assertEquals(List.class, desc.getType());
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
		assertNotNull(desc.getAnnotation(MethodAnnotation1.class));
		assertTrue(desc.hasAnnotation(MethodAnnotation1.class));
	}

	public static class GenericClass<T> {

		public T getProperty() {
			return null;
		}

		public void setProperty(T t) {
		}

		@MethodAnnotation1
		public List<T> getListProperty() {
			return null;
		}

		public void setListProperty(List<T> t) {
		}

	}

	public static class IntegerClass extends GenericClass<Integer> {

	}

	public void testProperty() throws Exception {
		Property property = new Property(getClass(), getClass().getMethod("getProperty"), getClass().getMethod("setProperty", Map.class));
		TypeDescriptor desc = new TypeDescriptor(property);
		assertEquals(Map.class, desc.getType());
		assertEquals(Integer.class, desc.getMapKeyTypeDescriptor().getElementTypeDescriptor().getType());
		assertEquals(Long.class, desc.getMapValueTypeDescriptor().getElementTypeDescriptor().getType());
		assertNotNull(desc.getAnnotation(MethodAnnotation1.class));
		assertNotNull(desc.getAnnotation(MethodAnnotation2.class));
		assertNotNull(desc.getAnnotation(MethodAnnotation3.class));
	}

	@MethodAnnotation1
	public Map<List<Integer>, List<Long>> getProperty() {
		return property;
	}

	@MethodAnnotation2
	public void setProperty(Map<List<Integer>, List<Long>> property) {
		this.property = property;
	}

	@MethodAnnotation3
	private Map<List<Integer>, List<Long>> property;

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MethodAnnotation1 {

	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MethodAnnotation2 {

	}

	@Target({ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MethodAnnotation3 {

	}

	public void testFieldScalar() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(getClass().getField("fieldScalar"));
		assertFalse(typeDescriptor.isPrimitive());
		assertFalse(typeDescriptor.isArray());
		assertFalse(typeDescriptor.isCollection());
		assertFalse(typeDescriptor.isMap());
		assertEquals(Integer.class, typeDescriptor.getType());
		assertEquals(Integer.class, typeDescriptor.getObjectType());
	}

	public Integer fieldScalar;

	public void testFieldList() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(TypeDescriptorTests.class.getDeclaredField("listOfString"));
		assertFalse(typeDescriptor.isArray());
		assertEquals(List.class, typeDescriptor.getType());
		assertEquals(String.class, typeDescriptor.getElementTypeDescriptor().getType());
		assertEquals("java.util.List<java.lang.String>", typeDescriptor.toString());
	}

	public void testFieldListOfListOfString() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(TypeDescriptorTests.class.getDeclaredField("listOfListOfString"));
		assertFalse(typeDescriptor.isArray());
		assertEquals(List.class, typeDescriptor.getType());
		assertEquals(List.class, typeDescriptor.getElementTypeDescriptor().getType());
		assertEquals(String.class, typeDescriptor.getElementTypeDescriptor().getElementTypeDescriptor().getType());
		assertEquals("java.util.List<java.util.List<java.lang.String>>", typeDescriptor.toString());
	}

	public void testFieldListOfListUnknown() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(TypeDescriptorTests.class.getDeclaredField("listOfListOfUnknown"));
		assertFalse(typeDescriptor.isArray());
		assertEquals(List.class, typeDescriptor.getType());
		assertEquals(List.class, typeDescriptor.getElementTypeDescriptor().getType());
		assertNull(typeDescriptor.getElementTypeDescriptor().getElementTypeDescriptor());
		assertEquals("java.util.List<java.util.List<?>>", typeDescriptor.toString());
	}

	public void testFieldArray() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(TypeDescriptorTests.class.getDeclaredField("intArray"));
		assertTrue(typeDescriptor.isArray());
		assertEquals(Integer.TYPE,typeDescriptor.getElementTypeDescriptor().getType());
		assertEquals("int[]",typeDescriptor.toString());
	}

	public void testFieldComplexTypeDescriptor() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(TypeDescriptorTests.class.getDeclaredField("arrayOfListOfString"));
		assertTrue(typeDescriptor.isArray());
		assertEquals(List.class,typeDescriptor.getElementTypeDescriptor().getType());
		assertEquals(String.class, typeDescriptor.getElementTypeDescriptor().getElementTypeDescriptor().getType());
		assertEquals("java.util.List[]",typeDescriptor.toString());
	}

	public void testFieldComplexTypeDescriptor2() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(TypeDescriptorTests.class.getDeclaredField("nestedMapField"));
		assertTrue(typeDescriptor.isMap());
		assertEquals(String.class,typeDescriptor.getMapKeyTypeDescriptor().getType());
		assertEquals(List.class, typeDescriptor.getMapValueTypeDescriptor().getType());
		assertEquals(Integer.class, typeDescriptor.getMapValueTypeDescriptor().getElementTypeDescriptor().getType());
		assertEquals("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>", typeDescriptor.toString());
	}

	public void testFieldMap() throws Exception {
		TypeDescriptor desc = new TypeDescriptor(TypeDescriptorTests.class.getField("fieldMap"));
		assertTrue(desc.isMap());
		assertEquals(Integer.class, desc.getMapKeyTypeDescriptor().getElementTypeDescriptor().getType());
		assertEquals(Long.class, desc.getMapValueTypeDescriptor().getElementTypeDescriptor().getType());
	}

	public Map<List<Integer>, List<Long>> fieldMap;

	public void testFieldAnnotated() throws Exception {
		TypeDescriptor typeDescriptor = new TypeDescriptor(getClass().getField("fieldAnnotated"));
		assertEquals(1, typeDescriptor.getAnnotations().length);
		assertNotNull(typeDescriptor.getAnnotation(FieldAnnotation.class));
	}

	@FieldAnnotation
	public List<String> fieldAnnotated;

	@Target({ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface FieldAnnotation {

	}

	public void testValueOfScalar() {
		TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(Integer.class);
		assertFalse(typeDescriptor.isPrimitive());
		assertFalse(typeDescriptor.isArray());
		assertFalse(typeDescriptor.isCollection());
		assertFalse(typeDescriptor.isMap());
		assertEquals(Integer.class, typeDescriptor.getType());
		assertEquals(Integer.class, typeDescriptor.getObjectType());
	}

	public void testValueOfPrimitive() {
		TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(int.class);
		assertTrue(typeDescriptor.isPrimitive());
		assertFalse(typeDescriptor.isArray());
		assertFalse(typeDescriptor.isCollection());
		assertFalse(typeDescriptor.isMap());
		assertEquals(Integer.TYPE, typeDescriptor.getType());
		assertEquals(Integer.class, typeDescriptor.getObjectType());
	}

	public void testValueOfArray() throws Exception {
		TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(int[].class);
		assertTrue(typeDescriptor.isArray());
		assertFalse(typeDescriptor.isCollection());
		assertFalse(typeDescriptor.isMap());
		assertEquals(Integer.TYPE, typeDescriptor.getElementTypeDescriptor().getType());
	}

	public void testValueOfCollection() throws Exception {
		TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(Collection.class);
		assertTrue(typeDescriptor.isCollection());
		assertFalse(typeDescriptor.isArray());
		assertFalse(typeDescriptor.isMap());
		assertNull(typeDescriptor.getElementTypeDescriptor());
	}

	public void testForObject() {
		TypeDescriptor desc = TypeDescriptor.forObject("3");
		assertEquals(String.class, desc.getType());
	}

	public void testForObjectNullTypeDescriptor() {
		TypeDescriptor desc = TypeDescriptor.forObject(null);
		assertNull(desc);
	}

	public void testNestedMethodParameterType2Levels() throws Exception {
		TypeDescriptor t1 = TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test2", List.class), 0), 2);
		assertEquals(String.class, t1.getType());
	}

	public void testNestedMethodParameterTypeMap() throws Exception {
		TypeDescriptor t1 = TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test3", Map.class), 0), 1);
		assertEquals(String.class, t1.getType());
	}

	public void testNestedMethodParameterTypeMapTwoLevels() throws Exception {
		TypeDescriptor t1 = TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test4", List.class), 0), 2);
		assertEquals(String.class, t1.getType());
	}

	public void testNestedMethodParameterNot1NestedLevel() throws Exception {
		try {
			TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test4", List.class), 0, 2), 2);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e){
		}
	}

	public void testNestedTooManyLevels() throws Exception {
		try {
			TypeDescriptor t1 = TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test4", List.class), 0), 3);
			assertEquals(String.class, t1.getType());
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException e) {
		}
	}

	public void testNestedMethodParameterTypeNotNestable() throws Exception {
		try {
			TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test5", String.class), 0), 2);
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException e) {
		}
	}

	public void testNestedMethodParameterTypeInvalidNestingLevel() throws Exception {
		try {
			TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test5", String.class), 0, 2), 2);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void test1(List<String> param1) {

	}

	public void test2(List<List<String>> param1) {

	}

	public void test3(Map<Integer, String> param1) {

	}

	public void test4(List<Map<Integer, String>> param1) {

	}

	public void test5(String param1) {

	}

	public void testNestedNotParameterized() throws Exception {
		TypeDescriptor t1 = TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test6", List.class), 0), 1);
		assertEquals(List.class,t1.getType());
		assertEquals("java.util.List<?>", t1.toString());
		TypeDescriptor t2 = TypeDescriptor.nested(new MethodParameter(getClass().getMethod("test6", List.class), 0), 2);
		assertNull(t2);
	}

	public void test6(List<List> param1) {

	}

	public void testNestedFieldTypeMapTwoLevels() throws Exception {
		TypeDescriptor t1 = TypeDescriptor.nested(getClass().getField("test4"), 2);
		assertEquals(String.class, t1.getType());
	}

	public List<Map<Integer, String>> test4;

	public void testNestedPropertyTypeMapTwoLevels() throws Exception {
		Property property = new Property(getClass(), getClass().getMethod("getTest4"), getClass().getMethod("setTest4", List.class));
		TypeDescriptor t1 = TypeDescriptor.nested(property, 2);
		assertEquals(String.class, t1.getType());
	}

	public List<Map<Integer, String>> getTest4() {
		return null;
	}

	public void setTest4(List<Map<Integer, String>> test4) {

	}

	public void testCollection() {
		TypeDescriptor desc = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Integer.class));
		assertEquals(List.class, desc.getType());
		assertEquals(List.class, desc.getObjectType());
		assertEquals("java.util.List", desc.getName());
		assertEquals("java.util.List<java.lang.Integer>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertTrue(desc.isCollection());
		assertFalse(desc.isArray());
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
		assertEquals(TypeDescriptor.valueOf(Integer.class), desc.getElementTypeDescriptor());
		assertFalse(desc.isMap());
	}

	public void testCollectionNested() {
		TypeDescriptor desc = TypeDescriptor.collection(List.class, TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Integer.class)));
		assertEquals(List.class, desc.getType());
		assertEquals(List.class, desc.getObjectType());
		assertEquals("java.util.List", desc.getName());
		assertEquals("java.util.List<java.util.List<java.lang.Integer>>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertTrue(desc.isCollection());
		assertFalse(desc.isArray());
		assertEquals(List.class, desc.getElementTypeDescriptor().getType());
		assertEquals(TypeDescriptor.valueOf(Integer.class), desc.getElementTypeDescriptor().getElementTypeDescriptor());
		assertFalse(desc.isMap());
	}

	public void testMap() {
		TypeDescriptor desc = TypeDescriptor.map(Map.class, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Integer.class));
		assertEquals(Map.class, desc.getType());
		assertEquals(Map.class, desc.getObjectType());
		assertEquals("java.util.Map", desc.getName());
		assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertFalse(desc.isCollection());
		assertFalse(desc.isArray());
		assertTrue(desc.isMap());
		assertEquals(String.class, desc.getMapKeyTypeDescriptor().getType());
		assertEquals(Integer.class, desc.getMapValueTypeDescriptor().getType());
	}

	public void testMapNested() {
		TypeDescriptor desc = TypeDescriptor.map(Map.class, TypeDescriptor.valueOf(String.class),
				TypeDescriptor.map(Map.class, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Integer.class)));
		assertEquals(Map.class, desc.getType());
		assertEquals(Map.class, desc.getObjectType());
		assertEquals("java.util.Map", desc.getName());
		assertEquals("java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>", desc.toString());
		assertTrue(!desc.isPrimitive());
		assertEquals(0, desc.getAnnotations().length);
		assertFalse(desc.isCollection());
		assertFalse(desc.isArray());
		assertTrue(desc.isMap());
		assertEquals(String.class, desc.getMapKeyTypeDescriptor().getType());
		assertEquals(String.class, desc.getMapValueTypeDescriptor().getMapKeyTypeDescriptor().getType());
		assertEquals(Integer.class, desc.getMapValueTypeDescriptor().getMapValueTypeDescriptor().getType());
	}

	public void testNarrow() {
		TypeDescriptor desc = TypeDescriptor.valueOf(Number.class);
		Integer value = new Integer(3);
		desc = desc.narrow(value);
		assertEquals(Integer.class, desc.getType());
	}

	public void testElementType() {
		TypeDescriptor desc = TypeDescriptor.valueOf(List.class);
		Integer value = new Integer(3);
		desc = desc.elementTypeDescriptor(value);
		assertEquals(Integer.class, desc.getType());
	}

	public void testElementTypePreserveContext() throws Exception {
		TypeDescriptor desc = new TypeDescriptor(getClass().getField("listPreserveContext"));
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getElementTypeDescriptor().getType());
		List<Integer> value = new ArrayList<Integer>(3);
		desc = desc.elementTypeDescriptor(value);
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
		assertNotNull(desc.getAnnotation(FieldAnnotation.class));
	}

	@FieldAnnotation
	public List<List<Integer>> listPreserveContext;

	public void testMapKeyType() {
		TypeDescriptor desc = TypeDescriptor.valueOf(Map.class);
		Integer value = new Integer(3);
		desc = desc.getMapKeyTypeDescriptor(value);
		assertEquals(Integer.class, desc.getType());
	}

	public void testMapKeyTypePreserveContext() throws Exception {
		TypeDescriptor desc = new TypeDescriptor(getClass().getField("mapPreserveContext"));
		assertEquals(Integer.class, desc.getMapKeyTypeDescriptor().getElementTypeDescriptor().getType());
		List<Integer> value = new ArrayList<Integer>(3);
		desc = desc.getMapKeyTypeDescriptor(value);
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
		assertNotNull(desc.getAnnotation(FieldAnnotation.class));
	}

	@FieldAnnotation
	public Map<List<Integer>, List<Integer>> mapPreserveContext;

	public void testMapValueType() {
		TypeDescriptor desc = TypeDescriptor.valueOf(Map.class);
		Integer value = new Integer(3);
		desc = desc.getMapValueTypeDescriptor(value);
		assertEquals(Integer.class, desc.getType());
	}

	public void testMapValueTypePreserveContext() throws Exception {
		TypeDescriptor desc = new TypeDescriptor(getClass().getField("mapPreserveContext"));
		assertEquals(Integer.class, desc.getMapValueTypeDescriptor().getElementTypeDescriptor().getType());
		List<Integer> value = new ArrayList<Integer>(3);
		desc = desc.getMapValueTypeDescriptor(value);
		assertEquals(Integer.class, desc.getElementTypeDescriptor().getType());
		assertNotNull(desc.getAnnotation(FieldAnnotation.class));
	}

	public void testEquals() throws Exception {
		TypeDescriptor t1 = TypeDescriptor.valueOf(String.class);
		TypeDescriptor t2 = TypeDescriptor.valueOf(String.class);
		TypeDescriptor t3 = TypeDescriptor.valueOf(Date.class);
		TypeDescriptor t4 = TypeDescriptor.valueOf(Date.class);
		TypeDescriptor t5 = TypeDescriptor.valueOf(List.class);
		TypeDescriptor t6 = TypeDescriptor.valueOf(List.class);
		TypeDescriptor t7 = TypeDescriptor.valueOf(Map.class);
		TypeDescriptor t8 = TypeDescriptor.valueOf(Map.class);
		assertEquals(t1, t2);
		assertEquals(t3, t4);
		assertEquals(t5, t6);
		assertEquals(t7, t8);

		TypeDescriptor t9 = new TypeDescriptor(getClass().getField("listField"));
		TypeDescriptor t10 = new TypeDescriptor(getClass().getField("listField"));
		assertEquals(t9, t10);

		TypeDescriptor t11 = new TypeDescriptor(getClass().getField("mapField"));
		TypeDescriptor t12 = new TypeDescriptor(getClass().getField("mapField"));
		assertEquals(t11, t12);
	}

	public void testIsAssignableTypes() {
		assertTrue(TypeDescriptor.valueOf(Integer.class).isAssignableTo(TypeDescriptor.valueOf(Number.class)));
		assertFalse(TypeDescriptor.valueOf(Number.class).isAssignableTo(TypeDescriptor.valueOf(Integer.class)));
		assertFalse(TypeDescriptor.valueOf(String.class).isAssignableTo(TypeDescriptor.valueOf(String[].class)));
	}

	public void testIsAssignableElementTypes() throws Exception {
		assertTrue(new TypeDescriptor(getClass().getField("listField")).isAssignableTo(new TypeDescriptor(getClass().getField("listField"))));
		assertTrue(new TypeDescriptor(getClass().getField("notGenericList")).isAssignableTo(new TypeDescriptor(getClass().getField("listField"))));
		assertTrue(new TypeDescriptor(getClass().getField("listField")).isAssignableTo(new TypeDescriptor(getClass().getField("notGenericList"))));
		assertFalse(new TypeDescriptor(getClass().getField("isAssignableElementTypes")).isAssignableTo(new TypeDescriptor(getClass().getField("listField"))));
		assertTrue(TypeDescriptor.valueOf(List.class).isAssignableTo(new TypeDescriptor(getClass().getField("listField"))));
	}

	public List notGenericList;

	public List<Number> isAssignableElementTypes;

	public void testIsAssignableMapKeyValueTypes() throws Exception {
		assertTrue(new TypeDescriptor(getClass().getField("mapField")).isAssignableTo(new TypeDescriptor(getClass().getField("mapField"))));
		assertTrue(new TypeDescriptor(getClass().getField("notGenericMap")).isAssignableTo(new TypeDescriptor(getClass().getField("mapField"))));
		assertTrue(new TypeDescriptor(getClass().getField("mapField")).isAssignableTo(new TypeDescriptor(getClass().getField("notGenericMap"))));
		assertFalse(new TypeDescriptor(getClass().getField("isAssignableMapKeyValueTypes")).isAssignableTo(new TypeDescriptor(getClass().getField("mapField"))));
		assertTrue(TypeDescriptor.valueOf(Map.class).isAssignableTo(new TypeDescriptor(getClass().getField("mapField"))));
	}

	public Map notGenericMap;

	public Map<CharSequence, Number> isAssignableMapKeyValueTypes;

	public void testUpCast() throws Exception {
		Property property = new Property(getClass(), getClass().getMethod("getProperty"),
				getClass().getMethod("setProperty", Map.class));
		TypeDescriptor typeDescriptor = new TypeDescriptor(property);
		TypeDescriptor upCast = typeDescriptor.upcast(Object.class);
		assertTrue(upCast.getAnnotation(MethodAnnotation1.class) != null);
	}

	public void testUpCastNotSuper() throws Exception {
		Property property = new Property(getClass(), getClass().getMethod("getProperty"),
				getClass().getMethod("setProperty", Map.class));
		TypeDescriptor typeDescriptor = new TypeDescriptor(property);
		try {
			typeDescriptor.upcast(Collection.class);
			fail("Did not throw");
		} catch(IllegalArgumentException e) {
			assertEquals("interface java.util.Map is not assignable to interface java.util.Collection", e.getMessage());
		}
	}

	public void testElementTypeForCollectionSubclass() throws Exception {
		@SuppressWarnings("serial")
		class CustomSet extends HashSet<String> {
		}

		assertEquals(TypeDescriptor.valueOf(CustomSet.class).getElementTypeDescriptor(), TypeDescriptor.valueOf(String.class));
		assertEquals(TypeDescriptor.forObject(new CustomSet()).getElementTypeDescriptor(), TypeDescriptor.valueOf(String.class));
	}

	public void testElementTypeForMapSubclass() throws Exception {
		@SuppressWarnings("serial")
		class CustomMap extends HashMap<String, Integer> {
		}

		assertEquals(TypeDescriptor.valueOf(CustomMap.class).getMapKeyTypeDescriptor(), TypeDescriptor.valueOf(String.class));
		assertEquals(TypeDescriptor.valueOf(CustomMap.class).getMapValueTypeDescriptor(), TypeDescriptor.valueOf(Integer.class));
		assertEquals(TypeDescriptor.forObject(new CustomMap()).getMapKeyTypeDescriptor(), TypeDescriptor.valueOf(String.class));
		assertEquals(TypeDescriptor.forObject(new CustomMap()).getMapValueTypeDescriptor(), TypeDescriptor.valueOf(Integer.class));
	}

	public void testCreateMapArray() throws Exception {
		TypeDescriptor mapType = TypeDescriptor.map(LinkedHashMap.class, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Integer.class));
		TypeDescriptor arrayType = TypeDescriptor.array(mapType);
		assertEquals(arrayType.getType(), LinkedHashMap[].class);
		assertEquals(arrayType.getElementTypeDescriptor(), mapType);
	}

	public void testCreateStringArray() throws Exception {
		TypeDescriptor arrayType = TypeDescriptor.array(TypeDescriptor.valueOf(String.class));
		assertEquals(arrayType, TypeDescriptor.valueOf(String[].class));
	}

	public void testCreateNullArray() throws Exception {
		assertNull(TypeDescriptor.array(null));
	}

	public void testSerializable() throws Exception {
		TypeDescriptor typeDescriptor = TypeDescriptor.forObject("");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(out);
		outputStream.writeObject(typeDescriptor);
		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		TypeDescriptor readObject = (TypeDescriptor) inputStream.readObject();
		assertThat(readObject, equalTo(typeDescriptor));
	}

}
