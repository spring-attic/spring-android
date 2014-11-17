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
import java.util.Arrays;

import org.springframework.core.io.AssetResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.util.FileCopyUtils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class ResourceHttpMessageConverterTests extends AndroidTestCase {

	private ResourceHttpMessageConverter converter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		converter = new ResourceHttpMessageConverter();
	}
	
	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(Resource.class, new MediaType("application", "octet-stream")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(Resource.class, new MediaType("application", "octet-stream")));
		assertTrue(converter.canWrite(Resource.class, MediaType.ALL));
	}

	@MediumTest
	public void testRead() throws IOException {
		Resource asset = new AssetResource(getContext().getAssets(), "logo.jpg");
		byte[] body = FileCopyUtils.copyToByteArray(asset.getInputStream());
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body);
		inputMessage.getHeaders().setContentType(MediaType.IMAGE_JPEG);
		converter.read(Resource.class, inputMessage);
	}

	@MediumTest
	public void testWrite() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		Resource asset = new AssetResource(getContext().getAssets(), "logo.jpg");
		converter.write(asset, null, outputMessage);
		assertEquals("Invalid content-type", MediaType.APPLICATION_OCTET_STREAM, outputMessage.getHeaders().getContentType());
		assertEquals("Invalid content-length", asset.contentLength(), outputMessage.getHeaders().getContentLength());
	}

	// SPR-10848

	@SmallTest
	public void writeByteArrayNullMediaType() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		byte[] byteArray = {1, 2, 3};
		Resource body = new ByteArrayResource(byteArray);
		converter.write(body, null, outputMessage);
		assertTrue(Arrays.equals(byteArray, outputMessage.getBodyAsBytes()));
	}

}
