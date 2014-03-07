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

import junit.framework.TestCase;

import org.springframework.android.test.Assert;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;

import android.test.suitebuilder.annotation.SmallTest;

/** 
 * @author Arjen Poutsma
 * @author Roy Clarkson 
 */
public class ByteArrayHttpMessageConverterTests extends TestCase {

	private ByteArrayHttpMessageConverter converter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		converter = new ByteArrayHttpMessageConverter();
	}
	
	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(byte[].class, new MediaType("application", "octet-stream")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(byte[].class, new MediaType("application", "octet-stream")));
		assertTrue(converter.canWrite(byte[].class, MediaType.ALL));
	}

	@SmallTest
	public void testRead() throws IOException {
		byte[] body = new byte[]{0x1, 0x2};
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body);
		inputMessage.getHeaders().setContentType(new MediaType("application", "octet-stream"));
		byte[] result = converter.read(byte[].class, inputMessage);
		Assert.assertArrayEquals("Invalid result", body, result);
	}

	@SmallTest
	public void testWrite() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		byte[] body = new byte[]{0x1, 0x2};
		converter.write(body, null, outputMessage);
		Assert.assertArrayEquals("Invalid result", body, outputMessage.getBodyAsBytes());
		assertEquals("Invalid content-type", new MediaType("application", "octet-stream"),
				outputMessage.getHeaders().getContentType());
		assertEquals("Invalid content-length", 2, outputMessage.getHeaders().getContentLength());
	}

}
