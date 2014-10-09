/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;

import org.mockito.InOrder;
import org.springframework.android.test.DexCacheInstrumentationTestCase;

/**
 * Tests for {@link StreamUtils}.
 *
 * @author Phillip Webb
 * @author Roy Clarkson
 */
public class StreamUtilsTests extends DexCacheInstrumentationTestCase {

	private byte[] bytes = new byte[StreamUtils.BUFFER_SIZE + 10];

	private String string = "";

	@Override
	public void setUp() throws Exception {
		super.setUp();
		new Random().nextBytes(bytes);
		while (string.length() < StreamUtils.BUFFER_SIZE + 10) {
			string += UUID.randomUUID().toString();
		}
	}

	public void testCopyToByteArray() throws Exception {
		InputStream inputStream = spy(new ByteArrayInputStream(bytes));
		byte[] actual = StreamUtils.copyToByteArray(inputStream);
		assertThat(actual, equalTo(bytes));
		verify(inputStream, never()).close();
	}

	public void testCopyToString() throws Exception {
		Charset charset = Charset.defaultCharset();
		InputStream inputStream = spy(new ByteArrayInputStream(string.getBytes(charset.displayName())));
		String actual = StreamUtils.copyToString(inputStream, charset);
		assertThat(actual, equalTo(string));
		verify(inputStream, never()).close();
	}

	public void testCopyBytes() throws Exception {
		ByteArrayOutputStream out = spy(new ByteArrayOutputStream());
		StreamUtils.copy(bytes, out);
		assertThat(out.toByteArray(), equalTo(bytes));
		verify(out, never()).close();
	}

	public void testCopyString() throws Exception {
		Charset charset = Charset.defaultCharset();
		ByteArrayOutputStream out = spy(new ByteArrayOutputStream());
		StreamUtils.copy(string, charset, out);
		assertThat(out.toByteArray(), equalTo(string.getBytes(charset.displayName())));
		verify(out, never()).close();
	}

	public void testCopyStream() throws Exception {
		ByteArrayOutputStream out = spy(new ByteArrayOutputStream());
		StreamUtils.copy(new ByteArrayInputStream(bytes), out);
		assertThat(out.toByteArray(), equalTo(bytes));
		verify(out, never()).close();
	}

	public void testNonClosingInputStream() throws Exception {
		InputStream source = mock(InputStream.class);
		InputStream nonClosing = StreamUtils.nonClosing(source);
		nonClosing.read();
		nonClosing.read(bytes);
		nonClosing.read(bytes, 1, 2);
		nonClosing.close();
		InOrder ordered = inOrder(source);
		ordered.verify(source).read();
		ordered.verify(source).read(bytes, 0, bytes.length);
		ordered.verify(source).read(bytes, 1, 2);
		ordered.verify(source, never()).close();
	}

	public void testNonClosingOutputStream() throws Exception {
		OutputStream source = mock(OutputStream.class);
		OutputStream nonClosing = StreamUtils.nonClosing(source);
		nonClosing.write(1);
		nonClosing.write(bytes);
		nonClosing.write(bytes, 1, 2);
		nonClosing.close();
		InOrder ordered = inOrder(source);
		ordered.verify(source).write(1);
		ordered.verify(source).write(bytes, 0, bytes.length);
		ordered.verify(source).write(bytes, 1, 2);
		ordered.verify(source, never()).close();
	}
}
