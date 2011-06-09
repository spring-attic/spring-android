/*
 * Copyright 2011 the original author or authors.
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
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import android.test.suitebuilder.annotation.SmallTest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * @author Roy Clarkson
 */
public class GsonHttpMessageConverterTests extends TestCase {

	private GsonHttpMessageConverter converter;
	
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.converter = new GsonHttpMessageConverter();
	}
	
	@Override
	public void tearDown() {
		this.converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(ComplexObject.class, new MediaType("application", "json")));
		assertTrue(converter.canRead(Map.class, new MediaType("application", "json")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(ComplexObject.class, new MediaType("application", "json")));
		assertTrue(converter.canWrite(Map.class, new MediaType("application", "json")));
	}

	@SmallTest
	public void testReadTyped() throws IOException {
		String body = "{\"array\":[\"Foo\",\"Bar\"],\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0,\"nullstring\":null}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF8.displayName()));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		ComplexObject result = (ComplexObject) converter.read(ComplexObject.class, inputMessage);
		assertEquals("Foo", result.getString());
		assertEquals(42, result.getNumber());
		assertEquals(42F, result.getFraction(), 0F);
		assertArrayEquals(new String[]{"Foo", "Bar"}, result.getArray());
		assertTrue(result.isBool());
		assertNull(result.getNullstring());
	}
	
	@SmallTest
	@SuppressWarnings("unchecked")
	public void testReadUntyped() throws IOException {
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		converter.setType(type);
		String body = "{\"string1\":\"Foo\",\"string2\":\"Bar\"}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF8.displayName()));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		Map<String, String> result = (Map<String, String>) converter.read(HashMap.class, inputMessage);
		converter.setType(null);
		assertEquals("Foo", result.get("string1"));
		assertEquals("Bar", result.get("string2"));
	}

	@SmallTest
	@SuppressWarnings("unchecked")
	public void testReadUntypedCustomDeserializer() throws IOException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Object.class, new CustomDeserializer());
		Gson gson = gsonBuilder.create();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		GsonHttpMessageConverter converter = new GsonHttpMessageConverter(gson);
		converter.setType(type);
		String body = "{\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0,\"nullstring\":null}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(UTF8.displayName()));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		Map<String, Object> result = (Map<String, Object>) converter.read(HashMap.class, inputMessage);
		converter = null;
		assertEquals("Foo", result.get("string"));
		Number n = (Number) result.get("number");
		assertEquals(42, n.longValue());
		n = (Number) result.get("fraction");
		assertEquals(42.0, n.doubleValue(), 0D);
		assertEquals(Boolean.TRUE, result.get("bool"));
		assertNull(result.get("nullstring"));
	}
	
	@SmallTest
	public void testWriteUntyped() throws IOException {
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		converter.setType(type);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		Map<String, String> body = new HashMap<String, String>();
		body.put("string1", "Foo");
		body.put("string2", "Bar");
		converter.write(body, null, outputMessage);
		converter.setType(null);
		String result = outputMessage.getBodyAsString(UTF8);
		assertTrue(result.contains("\"string1\":\"Foo\""));
		assertTrue(result.contains("\"string2\":\"Bar\""));
		assertEquals("Invalid content-type", new MediaType("application", "json", UTF8), outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testWriteTyped() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		ComplexObject body = new ComplexObject();
		body.setString("Foo");
		body.setNumber(42);
		body.setFraction(42F);
		body.setArray(new String[]{"Foo", "Bar"});
		body.setBool(true);
		body.setNullstring(null);
		converter.write(body, null, outputMessage);
		String result = outputMessage.getBodyAsString(UTF8);
		assertTrue(result.contains("\"string\":\"Foo\""));
		assertTrue(result.contains("\"number\":42"));
		assertTrue(result.contains("fraction\":42.0"));
		assertTrue(result.contains("\"array\":[\"Foo\",\"Bar\"]"));
		assertTrue(result.contains("\"bool\":true"));
		assertFalse(result.contains("\"nullstring\":null"));
		assertEquals("Invalid content-type", new MediaType("application", "json", UTF8), outputMessage.getHeaders().getContentType());
	}
	
	@SmallTest
	public void testWriteTypedSerializeNulls() throws IOException {
		GsonHttpMessageConverter converter = new GsonHttpMessageConverter(true);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		ComplexObject body = new ComplexObject();
		body.setString("Foo");
		body.setNumber(42);
		body.setFraction(42F);
		body.setArray(new String[]{"Foo", "Bar"});
		body.setBool(true);
		body.setNullstring(null);
		converter.write(body, null, outputMessage);
		String result = outputMessage.getBodyAsString(UTF8);
		assertTrue(result.contains("\"string\":\"Foo\""));
		assertTrue(result.contains("\"number\":42"));
		assertTrue(result.contains("fraction\":42.0"));
		assertTrue(result.contains("\"array\":[\"Foo\",\"Bar\"]"));
		assertTrue(result.contains("\"bool\":true"));
		assertTrue(result.contains("\"nullstring\":null"));
		assertEquals("Invalid content-type", new MediaType("application", "json", UTF8), outputMessage.getHeaders().getContentType());
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
		boolean success = false;
		try {
			String body = "FooBar";
			MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
			inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
			converter.read(ComplexObject.class, inputMessage);
		} catch(HttpMessageNotReadableException e) {
			success = true;
		}
		assertTrue(success);
	}
	
	public void testSetNullGson() {
		boolean success = false;
		try {
			converter.setGson(null);
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue(success);
	}
	
	
	// helpers
	
	private static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
		assertEquals(expecteds.length, actuals.length);
		if (expecteds.length == actuals.length) {
			for (int i = 0; i < expecteds.length; i++) {
				assertEquals(expecteds[i], actuals[i]);
			}
		}
	}

	private static class ComplexObject {

		private String string;

		private int number;

		private float fraction;

		private String[] array;

		private boolean bool;
		
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
	
	private static class CustomDeserializer implements JsonDeserializer<Object> {
		
		public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			if (json.isJsonNull()) {
				return json.getAsJsonNull();
			} else if (json.isJsonPrimitive()) {
				JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
				if (jsonPrimitive.isBoolean()) {
					return jsonPrimitive.getAsBoolean();
				} else if (jsonPrimitive.isString())
					return jsonPrimitive.getAsString();
				else if (jsonPrimitive.isNumber()){ 
					return jsonPrimitive.getAsNumber();
				}
			}
			
			return null;
		}

	}
	
}
