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

package org.springframework.http.client;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;

import android.os.Build;
import android.test.suitebuilder.annotation.MediumTest;

public class BufferingSimpleClientHttpRequestFactoryTests extends BufferingAbstractClientHttpRequestFactoryTests {

	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
	}

	@MediumTest
	@Override
	public void testGetAcceptEncodingNone() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/noencoding"), HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertFalse("Header found", response.getHeaders().containsKey("Content-Encoding"));
			byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(body.length);
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			FileCopyUtils.copy(body, gzipOutputStream);
			byte[] compressedBody = byteArrayOutputStream.toByteArray();
			long contentLength = response.getHeaders().getContentLength();
			// Gingerbread and newer seamlessly request and handle gzip responses from the server 
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				// content-length is not being set in Jelly Bean 4.2, 4.3, and KitKat 4.4!!!
				assertEquals(-1, contentLength);
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				// content-length is being set to the length of the compressed data
				assertEquals("Invalid content-length", compressedBody.length, contentLength);
			}
			else {
				assertEquals("Invalid content-length", body.length, contentLength);
			}
		}
		finally {
			response.close();
		}
	}

}
