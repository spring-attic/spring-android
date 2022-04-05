/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.converter;

import java.math.BigInteger;
import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * Test cases for {@link ObjectToStringHttpMessageConverter} class.
 *
 * @author <a href="mailto:dmitry.katsubo@gmail.com">Dmitry Katsubo</a>
 * @author Rossen Stoyanchev
 * @author Roy Clarkson
 */
public class ObjectToStringHttpMessageConverterTests extends TestCase {

	private ObjectToStringHttpMessageConverter converter;


	@Override
	public void setUp() {
		ConversionService conversionService = new DefaultConversionService();
		this.converter = new ObjectToStringHttpMessageConverter(conversionService);
	}

	@SmallTest
	public void testCanRead() {
		assertFalse(this.converter.canRead(Math.class, null));
		assertFalse(this.converter.canRead(Resource.class, null));

		assertTrue(this.converter.canRead(Locale.class, null));
		assertTrue(this.converter.canRead(BigInteger.class, null));

		assertFalse(this.converter.canRead(BigInteger.class, MediaType.TEXT_HTML));
		assertFalse(this.converter.canRead(BigInteger.class, MediaType.TEXT_XML));
		assertFalse(this.converter.canRead(BigInteger.class, MediaType.APPLICATION_XML));
	}

	@SmallTest
	public void testCanWrite() {
		assertFalse(this.converter.canWrite(Math.class, null));
		assertFalse(this.converter.canWrite(Resource.class, null));

		assertTrue(this.converter.canWrite(Locale.class, null));
		assertTrue(this.converter.canWrite(Double.class, null));

		assertFalse(this.converter.canWrite(BigInteger.class, MediaType.TEXT_HTML));
		assertFalse(this.converter.canWrite(BigInteger.class, MediaType.TEXT_XML));
		assertFalse(this.converter.canWrite(BigInteger.class, MediaType.APPLICATION_XML));

		assertTrue(this.converter.canWrite(BigInteger.class, MediaType.valueOf("text/*")));
	}

//	@SmallTest
//	public void testDefaultCharset() throws IOException {
//		this.converter.write(Integer.valueOf(5), null, response);
//
//		assertEquals("ISO-8859-1", servletResponse.getCharacterEncoding());
//	}

//	@SmallTest
//	public void testDefaultCharsetModified() throws IOException {
//		Charset charset = Charset.forName("UTF-16");
//		ConversionService cs = new DefaultConversionService();
//		ObjectToStringHttpMessageConverter converter = new ObjectToStringHttpMessageConverter(cs, charset);
//		converter.write((byte) 31, null, this.response);
//
//		assertEquals("UTF-16", this.servletResponse.getCharacterEncoding());
//	}

//	@SmallTest
//	public void testWriteAcceptCharset() throws IOException {
//		this.converter.write(new Date(), null, this.response);
//
//		assertNotNull(this.servletResponse.getHeader("Accept-Charset"));
//	}

//	@SmallTest
//	public void testWriteAcceptCharsetTurnedOff() throws IOException {
//		this.converter.setWriteAcceptCharset(false);
//		this.converter.write(new Date(), null, this.response);
//
//		assertNull(this.servletResponse.getHeader("Accept-Charset"));
//	}

//	@SmallTest
//	public void testRead() throws IOException {
//		MockHttpServletRequest request = new MockHttpServletRequest();
//
//		request.setContentType(MediaType.TEXT_PLAIN_VALUE);
//
//		Short shortValue = Short.valueOf((short) 781);
//
//		request.setContent(shortValue.toString().getBytes(
//				StringHttpMessageConverter.DEFAULT_CHARSET));
//
//		assertEquals(shortValue, this.converter.read(Short.class, new ServletServerHttpRequest(request)));
//
//		Float floatValue = Float.valueOf(123);
//
//		request.setCharacterEncoding("UTF-16");
//		request.setContent(floatValue.toString().getBytes("UTF-16"));
//
//		assertEquals(floatValue, this.converter.read(Float.class, new ServletServerHttpRequest(request)));
//
//		Long longValue = Long.valueOf(55819182821331L);
//
//		request.setCharacterEncoding("UTF-8");
//		request.setContent(longValue.toString().getBytes("UTF-8"));
//
//		assertEquals(longValue, this.converter.read(Long.class, new ServletServerHttpRequest(request)));
//	}

//	@SmallTest
//	public void testWrite() throws IOException {
//		this.converter.write((byte) -8, null, this.response);
//
//		assertEquals("ISO-8859-1", this.servletResponse.getCharacterEncoding());
//		assertTrue(this.servletResponse.getContentType().startsWith(MediaType.TEXT_PLAIN_VALUE));
//		assertEquals(2, this.servletResponse.getContentLength());
//		assertArrayEquals(new byte[] { '-', '8' }, this.servletResponse.getContentAsByteArray());
//	}

//	@SmallTest
//	public void testWriteUtf16() throws IOException {
//		MediaType contentType = new MediaType("text", "plain", Charset.forName("UTF-16"));
//		this.converter.write(Integer.valueOf(958), contentType, this.response);
//
//		assertEquals("UTF-16", this.servletResponse.getCharacterEncoding());
//		assertTrue(this.servletResponse.getContentType().startsWith(MediaType.TEXT_PLAIN_VALUE));
//		assertEquals(8, this.servletResponse.getContentLength());
//		// First two bytes: byte order mark
//		assertArrayEquals(new byte[] { -2, -1, 0, '9', 0, '5', 0, '8' }, this.servletResponse.getContentAsByteArray());
//	}

	@SmallTest
	public void testConversionServiceRequired() {
		try {
			new ObjectToStringHttpMessageConverter(null);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}
	}

}
