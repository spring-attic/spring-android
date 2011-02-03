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

package org.springframework.http.converter.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;

/** @author Roy Clarkson */
public class SimpleXmlHttpMessageConverterTests {

	private SimpleXmlHttpMessageConverter converter;
	private final Charset UTF8 = Charset.forName("UTF-8");

	@Before
	public void setUp() {
		this.converter = new SimpleXmlHttpMessageConverter();
	}
		
	@Test
	public void canRead() throws Exception {
		assertTrue("Converter does not support reading @Root", converter.canRead(MockObject.class, new MediaType("application", "xml")));
		assertTrue("Converter does not support reading @Root", converter.canRead(MockObject.class, new MediaType("application", "xml", UTF8)));
		assertTrue("Converter does not support reading @Root", converter.canRead(MockObject.class, new MediaType("text", "xml")));
		assertTrue("Converter does not support reading @Root", converter.canRead(MockObject.class, new MediaType("text", "xml", UTF8)));
		assertTrue("Converter does not support reading @Root", converter.canRead(MockObject.class, new MediaType("application", "*+xml")));
		assertTrue("Converter does not support reading @Root", converter.canRead(MockObject.class, new MediaType("application", "*+xml", UTF8)));
	}
	
	@Test
	public void canWrite() throws Exception {
		assertTrue("Converter does not support writing @Root", converter.canWrite(MockObject.class, new MediaType("application", "xml")));
		assertTrue("Converter does not support writing @Root", converter.canWrite(MockObject.class, new MediaType("application", "xml", UTF8)));
		assertTrue("Converter does not support writing @Root", converter.canWrite(MockObject.class, new MediaType("text", "xml")));
		assertTrue("Converter does not support writing @Root", converter.canWrite(MockObject.class, new MediaType("text", "xml", UTF8)));
		assertTrue("Converter does not support writing @Root", converter.canWrite(MockObject.class, new MediaType("application", "*+xml")));
		assertTrue("Converter does not support writing @Root", converter.canWrite(MockObject.class, new MediaType("application", "*+xml", UTF8)));
	}

	@Test
	public void read() throws Exception {
		String body = "<mockObject index=\"123\"><text>Example message</text></mockObject>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		MockObject result = (MockObject) converter.read(MockObject.class, inputMessage);
		assertEquals("Invalid result", 123, result.getId());
		assertEquals("Invalid result", "Example message", result.getMessage());
	}

	@Test
	public void write() throws Exception {
		MockObject mockObject = new MockObject("Example message", 123);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(mockObject, null, outputMessage);
		assertEquals("Invalid content-type", new MediaType("application", "xml"), outputMessage.getHeaders().getContentType());
	}
}
