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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.tests.sample.objects.TestObject;

import android.os.RemoteException;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class ReflectionUtilsTests extends TestCase {

	public void testFindField() {
		Field field = ReflectionUtils.findField(TestObjectSubclassWithPublicField.class, "publicField", String.class);
		assertNotNull(field);
		assertEquals("publicField", field.getName());
		assertEquals(String.class, field.getType());
		assertTrue("Field should be public.", Modifier.isPublic(field.getModifiers()));

		field = ReflectionUtils.findField(TestObjectSubclassWithNewField.class, "prot", String.class);
		assertNotNull(field);
		assertEquals("prot", field.getName());
		assertEquals(String.class, field.getType());
		assertTrue("Field should be protected.", Modifier.isProtected(field.getModifiers()));

		field = ReflectionUtils.findField(TestObjectSubclassWithNewField.class, "name", String.class);
		assertNotNull(field);
		assertEquals("name", field.getName());
		assertEquals(String.class, field.getType());
		assertTrue("Field should be private.", Modifier.isPrivate(field.getModifiers()));
	}

	public void testSetField() {
		final TestObjectSubclassWithNewField testBean = new TestObjectSubclassWithNewField();
		final Field field = ReflectionUtils.findField(TestObjectSubclassWithNewField.class, "name", String.class);

		ReflectionUtils.makeAccessible(field);

		ReflectionUtils.setField(field, testBean, "FooBar");
		assertNotNull(testBean.getName());
		assertEquals("FooBar", testBean.getName());

		ReflectionUtils.setField(field, testBean, null);
		assertNull(testBean.getName());
	}

	public void testSetFieldIllegal() {
		boolean success = false;
		try {
			final TestObjectSubclassWithNewField testBean = new TestObjectSubclassWithNewField();
			final Field field = ReflectionUtils.findField(TestObjectSubclassWithNewField.class, "name", String.class);
			ReflectionUtils.setField(field, testBean, "FooBar");
		}
		catch (IllegalStateException e) {
			success = true;
		}
		assertTrue("Expected IllegalStateException", success);
	}

	public void testInvokeMethod() throws Exception {
		String rob = "Rob Harrop";

		TestObject bean = new TestObject();
		bean.setName(rob);

		Method getName = TestObject.class.getMethod("getName", (Class[]) null);
		Method setName = TestObject.class.getMethod("setName", new Class[] { String.class });

		Object name = ReflectionUtils.invokeMethod(getName, bean);
		assertEquals("Incorrect name returned", rob, name);

		String juergen = "Juergen Hoeller";
		ReflectionUtils.invokeMethod(setName, bean, new Object[] { juergen });
		assertEquals("Incorrect name set", juergen, bean.getName());
	}

	public void testDeclaresException() throws Exception {
		Method remoteExMethod = A.class.getDeclaredMethod("foo", new Class[] { Integer.class });
		assertFalse(ReflectionUtils.declaresException(remoteExMethod, NoSuchMethodException.class));
		assertFalse(ReflectionUtils.declaresException(remoteExMethod, Exception.class));

		Method illegalExMethod = B.class.getDeclaredMethod("bar", new Class[] { String.class });
		assertTrue(ReflectionUtils.declaresException(illegalExMethod, IllegalArgumentException.class));
		assertTrue(ReflectionUtils.declaresException(illegalExMethod, NumberFormatException.class));
		assertFalse(ReflectionUtils.declaresException(illegalExMethod, IllegalStateException.class));
		assertFalse(ReflectionUtils.declaresException(illegalExMethod, Exception.class));
	}

	public void testCopySrcToDestinationOfIncorrectClass() {
		TestObject src = new TestObject();
		String dest = new String();
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		} catch (IllegalArgumentException ex) {
			// Ok
		}
	}

	public void testRejectsNullSrc() {
		TestObject src = null;
		String dest = new String();
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		} catch (IllegalArgumentException ex) {
			// Ok
		}
	}

	public void testRejectsNullDest() {
		TestObject src = new TestObject();
		String dest = null;
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		} catch (IllegalArgumentException ex) {
			// Ok
		}
	}

	public void testValidCopy() {
		TestObject src = new TestObject();
		TestObject dest = new TestObject();
		confirmValidCopy(src, dest);
	}

	public void testValidCopyOnSubTypeWithNewField() {
		TestObjectSubclassWithNewField src = new TestObjectSubclassWithNewField();
		TestObjectSubclassWithNewField dest = new TestObjectSubclassWithNewField();
		src.magic = 11;

		// Will check inherited fields are copied
		confirmValidCopy(src, dest);

		// Check subclass fields were copied
		assertEquals(src.magic, dest.magic);
		assertEquals(src.prot, dest.prot);
	}

	public void testValidCopyToSubType() {
		TestObject src = new TestObject();
		TestObjectSubclassWithNewField dest = new TestObjectSubclassWithNewField();
		dest.magic = 11;
		confirmValidCopy(src, dest);
		// Should have left this one alone
		assertEquals(11, dest.magic);
	}

	public void testValidCopyToSubTypeWithFinalField() {
		TestObjectSubclassWithFinalField src = new TestObjectSubclassWithFinalField();
		TestObjectSubclassWithFinalField dest = new TestObjectSubclassWithFinalField();
		// Check that this doesn't fail due to attempt to assign final
		confirmValidCopy(src, dest);
	}

	private void confirmValidCopy(TestObject src, TestObject dest) {
		src.setName("freddie");
		src.setAge(15);
		src.setSpouse(new TestObject());
		assertFalse(src.getAge() == dest.getAge());

		ReflectionUtils.shallowCopyFieldState(src, dest);
		assertEquals(src.getAge(), dest.getAge());
		assertEquals(src.getSpouse(), dest.getSpouse());
	}

	public void testDoWithProtectedMethods() {
		ListSavingMethodCallback mc = new ListSavingMethodCallback();
		ReflectionUtils.doWithMethods(TestObject.class, mc, new ReflectionUtils.MethodFilter() {
			@Override
			public boolean matches(Method m) {
				return Modifier.isProtected(m.getModifiers());
			}
		});
		assertFalse(mc.getMethodNames().isEmpty());
		assertTrue("Must find protected method on Object", mc.getMethodNames().contains("clone"));
		assertTrue("Must find protected method on Object", mc.getMethodNames().contains("finalize"));
		assertFalse("Public, not protected", mc.getMethodNames().contains("hashCode"));
		assertFalse("Public, not protected", mc.getMethodNames().contains("absquatulate"));
	}

	public void testDuplicatesFound() {
		ListSavingMethodCallback mc = new ListSavingMethodCallback();
		ReflectionUtils.doWithMethods(TestObjectSubclass.class, mc);
		int absquatulateCount = 0;
		for (String name : mc.getMethodNames()) {
			if (name.equals("absquatulate")) {
				++absquatulateCount;
			}
		}
		assertEquals("Found 2 absquatulates", 2, absquatulateCount);
	}

	public void testFindMethod() throws Exception {
		assertNotNull(ReflectionUtils.findMethod(B.class, "bar", String.class));
		assertNotNull(ReflectionUtils.findMethod(B.class, "foo", Integer.class));
		assertNotNull(ReflectionUtils.findMethod(B.class, "getClass"));
	}

//	@Ignore("[SPR-8644] findMethod() does not currently support var-args")
//	@Test
//	public void findMethodWithVarArgs() throws Exception {
//		assertNotNull(ReflectionUtils.findMethod(B.class, "add", int.class, int.class, int.class));
//	}

	public void testGetAllDeclaredMethods() throws Exception {
		class Foo {
			@Override
			public String toString() {
				return super.toString();
			}
		}
		int toStringMethodCount = 0;
		for (Method method : ReflectionUtils.getAllDeclaredMethods(Foo.class)) {
			if (method.getName().equals("toString")) {
				toStringMethodCount++;
			}
		}
		assertThat(toStringMethodCount, is(2));
	}

	public void testGetUniqueDeclaredMethods() throws Exception {
		class Foo {
			@Override
			public String toString() {
				return super.toString();
			}
		}
		int toStringMethodCount = 0;
		for (Method method : ReflectionUtils.getUniqueDeclaredMethods(Foo.class)) {
			if (method.getName().equals("toString")) {
				toStringMethodCount++;
			}
		}
		assertThat(toStringMethodCount, is(1));
	}

	public void testGetUniqueDeclaredMethods_withCovariantReturnType() throws Exception {
		class Parent {
			@SuppressWarnings("unused")
			public Number m1() {
				return new Integer(42);
			}
		}
		class Leaf extends Parent {
			@Override
			public Integer m1() {
				return new Integer(42);
			}
		}
		int m1MethodCount = 0;
		Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(Leaf.class);
		for (Method method : methods) {
			if (method.getName().equals("m1")) {
				m1MethodCount++;
			}
		}
		assertThat(m1MethodCount, is(1));
		assertTrue(ObjectUtils.containsElement(methods, Leaf.class.getMethod("m1")));
		assertFalse(ObjectUtils.containsElement(methods, Parent.class.getMethod("m1")));
	}

	private static class ListSavingMethodCallback implements ReflectionUtils.MethodCallback {

		private List<String> methodNames = new LinkedList<String>();

		private List<Method> methods = new LinkedList<Method>();

		@Override
		public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
			this.methodNames.add(m.getName());
			this.methods.add(m);
		}

		public List<String> getMethodNames() {
			return this.methodNames;
		}

		@SuppressWarnings("unused")
		public List<Method> getMethods() {
			return this.methods;
		}
	}

	private static class TestObjectSubclass extends TestObject {

		@Override
		public void absquatulate() {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestObjectSubclassWithPublicField extends TestObject {

		@SuppressWarnings("unused")
		public String publicField = "foo";
	}

	private static class TestObjectSubclassWithNewField extends TestObject {

		private int magic;

		protected String prot = "foo";
	}

	private static class TestObjectSubclassWithFinalField extends TestObject {

		@SuppressWarnings("unused")
		private final String foo = "will break naive copy that doesn't exclude statics";
	}

	private static class A {

		@SuppressWarnings("unused")
		private void foo(Integer i) throws RemoteException {
		}
	}

	@SuppressWarnings("unused")
	private static class B extends A {

		void bar(String s) throws IllegalArgumentException {
		}

		int add(int... args) {
			int sum = 0;
			for (int i = 0; i < args.length; i++) {
				sum += args[i];
			}
			return sum;
		}
	}

}
