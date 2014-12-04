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

package org.springframework.http.converter.xml;

import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;
import org.springframework.android.test.Assert;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import android.test.suitebuilder.annotation.SmallTest;

/** 
 * @author Roy Clarkson 
 */
public class SimpleXmlHttpMessageConverterTests extends TestCase {

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

	private SimpleXmlHttpMessageConverter converter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.converter = new SimpleXmlHttpMessageConverter();
	}

	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue("Converter does not support reading @Root", converter.canRead(SimpleObject.class, new MediaType("application", "xml")));
		assertTrue("Converter does not support reading @Root", converter.canRead(SimpleObject.class, new MediaType("text", "xml")));
		assertTrue("Converter does not support reading @Root", converter.canRead(SimpleObject.class, new MediaType("application", "*+xml")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue("Converter does not support writing @Root", converter.canWrite(SimpleObject.class, new MediaType("application", "xml")));
		assertTrue("Converter does not support writing @Root", converter.canWrite(SimpleObject.class, new MediaType("text", "xml")));
		assertTrue("Converter does not support writing @Root", converter.canWrite(SimpleObject.class, new MediaType("application", "*+xml")));
	}

	@SmallTest
	public void testCanReadNoRootAnnotation() {
		// @Root annotation not required for reading
		assertTrue("Converter does not support reading @Root", converter.canRead(SimpleNoRootAnnotationObject.class, new MediaType("application", "xml")));
	}

	@SmallTest
	public void testCanWriteNoRootAnnotation() {
		// @Root annotation is required for writing
		assertFalse("Converter does not support reading @Root", converter.canWrite(SimpleNoRootAnnotationObject.class, new MediaType("application", "xml")));
	}

	@SmallTest
	public void testReadSimple() throws IOException {
		String body = "<root number=\"123\"><string>Example message</string></root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF_8.displayName()));
		SimpleObject result = (SimpleObject) converter.read(SimpleObject.class, inputMessage);
		assertEquals("Invalid result", 123, result.getNumber());
		assertEquals("Invalid result", "Example message", result.getString());
	}

	@SmallTest
	public void testReadComplex() throws IOException {
		String body = "<root number=\"123\"><string>Example message</string><fraction>42.0</fraction><array length=\"2\"><string>Foo</string><string>Bar</string></array><bool>true</bool></root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF_8.displayName()));
		ComplexObject result = (ComplexObject) converter.read(ComplexObject.class, inputMessage);
		assertEquals("Invalid result", 123, result.getNumber());
		assertEquals("Invalid result", "Example message", result.getString());
		assertEquals("Invalid result", 42.0f, result.getFraction());
		Assert.assertArrayEquals("Invalid result", new String[]{"Foo", "Bar"}, result.getArray());
		assertTrue(result.isBool());
		assertNull(result.getNullstring());
	}

	@SmallTest
	public void testReadSimpleNonAnnotated() throws IOException {
		String body = "<root><string>Example message</string><number>123</number></root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF_8.displayName()));
		SimpleNonAnnotatedObject result = (SimpleNonAnnotatedObject) converter.read(SimpleNonAnnotatedObject.class, inputMessage);
		assertEquals("Invalid result", "Example message", result.getString());
		assertEquals("Invalid result", 123, result.getNumber());
	}

	@SmallTest
	public void testReadSimpleNoRootAnnotation() throws IOException {
		String body = "<root><string>Example message</string><number>123</number></root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF_8.displayName()));
		SimpleNoRootAnnotationObject result = (SimpleNoRootAnnotationObject) converter.read(SimpleNoRootAnnotationObject.class, inputMessage);
		assertEquals("Invalid result", "Example message", result.getString());
		assertEquals("Invalid result", 123, result.getNumber());
	}

	@SmallTest
	public void testReadSimple_ISO_8859_1() throws IOException {
		String body = "<root number=\"123\"><string>&#160;&#161;&#162;&#163;&#164;&#165;</string></root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(ISO_8859_1.displayName()));
		SimpleObject result = (SimpleObject) converter.read(SimpleObject.class, inputMessage);
		assertEquals("Invalid result", 123, result.getNumber());
		byte[] bytes = new byte[] {(byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5};
		Assert.assertArrayEquals("Invalid result", bytes, result.getString().getBytes(ISO_8859_1.displayName()));
		assertEquals("Invalid result", new String(bytes, ISO_8859_1.displayName()), result.getString());
	}

	@SmallTest
	public void testWriteSimple() throws IOException {
		SimpleObject body = new SimpleObject();
		body.setString("Example message");
		body.setNumber(123);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(body, null, outputMessage);
		String result = outputMessage.getBodyAsString(UTF_8);
		assertTrue(result.contains("<string>Example message</string>"));
		assertTrue(result.contains("number=\"123\""));
		assertEquals("Invalid content-type", new MediaType("application", "xml"), outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testWriteComplex() throws IOException {
		ComplexObject body = new ComplexObject();
		body.setString("Example message");
		body.setNumber(123);
		body.setFraction(42.0f);
		body.setArray(new String[] {"Foo", "Bar" });
		body.setBool(true);
		body.setNullstring(null);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(body, null, outputMessage);
		String result = outputMessage.getBodyAsString(UTF_8);
		assertTrue(result.contains("<string>Example message</string>"));
		assertTrue(result.contains("number=\"123\""));
		assertTrue(result.contains("<fraction>42.0</fraction>"));
		assertTrue(result.contains("<array length=\"2\">") && result.contains("<string>Foo</string>") && result.contains("<string>Bar</string>") && result.contains("</array>"));
		assertFalse(result.contains("<nullstring>"));
		assertEquals("Invalid content-type", new MediaType("application", "xml"), outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testWrite_ISO_8859_1() throws IOException {
		MediaType contentType = new MediaType("text", "xml", ISO_8859_1);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		byte[] bytes = new byte[] {(byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5};
		String body = new String(bytes, ISO_8859_1.displayName());
		converter.write(body, contentType, outputMessage);
		assertEquals("Invalid result", "<string>" + body + "</string>", outputMessage.getBodyAsString(ISO_8859_1));
		assertEquals("Invalid content-type", contentType, outputMessage.getHeaders().getContentType());
	}


	// helpers

	@Root
	private static class SimpleObject {

		@Element
		private String string;

		@Attribute
		private int number;

		public void setString(String string) {
			this.string = string;
		}

		public String getString() {
			return string;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}

	}

	@SuppressWarnings("unused")
	private static class SimpleNonAnnotatedObject {

		private String string;

		private int number;

		public void setString(String string) {
			this.string = string;
		}

		public String getString() {
			return string;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}

	}

	@SuppressWarnings("unused")
	private static class SimpleNoRootAnnotationObject {

		@Element
		private String string;

		@Element
		private int number;

		public void setString(String string) {
			this.string = string;
		}

		public String getString() {
			return string;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}

	}

	@Root
	private static class ComplexObject {

		@Element
		private String string;

		@Attribute
		private int number;

		@Element
		private float fraction;

		@ElementArray
		private String[] array;

		@Element
		private boolean bool;
		
		@Element(required = false)
		private String nullstring;

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public float getFraction() {
			return fraction;
		}

		public void setFraction(float fraction) {
			this.fraction = fraction;
		}

		public String[] getArray() {
			return array;
		}

		public void setArray(String[] array) {
			this.array = array;
		}

		public void setNullstring(String nullstring) {
			this.nullstring = nullstring;
		}

		public String getNullstring() {
			return nullstring;
		}

	}

}
