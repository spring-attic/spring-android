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

package org.springframework.http.converter.json;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.android.test.Assert;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Roy Clarkson
 */
public abstract class AbstractMappingJacksonHttpMessageConverterTests<T extends HttpMessageConverter<Object>>
		extends TestCase {
	
	protected static final String NEWLINE_SYSTEM_PROPERTY = System.getProperty("line.separator");

	protected T converter;
	
	protected T getConverter() {
		return this.converter;
	}

	protected abstract void prepareReadGenericsTest();

	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(MyBean.class, new MediaType("application", "json")));
		assertTrue(converter.canRead(Map.class, new MediaType("application", "json")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(MyBean.class, new MediaType("application", "json")));
		assertTrue(converter.canWrite(Map.class, new MediaType("application", "json")));
	}

	@SmallTest
	public void testCanReadAndWriteMicroformats() {
		assertTrue(converter.canRead(MyBean.class, new MediaType("application", "vnd.test-micro-type+json")));
		assertTrue(converter.canWrite(MyBean.class, new MediaType("application", "vnd.test-micro-type+json")));
	}

	@SmallTest
	public void testReadTyped() throws IOException {
		String body =
				"{\"bytes\":\"AQI=\",\"array\":[\"Foo\",\"Bar\"],\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		MyBean result = (MyBean) converter.read(MyBean.class, inputMessage);
		assertEquals("Foo", result.getString());
		assertEquals(42, result.getNumber());
		assertEquals(42F, result.getFraction(), 0F);
		Assert.assertArrayEquals(new String[]{"Foo", "Bar"}, result.getArray());
		assertTrue(result.isBool());
		Assert.assertArrayEquals(new byte[]{0x1, 0x2}, result.getBytes());
	}

	@SmallTest
	@SuppressWarnings("unchecked")
	public void testReadGenerics() throws IOException {
		prepareReadGenericsTest();
		String body =
				"[{\"bytes\":\"AQI=\",\"array\":[\"Foo\",\"Bar\"],\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}]";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));

		List<MyBean> results = (List<MyBean>) converter.read(List.class, inputMessage);
		assertEquals(1, results.size());
		MyBean result = results.get(0);
		assertEquals("Foo", result.getString());
		assertEquals(42, result.getNumber());
		assertEquals(42F, result.getFraction(), 0F);
		Assert.assertArrayEquals(new String[]{"Foo", "Bar"}, result.getArray());
		assertTrue(result.isBool());
		Assert.assertArrayEquals(new byte[]{0x1, 0x2}, result.getBytes());
	}

	@SmallTest
	@SuppressWarnings("unchecked")
	public void testReadParameterizedType() throws IOException {
		ParameterizedTypeReference<List<MyBean>> beansList = new ParameterizedTypeReference<List<MyBean>>() {};

		String body =
				"[{\"bytes\":\"AQI=\",\"array\":[\"Foo\",\"Bar\"],\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}]";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		List<MyBean> results = (List<MyBean>) converter.read(beansList.getType(), null, inputMessage);
		assertEquals(1, results.size());
		MyBean result = results.get(0);
		assertEquals("Foo", result.getString());
		assertEquals(42, result.getNumber());
		assertEquals(42F, result.getFraction(), 0F);
		Assert.assertArrayEquals(new String[]{"Foo", "Bar"}, result.getArray());
		assertTrue(result.isBool());
		Assert.assertArrayEquals(new byte[]{0x1, 0x2}, result.getBytes());
	}

	@SmallTest
	@SuppressWarnings("unchecked")
	public void testReadUntyped() throws IOException {
		String body =
				"{\"bytes\":\"AQI=\",\"array\":[\"Foo\",\"Bar\"],\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		HashMap<String, Object> result = (HashMap<String, Object>) converter.read(HashMap.class, inputMessage);
		assertEquals("Foo", result.get("string"));
		assertEquals(42, result.get("number"));
		assertEquals(42D, (Double) result.get("fraction"), 0D);
		List<String> array = new ArrayList<String>();
		array.add("Foo");
		array.add("Bar");
		assertEquals(array, result.get("array"));
		assertEquals(Boolean.TRUE, result.get("bool"));
		assertEquals("AQI=", result.get("bytes"));
	}

	@SmallTest
	public void testWrite() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MyBean body = new MyBean();
		body.setString("Foo");
		body.setNumber(42);
		body.setFraction(42F);
		body.setArray(new String[]{"Foo", "Bar"});
		body.setBool(true);
		body.setBytes(new byte[]{0x1, 0x2});
		converter.write(body, null, outputMessage);
		Charset utf8 = Charset.forName("UTF-8");
		String result = outputMessage.getBodyAsString(utf8);
		assertTrue(result.contains("\"string\":\"Foo\""));
		assertTrue(result.contains("\"number\":42"));
		assertTrue(result.contains("fraction\":42.0"));
		assertTrue(result.contains("\"array\":[\"Foo\",\"Bar\"]"));
		assertTrue(result.contains("\"bool\":true"));
		assertTrue(result.contains("\"bytes\":\"AQI=\""));
		assertEquals("Invalid content-type", new MediaType("application", "json", utf8),
				outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testWriteUTF16() throws IOException {
		Charset utf16 = Charset.forName("UTF-16BE");
		MediaType contentType = new MediaType("application", "json", utf16);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		String body = "H\u00e9llo W\u00f6rld";
		converter.write(body, contentType, outputMessage);
		assertEquals("Invalid result", "\"" + body + "\"", outputMessage.getBodyAsString(utf16));
		assertEquals("Invalid content-type", contentType, outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testReadInvalidJson() throws IOException {
		String body = "FooBar";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		try {
			converter.read(MyBean.class, inputMessage);
			fail("expected HttpMessageNotReadableException");
		} catch (HttpMessageNotReadableException e) {
			// expected
		}
	}

	@SmallTest
	public void testReadValidJsonWithUnknownProperty() throws IOException {
		String body = "{\"string\":\"string\",\"unknownProperty\":\"value\"}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		try {
			converter.read(MyBean.class, inputMessage);
			fail("expected HttpMessageNotReadableException");
		} catch (HttpMessageNotReadableException e) {
			// expected
		}
	}

	public static class MyBean {

		private String string;

		private int number;

		private float fraction;

		private String[] array;

		private boolean bool;

		private byte[] bytes;

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

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
	}

}
