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

import org.springframework.http.MediaType;
import org.springframework.http.MockHttpOutputMessage;

import android.test.suitebuilder.annotation.SmallTest;

/** 
 * @author Roy Clarkson 
 */
public class StringHttpMessageConverterDefaultTests extends AbstractStringHttpMessageConverterTests {

	@Override
	protected StringHttpMessageConverter getStringHttpMessageConverter() {
		return new StringHttpMessageConverter();
	}

	@SmallTest
	public void testWriteDefaultCharset() throws IOException {
		Charset iso88591 = Charset.forName("ISO-8859-1");
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		String body = "H\u00e9llo W\u00f6rld";
		this.converter.write(body, null, outputMessage);
		assertEquals("Invalid result", body, outputMessage.getBodyAsString(iso88591));
		assertEquals("Invalid content-type", new MediaType("text", "plain", iso88591), outputMessage.getHeaders()
				.getContentType());
		assertEquals("Invalid content-length", body.getBytes(iso88591.displayName()).length, outputMessage.getHeaders()
				.getContentLength());
		assertFalse("Invalid accept-charset", outputMessage.getHeaders().getAcceptCharset().isEmpty());
	}

}
