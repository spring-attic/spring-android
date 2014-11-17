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

package org.springframework.http.converter;

import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;

import android.test.suitebuilder.annotation.SmallTest;

/** 
 * @author Arjen Poutsma
 * @author Roy Clarkson 
 */
public abstract class AbstractStringHttpMessageConverterTests extends TestCase {

	protected StringHttpMessageConverter converter;

	protected abstract StringHttpMessageConverter getStringHttpMessageConverter();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.converter = getStringHttpMessageConverter();
	}

	@Override
	public void tearDown() {
		this.converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(this.converter.canRead(String.class, new MediaType("text", "plain")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(this.converter.canWrite(String.class, new MediaType("text", "plain")));
		assertTrue(this.converter.canWrite(String.class, MediaType.ALL));
	}

	@SmallTest
	public void testRead() throws IOException {
		String body = "Hello World";
		Charset charset = Charset.forName("UTF-8");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(charset.displayName()));
		inputMessage.getHeaders().setContentType(new MediaType("text", "plain", charset));
		String result = converter.read(String.class, inputMessage);
		assertEquals("Invalid result", body, result);
	}

	@SmallTest
	public void testWriteUTF8() throws IOException {
		Charset utf8 = Charset.forName("UTF-8");
		MediaType contentType = new MediaType("text", "plain", utf8);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		String body = "H\u00e9llo W\u00f6rld";
		converter.write(body, contentType, outputMessage);
		assertEquals("Invalid result", body, outputMessage.getBodyAsString(utf8));
		assertEquals("Invalid content-type", contentType, outputMessage.getHeaders().getContentType());
		assertEquals("Invalid content-length", body.getBytes(utf8.displayName()).length,
				outputMessage.getHeaders().getContentLength());
		assertFalse("Invalid accept-charset", outputMessage.getHeaders().getAcceptCharset().isEmpty());
	}

	// SPR-8867

	@SmallTest
	public void testWriteOverrideRequestedContentType() throws IOException {
		Charset utf8 = Charset.forName("UTF-8");
		MediaType requestedContentType = new MediaType("text", "html");
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = new MediaType("text", "plain", utf8);
		outputMessage.getHeaders().setContentType(contentType);
		String body = "H\u00e9llo W\u00f6rld";
		converter.write(body, requestedContentType, outputMessage);
		assertEquals("Invalid result", body, outputMessage.getBodyAsString(utf8));
		assertEquals("Invalid content-type", contentType, outputMessage.getHeaders().getContentType());
		assertEquals("Invalid content-length", body.getBytes(utf8.displayName()).length,
				outputMessage.getHeaders().getContentLength());
		assertFalse("Invalid accept-charset", outputMessage.getHeaders().getAcceptCharset().isEmpty());
	}

}
