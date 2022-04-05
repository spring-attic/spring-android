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

import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * Test-case for AbstractHttpMessageConverter.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class HttpMessageConverterTests extends TestCase {

	private static final MediaType MEDIA_TYPE = new MediaType("foo", "bar");

	@SmallTest
	public void testCanRead() {
		AbstractHttpMessageConverter<MyType> converter = new MyHttpMessageConverter<MyType>(MEDIA_TYPE) {
			@Override
			protected boolean supports(Class<?> clazz) {
				return MyType.class.equals(clazz);
			}

		};

		assertTrue(converter.canRead(MyType.class, MEDIA_TYPE));
		assertFalse(converter.canRead(MyType.class, new MediaType("foo", "*")));
		assertFalse(converter.canRead(MyType.class, MediaType.ALL));
	}

	@SmallTest
	public void testCanWrite() {
		AbstractHttpMessageConverter<MyType> converter = new MyHttpMessageConverter<MyType>(MEDIA_TYPE) {
			@Override
			protected boolean supports(Class<?> clazz) {
				return MyType.class.equals(clazz);
			}

		};

		assertTrue(converter.canWrite(MyType.class, MEDIA_TYPE));
		assertTrue(converter.canWrite(MyType.class, new MediaType("foo", "*")));
		assertTrue(converter.canWrite(MyType.class, MediaType.ALL));
	}


	private static class MyHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {

		private MyHttpMessageConverter(MediaType supportedMediaType) {
			super(supportedMediaType);
		}

		@Override
		protected boolean supports(Class<?> clazz) {
			fail("Not expected");
			return false;
		}

		@SuppressWarnings("rawtypes")
		@Override
		protected T readInternal(Class clazz, HttpInputMessage inputMessage)
				throws IOException, HttpMessageNotReadableException {
			fail("Not expected");
			return null;
		}

		@Override
		protected void writeInternal(T t, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {
			fail("Not expected");
		}
	}

	private static class MyType {

	}

}
